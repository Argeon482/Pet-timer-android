package com.example.ui.components

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Schedule
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
import com.example.ui.theme.CrimsonBurnAlert
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.FCoinCyan
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.PenyaGold
import com.example.util.AlarmActionItem
import com.example.util.AlarmHelper

/**
 * Sequential Alarm Scheduler Dialog.
 *
 * Solves Android's single-intent limitation: Android's Clock app only processes
 * one ACTION_SET_ALARM intent at a time. This dialog allows setting consolidated
 * alarms sequentially with one tap per alarm or stepped 'Set Next Alarm' progression.
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

    fun triggerScheduleItem(item: AlarmActionItem) {
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
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isOnlyChanged) "Set Changed Alarms (1-by-1)" else "Set Empire Alarms (1-by-1)",
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
                                text = "Sequential Queue ($completedCount / $totalCount set)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Android Clock accepts 1 alarm at a time. Tap 'Set Next' to pre-fill each alarm in your Clock app sequentially.",
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
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                        val isScheduled = item.id in scheduledIds
                        val isCurrentNext = index == nextItemIndex
                        val (hour, minute) = AlarmHelper.computeTargetAlarmTime(item.hoursFromNow)
                        val formattedTime = AlarmHelper.formatTime24(hour, minute)

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrentNext) ObsidianBackground.copy(alpha = 0.9f)
                                else ObsidianBackground.copy(alpha = 0.5f)
                            ),
                            border = if (isCurrentNext) {
                                androidx.compose.foundation.BorderStroke(1.5.dp, PenyaGold)
                            } else null,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = formattedTime,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isScheduled) EmeraldProfit else PenyaGold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(+${CalculationEngine.formatHours(item.hoursFromNow)})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                    if (item.clusterCount > 1) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Groups,
                                                contentDescription = null,
                                                tint = FCoinCyan,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${item.clusterCount} houses consolidated into earliest timer",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = FCoinCyan
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                if (isScheduled) {
                                    OutlinedButton(
                                        onClick = { triggerScheduleItem(item) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Set",
                                            tint = EmeraldProfit,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Set ✓", style = MaterialTheme.typography.labelSmall, color = EmeraldProfit)
                                    }
                                } else {
                                    Button(
                                        onClick = { triggerScheduleItem(item) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isCurrentNext) PenyaGold else Color.DarkGray,
                                            contentColor = if (isCurrentNext) ObsidianBackground else Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(
                                            text = if (isCurrentNext) "Set Now" else "Set",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
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
                    Button(
                        onClick = { triggerScheduleItem(nextItem) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PenyaGold,
                            contentColor = ObsidianBackground
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("set_next_alarm_queue_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessAlarm,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Set Alarm ${nextItemIndex + 1} of $totalCount",
                            fontWeight = FontWeight.Bold
                        )
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
                            text = "All $totalCount Alarms Scheduled • Done",
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
