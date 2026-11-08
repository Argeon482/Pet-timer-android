package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CalculationEngine
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonBurnAlert
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.FCoinCyan
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.PenyaGold
import com.example.util.AlarmActionItem
import com.example.util.AlarmHelper

/**
 * Sequential Alarm & Calendar Reminder Scheduler Dialog.
 *
 * Displays exact target calendar dates (e.g. Tuesday Nov 10, Wednesday Nov 11, Thursday Nov 19).
 * Pre-populates the exact day-of-week for alarms within 7 days, and offers Calendar reminders
 * for long-duration cycles (such as 250h S-tier bake) which exceed the Android Clock 7-day limit.
 */
@Composable
fun AlarmBatchQueueDialog(
    items: List<AlarmActionItem>,
    isOnlyChanged: Boolean,
    onDismiss: () -> Unit,
    onSingleAlarmScheduled: (AlarmActionItem) -> Unit,
    onAllAlarmsFinished: () -> Unit
) {
    val context = LocalContext.current
    val scheduledIds = remember { mutableStateListOf<String>() }

    val totalCount = items.size
    val completedCount = scheduledIds.size
    val nextItemIndex = items.indexOfFirst { it.id !in scheduledIds }
    val isAllCompleted = totalCount > 0 && completedCount >= totalCount

    fun triggerScheduleClock(item: AlarmActionItem) {
        AlarmHelper.scheduleClockAlarm(
            context = context,
            hoursFromNow = item.hoursFromNow,
            label = item.label,
            skipUi = false
        )
        if (item.id !in scheduledIds) {
            scheduledIds.add(item.id)
        }
        onSingleAlarmScheduled(item)
    }

    fun triggerScheduleCalendar(item: AlarmActionItem) {
        val targetCal = AlarmHelper.computeTargetCalendar(item.hoursFromNow)
        val formattedDate = AlarmHelper.formatTargetDateShort(targetCal)
        AlarmHelper.scheduleCalendarReminder(
            context = context,
            hoursFromNow = item.hoursFromNow,
            label = "[$formattedDate] ${item.label}"
        )
        if (item.id !in scheduledIds) {
            scheduledIds.add(item.id)
        }
        onSingleAlarmScheduled(item)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        modifier = Modifier.testTag("alarm_batch_queue_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessAlarm,
                    contentDescription = null,
                    tint = PenyaGold,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isOnlyChanged) "Set Changed Timers (1-by-1)" else "Set Empire Timers (1-by-1)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "$totalCount consolidated timers • Same class within 1h merged",
                        style = MaterialTheme.typography.labelSmall,
                        color = PenyaGold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Info Banner
                Surface(
                    color = ObsidianBackground,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = FCoinCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Target Dates & Alarms ($completedCount / $totalCount scheduled)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Timers are scheduled with their target day of week. For long cycles > 7 days (like 250h S-tier), use Calendar Reminder for the exact multi-day alert.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { if (totalCount > 0) completedCount.toFloat() / totalCount else 1f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = if (isAllCompleted) EmeraldProfit else PenyaGold,
                            trackColor = Color.DarkGray
                        )
                    }
                }

                // List of Consolidated Alarms
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                        val isScheduled = item.id in scheduledIds
                        val isCurrentNext = index == nextItemIndex
                        val targetCal = AlarmHelper.computeTargetCalendar(item.hoursFromNow)
                        val formattedDateTime = AlarmHelper.formatTargetDateTime(targetCal)
                        val relativeDuration = AlarmHelper.formatRelativeDuration(item.hoursFromNow)
                        val isBeyond7Days = item.hoursFromNow > 168.0

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrentNext) ObsidianBackground.copy(alpha = 0.95f)
                                else ObsidianBackground.copy(alpha = 0.6f)
                            ),
                            border = if (isCurrentNext) {
                                BorderStroke(1.5.dp, PenyaGold)
                            } else null,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = formattedDateTime,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isScheduled) EmeraldProfit else PenyaGold
                                        )
                                        Text(
                                            text = "(+${CalculationEngine.formatHours(item.hoursFromNow)} • in $relativeDuration)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (isScheduled) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Scheduled",
                                                tint = EmeraldProfit,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Scheduled ✓",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = EmeraldProfit,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )

                                if (item.clusterCount > 1) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Groups,
                                            contentDescription = null,
                                            tint = FCoinCyan,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${item.clusterCount} houses within 1h consolidated to this earliest timer",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FCoinCyan
                                        )
                                    }
                                }

                                if (isBeyond7Days) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        color = AmberWarning.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = AmberWarning,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Exceeds Clock 7-day limit ($relativeDuration away). Use Calendar Reminder for exact date alert.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AmberWarning
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { triggerScheduleClock(item) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isCurrentNext && !isBeyond7Days) PenyaGold else Color.DarkGray,
                                            contentColor = if (isCurrentNext && !isBeyond7Days) ObsidianBackground else Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccessAlarm,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Clock Alarm",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { triggerScheduleCalendar(item) },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (isBeyond7Days) PenyaGold else FCoinCyan
                                        ),
                                        border = if (isBeyond7Days) BorderStroke(1.dp, PenyaGold) else null,
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Calendar",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isBeyond7Days) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Step-through Main Button
                if (!isAllCompleted && nextItemIndex in items.indices) {
                    val nextItem = items[nextItemIndex]
                    val isBeyond = nextItem.hoursFromNow > 168.0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { triggerScheduleClock(nextItem) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isBeyond) PenyaGold else Color.DarkGray,
                                contentColor = if (!isBeyond) ObsidianBackground else Color.White
                            ),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(46.dp)
                                .testTag("set_next_alarm_queue_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessAlarm,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Set Clock (${nextItemIndex + 1}/$totalCount)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Button(
                            onClick = { triggerScheduleCalendar(nextItem) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isBeyond) PenyaGold else FCoinCyan,
                                contentColor = ObsidianBackground
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("set_next_calendar_queue_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Calendar",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else if (isAllCompleted) {
                    Button(
                        onClick = {
                            onAllAlarmsFinished()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldProfit,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("all_alarms_finished_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "All $totalCount Timers Scheduled • Done",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isAllCompleted || scheduledIds.isNotEmpty()) {
                        onAllAlarmsFinished()
                    }
                    onDismiss()
                }
            ) {
                Text(if (isAllCompleted) "Close" else "Done", color = PenyaGold)
            }
        }
    )
}
