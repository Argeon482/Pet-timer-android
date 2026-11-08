package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
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

object AlarmHelper {

    fun computeTargetAlarmTime(hoursFromNow: Double, nowCalendar: Calendar = Calendar.getInstance()): Pair<Int, Int> {
        val minutesToAdd = (hoursFromNow.coerceAtLeast(0.02) * 60.0).roundToInt().coerceAtLeast(1)
        val targetCal = (nowCalendar.clone() as Calendar).apply {
            add(Calendar.MINUTE, minutesToAdd)
        }
        return targetCal.get(Calendar.HOUR_OF_DAY) to targetCal.get(Calendar.MINUTE)
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
     * pre-populated with the exact hour, minute, and Flyff Mac NPC action label.
     */
    fun scheduleClockAlarm(
        context: Context,
        hoursFromNow: Double,
        label: String,
        skipUi: Boolean = false
    ): AlarmScheduleResult {
        val (hour, minute) = computeTargetAlarmTime(hoursFromNow)
        val formatted = formatTime24(hour, minute)

        val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, skipUi)
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
            formattedTime = formatted,
            label = label,
            hoursFromNow = hoursFromNow
        )
    }

    /**
     * Launches the user's Android Clock app countdown timer using [AlarmClock.ACTION_SET_TIMER].
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
