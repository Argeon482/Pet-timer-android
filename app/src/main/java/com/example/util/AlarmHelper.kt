package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.provider.CalendarContract
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

data class AlarmScheduleResult(
    val launchedClockApp: Boolean,
    val targetHour24: Int,
    val targetMinute: Int,
    val formattedTime: String,
    val label: String,
    val hoursFromNow: Double
)

data class AlarmActionItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val hoursFromNow: Double,
    val label: String,
    val categoryKey: String = "general",
    val clusterCount: Int = 1
)

object AlarmHelper {

    fun computeTargetCalendar(hoursFromNow: Double, nowCalendar: Calendar = Calendar.getInstance()): Calendar {
        val minutesToAdd = (hoursFromNow.coerceAtLeast(0.02) * 60.0).roundToInt().coerceAtLeast(1)
        return (nowCalendar.clone() as Calendar).apply {
            add(Calendar.MINUTE, minutesToAdd)
        }
    }

    fun computeTargetAlarmTime(hoursFromNow: Double, nowCalendar: Calendar = Calendar.getInstance()): Pair<Int, Int> {
        val cal = computeTargetCalendar(hoursFromNow, nowCalendar)
        return cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.MINUTE)
    }

    fun formatTargetDateTime(targetCal: Calendar): String {
        val sdf = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.US)
        return sdf.format(targetCal.time)
    }

    fun formatTargetDateShort(targetCal: Calendar): String {
        val sdf = SimpleDateFormat("EEE, MMM d", Locale.US)
        return sdf.format(targetCal.time)
    }

    fun formatRelativeDuration(hoursFromNow: Double): String {
        val totalMinutes = (hoursFromNow * 60.0).roundToInt()
        val days = totalMinutes / (24 * 60)
        val remainingHours = (totalMinutes % (24 * 60)) / 60
        val remainingMins = totalMinutes % 60
        return when {
            days > 0 && remainingHours > 0 -> "${days}d ${remainingHours}h"
            days > 0 -> "${days}d"
            remainingHours > 0 -> "${remainingHours}h ${remainingMins}m"
            else -> "${remainingMins}m"
        }
    }

    fun formatTime24(hour: Int, minute: Int): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val hour12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format(Locale.US, "%02d:%02d (%d:%02d %s)", hour, minute, hour12, minute, amPm)
    }

    /**
     * Launches the user's Android Clock app using [AlarmClock.ACTION_SET_ALARM]
     * pre-populated with the exact hour, minute, day-of-week, and Flyff Mac NPC action label.
     */
    fun scheduleClockAlarm(
        context: Context,
        hoursFromNow: Double,
        label: String,
        skipUi: Boolean = false
    ): AlarmScheduleResult {
        val targetCal = computeTargetCalendar(hoursFromNow)
        val hour = targetCal.get(Calendar.HOUR_OF_DAY)
        val minute = targetCal.get(Calendar.MINUTE)
        val formattedTime = formatTargetDateTime(targetCal)

        val datePrefix = SimpleDateFormat("EEE MMM d", Locale.US).format(targetCal.time)
        val enrichedLabel = "[$datePrefix] $label"

        val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, enrichedLabel)
            putExtra(AlarmClock.EXTRA_SKIP_UI, skipUi)

            // Pass the target day of week for alarms within 7 days so Android Clock doesn't default to today!
            if (hoursFromNow <= 168.0) {
                val dayOfWeek = targetCal.get(Calendar.DAY_OF_WEEK)
                putExtra(AlarmClock.EXTRA_DAYS, arrayListOf(dayOfWeek))
            }

            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val launched = try {
            context.startActivity(alarmIntent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }

        return AlarmScheduleResult(
            launchedClockApp = launched,
            targetHour24 = hour,
            targetMinute = minute,
            formattedTime = formattedTime,
            label = enrichedLabel,
            hoursFromNow = hoursFromNow
        )
    }

    /**
     * Pre-populates a Google Calendar or Samsung Calendar Event Reminder with the exact
     * calendar date and time (e.g., Nov 10, Nov 11, Nov 19) with a notification alarm.
     */
    fun scheduleCalendarReminder(
        context: Context,
        hoursFromNow: Double,
        label: String
    ): Boolean {
        val targetCal = computeTargetCalendar(hoursFromNow)
        val startMs = targetCal.timeInMillis
        val endMs = startMs + (30 * 60 * 1000) // 30 min duration

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMs)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMs)
            putExtra(CalendarContract.Events.TITLE, label)
            putExtra(CalendarContract.Events.DESCRIPTION, "Flyff Pet Empire Alert: Action required for your house pets.")
            putExtra(CalendarContract.Events.HAS_ALARM, 1)
            putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Consolidates timers by class and 1-hour window:
     * "Any timer that is for the same class pet and within 1 hour of other same class pets
     * should be reduced to 1 timer (taking only the earliest timer for that group)".
     */
    fun consolidateAlarmItems(items: List<AlarmActionItem>): List<AlarmActionItem> {
        if (items.isEmpty()) return emptyList()

        // Group items by category (e.g. "tier_C", "tier_B", "s_tier_bake", "mac_A")
        val byCategory = items.groupBy { it.categoryKey }
        val consolidated = mutableListOf<AlarmActionItem>()

        for ((_, groupItems) in byCategory) {
            val sorted = groupItems.sortedBy { it.hoursFromNow }
            var currentClusterEarliest: AlarmActionItem? = null
            var clusterCount = 0

            for (item in sorted) {
                if (currentClusterEarliest == null) {
                    currentClusterEarliest = item
                    clusterCount = 1
                } else {
                    // Check if within 1.0 hour of the cluster's earliest timer
                    if (item.hoursFromNow - currentClusterEarliest.hoursFromNow <= 1.0) {
                        clusterCount++
                    } else {
                        // Emit completed cluster with combined label if multiple houses
                        val finalLabel = if (clusterCount > 1) {
                            "${currentClusterEarliest.label} ($clusterCount houses within 1h)"
                        } else {
                            currentClusterEarliest.label
                        }
                        consolidated.add(
                            currentClusterEarliest.copy(
                                label = finalLabel,
                                clusterCount = clusterCount
                            )
                        )

                        currentClusterEarliest = item
                        clusterCount = 1
                    }
                }
            }

            if (currentClusterEarliest != null) {
                val finalLabel = if (clusterCount > 1) {
                    "${currentClusterEarliest.label} ($clusterCount houses within 1h)"
                } else {
                    currentClusterEarliest.label
                }
                consolidated.add(
                    currentClusterEarliest.copy(
                        label = finalLabel,
                        clusterCount = clusterCount
                    )
                )
            }
        }

        return consolidated.sortedBy { it.hoursFromNow }
    }

    /**
     * Launches the Clock app countdown timer using [AlarmClock.ACTION_SET_TIMER].
     */
    fun scheduleCountdownTimer(
        context: Context,
        hoursFromNow: Double,
        label: String,
        skipUi: Boolean = false
    ): Boolean {
        val seconds = (hoursFromNow.coerceAtLeast(0.01) * 3600.0).roundToInt().coerceAtLeast(60)
        val timerIntent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, skipUi)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(timerIntent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
