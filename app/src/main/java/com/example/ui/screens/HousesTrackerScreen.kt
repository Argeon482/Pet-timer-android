package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CalculationEngine
import com.example.data.HouseComputedStatus
import com.example.data.MacGradeTimerStatus
import com.example.data.PetTier
import com.example.data.SlotComputedStatus
import com.example.ui.HouseFilter
import com.example.ui.PetEmpireUiState
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningContainer
import com.example.ui.theme.CrimsonBurnAlert
import com.example.ui.theme.CrimsonBurnContainer
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.FCoinCyan
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.PenyaGold
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HousesTrackerScreen(
    uiState: PetEmpireUiState,
    onSelectFilter: (HouseFilter) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleSlotRunning: (Int, Int) -> Unit,
    onResetSlotTimer: (Int, Int) -> Unit,
    onAdjustSlotHours: (Int, Int, Double, Boolean?) -> Unit,
    onSetPipelineSlotTier: (Int, Int, PetTier) -> Unit,
    onRenewMacGrade: (Int, PetTier) -> Unit,
    onRenewAllMacGrades: (Int) -> Unit,
    onOpenHouseDetail: (Int?) -> Unit,
    onPauseAllFinished: () -> Unit,
    onSyncAllHourZero: () -> Unit,
    onSetAllRunning: (Boolean) -> Unit,
    onScheduleClockAlarm: (Double, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("houses_tracker_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header & Bulk Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "40-House & Manual Mac NPC Tracker",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = "Manual Start/Pause/Reset • Wasted overflow Penya protection",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = PenyaGold.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.border(1.dp, PenyaGold.copy(alpha = 0.4f), RoundedCornerShape(50))
                        ) {
                            Text(
                                text = "${uiState.filteredHouses.size}/${uiState.activeHouses.size} Houses",
                                style = MaterialTheme.typography.labelLarge,
                                color = PenyaGold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Search Bar
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search House # (e.g. 37) or label...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search Houses")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("house_search_input")
                    )

                    // Filter Chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val warningsCount = uiState.activeHouses.count { it.hasOverflowWarning || it.hasExpiryWarning }
                        FilterChip(
                            selected = uiState.selectedFilter == HouseFilter.ALL,
                            onClick = { onSelectFilter(HouseFilter.ALL) },
                            label = { Text("All (${uiState.activeHouses.size})") },
                            modifier = Modifier.testTag("filter_chip_all")
                        )
                        FilterChip(
                            selected = uiState.selectedFilter == HouseFilter.WARNINGS_ONLY,
                            onClick = { onSelectFilter(HouseFilter.WARNINGS_ONLY) },
                            label = { Text("Warnings ($warningsCount)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = "Warnings",
                                    tint = if (warningsCount > 0) CrimsonBurnAlert else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonBurnContainer,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_chip_warnings")
                        )
                        FilterChip(
                            selected = uiState.selectedFilter == HouseFilter.RUNNING,
                            onClick = { onSelectFilter(HouseFilter.RUNNING) },
                            label = { Text("Running") },
                            modifier = Modifier.testTag("filter_chip_running")
                        )
                        FilterChip(
                            selected = uiState.selectedFilter == HouseFilter.PAUSED,
                            onClick = { onSelectFilter(HouseFilter.PAUSED) },
                            label = { Text("All Paused") },
                            modifier = Modifier.testTag("filter_chip_paused")
                        )
                    }

                    // Bulk Action Row
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onPauseAllFinished,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonBurnAlert,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("bulk_pause_finished_button")
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause Finished", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop Overflow Burn", style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = onSyncAllHourZero,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("bulk_sync_hour_zero_button")
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Sync Hour 0", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync All to Hour 0", style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = { onSetAllRunning(false) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("bulk_pause_all_button")
                        ) {
                            Text("Pause All", style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = { onSetAllRunning(true) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("bulk_resume_all_button")
                        ) {
                            Text("Resume All", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // Empty filter state
        if (uiState.filteredHouses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "No matching houses",
                            tint = PenyaGold,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No houses match the current filter",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        TextButton(onClick = {
                            onSelectFilter(HouseFilter.ALL)
                            onSearchQueryChange("")
                        }) {
                            Text("Show All Active Houses")
                        }
                    }
                }
            }
        }

        // House Cards List (#1 to #40+)
        items(
            items = uiState.filteredHouses,
            key = { it.entity.houseNumber }
        ) { houseStatus ->
            HouseTrackerCard(
                houseStatus = houseStatus,
                onToggleSlot = { slotIdx ->
                    onToggleSlotRunning(houseStatus.entity.houseNumber, slotIdx)
                },
                onResetSlot = { slotIdx ->
                    onResetSlotTimer(houseStatus.entity.houseNumber, slotIdx)
                },
                onAdvanceTier = { slotIdx, nextTier ->
                    onSetPipelineSlotTier(houseStatus.entity.houseNumber, slotIdx, nextTier)
                },
                onScheduleAlarm = { hours, label ->
                    onScheduleClockAlarm(hours, label)
                },
                onOpenDetail = {
                    onOpenHouseDetail(houseStatus.entity.houseNumber)
                }
            )
        }
    }

    // Detailed House Inspector & Manual Simulator Modal
    val detailHouse = uiState.selectedHouseDetail
    if (detailHouse != null) {
        HouseDetailDialog(
            houseStatus = detailHouse,
            onDismiss = { onOpenHouseDetail(null) },
            onToggleSlot = { slotIdx ->
                onToggleSlotRunning(detailHouse.entity.houseNumber, slotIdx)
            },
            onResetSlot = { slotIdx ->
                onResetSlotTimer(detailHouse.entity.houseNumber, slotIdx)
            },
            onAdjustSlotHours = { slotIdx, newHours, keepRunning ->
                onAdjustSlotHours(detailHouse.entity.houseNumber, slotIdx, newHours, keepRunning)
            },
            onSetTier = { slotIdx, tier ->
                onSetPipelineSlotTier(detailHouse.entity.houseNumber, slotIdx, tier)
            },
            onRenewMacGrade = { grade ->
                onRenewMacGrade(detailHouse.entity.houseNumber, grade)
            },
            onRenewAllMacs = {
                onRenewAllMacGrades(detailHouse.entity.houseNumber)
            },
            onScheduleAlarm = onScheduleClockAlarm
        )
    }
}

@Composable
private fun HouseTrackerCard(
    houseStatus: HouseComputedStatus,
    onToggleSlot: (Int) -> Unit,
    onResetSlot: (Int) -> Unit,
    onAdvanceTier: (Int, PetTier) -> Unit,
    onScheduleAlarm: (Double, String) -> Unit,
    onOpenDetail: () -> Unit
) {
    val houseNum = houseStatus.entity.houseNumber
    val borderColor = when {
        houseStatus.hasOverflowWarning -> CrimsonBurnAlert
        houseStatus.hasExpiryWarning -> AmberWarning
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onOpenDetail() }
            .testTag("house_card_$houseNum"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = PenyaGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "#$houseNum",
                            style = MaterialTheme.typography.titleMedium,
                            color = PenyaGold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = houseStatus.entity.customLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = houseStatus.nextActionDescription,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (houseStatus.hasOverflowWarning) CrimsonBurnAlert else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (houseStatus.hasOverflowWarning) {
                        Surface(
                            color = CrimsonBurnContainer,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.border(1.dp, CrimsonBurnAlert, RoundedCornerShape(50))
                        ) {
                            Text(
                                text = "LOST -${CalculationEngine.formatPenyaCompact(houseStatus.totalWastedPenya).removeSuffix(" Penya")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CrimsonBurnAlert,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    } else if (houseStatus.hasExpiryWarning) {
                        Surface(
                            color = AmberWarningContainer,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.border(1.dp, AmberWarning, RoundedCornerShape(50))
                        ) {
                            Text(
                                text = "MAC EXPIRING",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberWarning,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    IconButton(
                        onClick = onOpenDetail,
                        modifier = Modifier.testTag("house_${houseNum}_tune_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Inspect & Adjust Timers",
                            tint = FCoinCyan
                        )
                    }
                }
            }

            // Slot 1: S-Tier Bake (250h Mac Grade A)
            SlotControlRow(
                houseNumber = houseNum,
                slot = houseStatus.slot1,
                accentColor = PenyaGold,
                onToggle = { onToggleSlot(1) },
                onReset = { onResetSlot(1) },
                onAdvanceTier = null,
                onScheduleAlarm = {
                    val rem = houseStatus.slot1.remainingHours.coerceAtLeast(0.05)
                    onScheduleAlarm(rem, "Flyff House #$houseNum • Pause Slot 1 S-Tier Pet (250h Complete)")
                }
            )

            // Slot 2: A-Tier Pipeline 1
            SlotControlRow(
                houseNumber = houseNum,
                slot = houseStatus.slot2,
                accentColor = FCoinCyan,
                onToggle = { onToggleSlot(2) },
                onReset = { onResetSlot(2) },
                onAdvanceTier = if (houseStatus.slot2.tier != PetTier.A) {
                    { onAdvanceTier(2, houseStatus.slot2.tier.nextTier()) }
                } else null,
                onScheduleAlarm = {
                    val rem = houseStatus.slot2.remainingHours.coerceAtLeast(0.05)
                    onScheduleAlarm(rem, "Flyff House #$houseNum • Slot 2 Grade ${houseStatus.slot2.tier.code} Ready")
                }
            )

            // Slot 3: A-Tier Pipeline 2
            SlotControlRow(
                houseNumber = houseNum,
                slot = houseStatus.slot3,
                accentColor = EmeraldProfit,
                onToggle = { onToggleSlot(3) },
                onReset = { onResetSlot(3) },
                onAdvanceTier = if (houseStatus.slot3.tier != PetTier.A) {
                    { onAdvanceTier(3, houseStatus.slot3.tier.nextTier()) }
                } else null,
                onScheduleAlarm = {
                    val rem = houseStatus.slot3.remainingHours.coerceAtLeast(0.05)
                    onScheduleAlarm(rem, "Flyff House #$houseNum • Slot 3 Grade ${houseStatus.slot3.tier.code} Ready")
                }
            )

            // 6-Grade Physical Mac NPC 360-Hour Timer Strip
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianBackground.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mac NPC 360h Physical Timers (Grades F–A)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap card to renew / edit hours",
                        style = MaterialTheme.typography.labelSmall,
                        color = FCoinCyan
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    houseStatus.macTimers.forEach { mac ->
                        MacMiniPill(
                            mac = mac,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotControlRow(
    houseNumber: Int,
    slot: SlotComputedStatus,
    accentColor: Color,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    onAdvanceTier: (() -> Unit)?,
    onScheduleAlarm: () -> Unit
) {
    val isOverflowing = slot.overflowHours > 0.01 || (slot.isFinished && slot.isRunning)
    val rowBg = if (isOverflowing) {
        CrimsonBurnContainer.copy(alpha = 0.5f)
    } else {
        ObsidianElevated.copy(alpha = 0.65f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg, RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = slot.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = accentColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (slot.slotIndex == 1) "A → S" else "Tier ${slot.tier.code}",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                val statusLine = if (isOverflowing) {
                    String.format(
                        Locale.US,
                        "OVERFLOW +%.1fh burned! (-%s)",
                        slot.overflowHours,
                        CalculationEngine.formatPenyaCompact(slot.wastedPenya)
                    )
                } else if (slot.isFinished) {
                    "100% Complete • Paused safely"
                } else {
                    String.format(
                        Locale.US,
                        "%.1f / %.0fh (%d%%) • %s left",
                        slot.elapsedHours,
                        slot.requiredHours,
                        (slot.progressFraction * 100).toInt(),
                        CalculationEngine.formatHours(slot.remainingHours)
                    )
                }

                Text(
                    text = statusLine,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOverflowing) CrimsonBurnAlert else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Start / Pause Manual Toggle Button
                Button(
                    onClick = onToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOverflowing) {
                            CrimsonBurnAlert
                        } else if (slot.isRunning) {
                            AmberWarning
                        } else {
                            EmeraldProfit
                        },
                        contentColor = ObsidianBackground
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("house_${houseNumber}_slot_${slot.slotIndex}_toggle")
                ) {
                    Icon(
                        imageVector = if (slot.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (slot.isRunning) "Pause Slot" else "Start Slot",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (slot.isRunning) "Pause" else "Start",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (onAdvanceTier != null) {
                    IconButton(
                        onClick = onAdvanceTier,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("house_${houseNumber}_slot_${slot.slotIndex}_next_tier")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Advance Pet Tier",
                            tint = FCoinCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onReset,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("house_${houseNumber}_slot_${slot.slotIndex}_reset")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Slot Timer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onScheduleAlarm,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("house_${houseNumber}_slot_${slot.slotIndex}_alarm")
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessAlarm,
                        contentDescription = "Set Clock App Alarm",
                        tint = PenyaGold,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        LinearProgressIndicator(
            progress = { slot.progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = if (isOverflowing) CrimsonBurnAlert else accentColor,
            trackColor = ObsidianBackground
        )
    }
}

@Composable
private fun MacMiniPill(
    mac: MacGradeTimerStatus,
    modifier: Modifier = Modifier
) {
    val pillColor = when {
        mac.isExpired -> CrimsonBurnAlert
        mac.isExpiringSoon -> AmberWarning
        else -> FCoinCyan
    }
    Column(
        modifier = modifier
            .background(ObsidianElevated, RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = if (mac.isExpiringSoon || mac.isExpired) pillColor else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 4.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = "Mac ${mac.grade.code}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${mac.hoursRemaining.toInt()}h",
            style = MaterialTheme.typography.labelSmall,
            color = pillColor
        )
        LinearProgressIndicator(
            progress = { mac.usageFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(50)),
            color = pillColor,
            trackColor = ObsidianBackground
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HouseDetailDialog(
    houseStatus: HouseComputedStatus,
    onDismiss: () -> Unit,
    onToggleSlot: (Int) -> Unit,
    onResetSlot: (Int) -> Unit,
    onAdjustSlotHours: (Int, Double, Boolean?) -> Unit,
    onSetTier: (Int, PetTier) -> Unit,
    onRenewMacGrade: (PetTier) -> Unit,
    onRenewAllMacs: () -> Unit,
    onScheduleAlarm: (Double, String) -> Unit
) {
    val houseNum = houseStatus.entity.houseNumber
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "House #$houseNum Timer & Mac Control",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Slot 1 Simulator & Controls
                item {
                    DetailSlotControlBlock(
                        slot = houseStatus.slot1,
                        onToggle = { onToggleSlot(1) },
                        onReset = { onResetSlot(1) },
                        onAddHours = { delta ->
                            onAdjustSlotHours(1, houseStatus.slot1.elapsedHours + delta, null)
                        },
                        onSimulateOverflow = {
                            onAdjustSlotHours(1, houseStatus.slot1.requiredHours + 8.0, true)
                        },
                        onScheduleAlarm = {
                            onScheduleAlarm(
                                houseStatus.slot1.remainingHours.coerceAtLeast(0.05),
                                "Flyff House #$houseNum • Slot 1 S-Tier Bake Complete"
                            )
                        }
                    )
                }

                // Slot 2 Simulator & Tier Selector
                item {
                    DetailSlotControlBlock(
                        slot = houseStatus.slot2,
                        onToggle = { onToggleSlot(2) },
                        onReset = { onResetSlot(2) },
                        onAddHours = { delta ->
                            onAdjustSlotHours(2, houseStatus.slot2.elapsedHours + delta, null)
                        },
                        onSimulateOverflow = {
                            onAdjustSlotHours(2, houseStatus.slot2.requiredHours + 5.0, true)
                        },
                        onScheduleAlarm = {
                            onScheduleAlarm(
                                houseStatus.slot2.remainingHours.coerceAtLeast(0.05),
                                "Flyff House #$houseNum • Slot 2 Tier ${houseStatus.slot2.tier.code} Complete"
                            )
                        },
                        onSelectTier = { tier -> onSetTier(2, tier) }
                    )
                }

                // Slot 3 Simulator & Tier Selector
                item {
                    DetailSlotControlBlock(
                        slot = houseStatus.slot3,
                        onToggle = { onToggleSlot(3) },
                        onReset = { onResetSlot(3) },
                        onAddHours = { delta ->
                            onAdjustSlotHours(3, houseStatus.slot3.elapsedHours + delta, null)
                        },
                        onSimulateOverflow = {
                            onAdjustSlotHours(3, houseStatus.slot3.requiredHours + 5.0, true)
                        },
                        onScheduleAlarm = {
                            onScheduleAlarm(
                                houseStatus.slot3.remainingHours.coerceAtLeast(0.05),
                                "Flyff House #$houseNum • Slot 3 Tier ${houseStatus.slot3.tier.code} Complete"
                            )
                        },
                        onSelectTier = { tier -> onSetTier(3, tier) }
                    )
                }

                // Mac NPC 360-Hour Renewal Panel
                item {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mac NPC 360h Cash Shop Timers",
                            style = MaterialTheme.typography.titleSmall,
                            color = PenyaGold
                        )
                        TextButton(
                            onClick = onRenewAllMacs,
                            modifier = Modifier.testTag("renew_all_macs_button")
                        ) {
                            Icon(Icons.Default.Autorenew, contentDescription = "Renew All", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Renew All 6")
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        houseStatus.macTimers.forEach { mac ->
                            OutlinedButton(
                                onClick = { onRenewMacGrade(mac.grade) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Mac ${mac.grade.code}: ${mac.hoursRemaining.toInt()}h left (Reset)",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_house_detail_button")
            ) {
                Text("Done")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailSlotControlBlock(
    slot: SlotComputedStatus,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    onAddHours: (Double) -> Unit,
    onSimulateOverflow: () -> Unit,
    onScheduleAlarm: () -> Unit,
    onSelectTier: ((PetTier) -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ObsidianElevated, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = slot.title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = String.format(Locale.US, "%.1f / %.0fh", slot.elapsedHours, slot.requiredHours),
                style = MaterialTheme.typography.labelLarge,
                color = PenyaGold
            )
        }

        if (onSelectTier != null) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PetTier.entries.forEach { tier ->
                    FilterChip(
                        selected = slot.tier == tier,
                        onClick = { onSelectTier(tier) },
                        label = { Text(tier.code) }
                    )
                }
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = onToggle,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(if (slot.isRunning) "Pause" else "Resume")
            }
            OutlinedButton(
                onClick = { onAddHours(10.0) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("+10h")
            }
            OutlinedButton(
                onClick = onSimulateOverflow,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("simulate_overflow_slot_${slot.slotIndex}")
            ) {
                Text("Test Overflow")
            }
            OutlinedButton(
                onClick = onReset,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Reset 0h")
            }
            OutlinedButton(
                onClick = onScheduleAlarm,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.AccessAlarm, contentDescription = "Alarm", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clock Alarm")
            }
        }
    }
}
