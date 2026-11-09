package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NotificationsActive
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    onSetSlotRemainingHours: (Int, Int, Double) -> Unit,
    onSetMacRemainingHours: (Int, PetTier, Double) -> Unit,
    onSetPipelineSlotTier: (Int, Int, PetTier) -> Unit,
    onRenewMacGrade: (Int, PetTier) -> Unit,
    onRenewAllMacGrades: (Int) -> Unit,
    onOpenHouseDetail: (Int?) -> Unit,
    onPauseAllFinished: () -> Unit,
    onSyncAllHourZero: () -> Unit,
    onSetAllRunning: (Boolean) -> Unit,
    onBatchUpdateHouses: (Set<Int>, Double?, Boolean?, PetTier?, Double?, Boolean?, PetTier?, Double?, Boolean?, Double?) -> Unit,
    onScheduleClockAlarm: (Double, String) -> Unit,
    onScheduleAllEmpireAlarms: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Multi-selection state for house cards
    var selectedHouseNumbers by remember { mutableStateOf(setOf<Int>()) }
    var showBatchSetupDialog by remember { mutableStateOf(false) }

    // Confirmation dialog states to prevent accidents
    var showConfirmSyncZeroDialog by remember { mutableStateOf(false) }
    var showConfirmPauseAllDialog by remember { mutableStateOf(false) }
    var showConfirmResumeAllDialog by remember { mutableStateOf(false) }
    var showConfirmPauseFinishedDialog by remember { mutableStateOf(false) }

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
                                text = "40-House & Mac NPC Tracker",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = "Multi-select cards • Direct hours input • 1-tap alarms",
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

                    // CLOCK ALARMS PANEL (CHANGED TIMERS & ALL TIMERS)
                    Surface(
                        color = PenyaGold.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PenyaGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Clock Alarms",
                                        tint = PenyaGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Sync Clock Alarms",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = PenyaGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (uiState.pendingChangedAlarmsCount > 0) {
                                    Surface(
                                        color = FCoinCyan.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.border(1.dp, FCoinCyan, RoundedCornerShape(50))
                                    ) {
                                        Text(
                                            text = "${uiState.pendingChangedAlarmsCount} Timers Edited",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FCoinCyan,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Create new alarms in your Android Clock app. Choose to set alarms only for houses/slots you recently edited, or set alarms for all active houses.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onScheduleAllEmpireAlarms(true) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (uiState.pendingChangedAlarmsCount > 0) FCoinCyan else PenyaGold,
                                        contentColor = ObsidianBackground
                                    ),
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("set_changed_alarms_button")
                                ) {
                                    Icon(Icons.Default.AccessAlarm, contentDescription = "Set Alarms", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (uiState.pendingChangedAlarmsCount > 0) {
                                            "Set Alarms (Changed: ${uiState.pendingChangedAlarmsCount})"
                                        } else {
                                            "Set Alarms (Changed)"
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = { onScheduleAllEmpireAlarms(false) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("set_all_active_alarms_button")
                                ) {
                                    Text("Set All Alarms")
                                }
                            }
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

                    // Bulk Action Row with Confirmation Triggers
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showConfirmPauseFinishedDialog = true },
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
                            onClick = { showConfirmSyncZeroDialog = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("bulk_sync_hour_zero_button")
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Sync Hour 0", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync All to Hour 0", style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = { showConfirmPauseAllDialog = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("bulk_pause_all_button")
                        ) {
                            Text("Pause All", style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = { showConfirmResumeAllDialog = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("bulk_resume_all_button")
                        ) {
                            Text("Resume All", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // Multi-Selection Active Action Bar
        if (selectedHouseNumbers.isNotEmpty()) {
            item {
                Surface(
                    color = ObsidianCard,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, PenyaGold, RoundedCornerShape(16.dp))
                        .testTag("multi_select_action_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${selectedHouseNumbers.size} Houses Selected",
                                style = MaterialTheme.typography.titleMedium,
                                color = PenyaGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Setup once, apply to all selected",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { showBatchSetupDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PenyaGold,
                                    contentColor = ObsidianBackground
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("open_batch_setup_dialog_button")
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = "Batch Edit", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Batch Edit (${selectedHouseNumbers.size})", fontWeight = FontWeight.Bold)
                            }

                            val allFilteredIds = uiState.filteredHouses.map { it.entity.houseNumber }.toSet()
                            val allSelected = allFilteredIds.isNotEmpty() && selectedHouseNumbers.containsAll(allFilteredIds)

                            OutlinedButton(
                                onClick = {
                                    selectedHouseNumbers = if (allSelected) {
                                        emptySet()
                                    } else {
                                        selectedHouseNumbers + allFilteredIds
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(if (allSelected) "Deselect" else "Select All")
                            }

                            IconButton(
                                onClick = { selectedHouseNumbers = emptySet() },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Selection", tint = Color.White)
                            }
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
            val houseNum = houseStatus.entity.houseNumber
            val isSelected = houseNum in selectedHouseNumbers

            HouseTrackerCard(
                houseStatus = houseStatus,
                isSelected = isSelected,
                onToggleSelect = {
                    selectedHouseNumbers = if (isSelected) {
                        selectedHouseNumbers - houseNum
                    } else {
                        selectedHouseNumbers + houseNum
                    }
                },
                onToggleSlot = { slotIdx ->
                    onToggleSlotRunning(houseNum, slotIdx)
                },
                onResetSlot = { slotIdx ->
                    onResetSlotTimer(houseNum, slotIdx)
                },
                onAdvanceTier = { slotIdx, nextTier ->
                    onSetPipelineSlotTier(houseNum, slotIdx, nextTier)
                },
                onScheduleAlarm = { hours, label ->
                    onScheduleClockAlarm(hours, label)
                },
                onOpenDetail = {
                    onOpenHouseDetail(houseNum)
                }
            )
        }
    }

    // Confirmation Dialog: Sync All to Hour Zero
    if (showConfirmSyncZeroDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmSyncZeroDialog = false },
            containerColor = ObsidianCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = AmberWarning)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Sync to Hour 0", color = Color.White)
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to synchronize all houses to Hour 0? This resets all pet slots to the default A, B, C setup, restarts all timers, and resets Mac NPC replacement timers to 360 hours.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmSyncZeroDialog = false
                        onSyncAllHourZero()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PenyaGold, contentColor = ObsidianBackground),
                    modifier = Modifier.testTag("confirm_sync_zero_button")
                ) {
                    Text("Confirm Sync", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmSyncZeroDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation Dialog: Pause All
    if (showConfirmPauseAllDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmPauseAllDialog = false },
            containerColor = ObsidianCard,
            title = { Text("Pause All Houses?", color = Color.White) },
            text = { Text("This will pause pet progression across all active houses.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmPauseAllDialog = false
                        onSetAllRunning(false)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberWarning, contentColor = ObsidianBackground)
                ) {
                    Text("Confirm Pause", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmPauseAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation Dialog: Resume All
    if (showConfirmResumeAllDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmResumeAllDialog = false },
            containerColor = ObsidianCard,
            title = { Text("Resume All Houses?", color = Color.White) },
            text = { Text("This will resume timer progression across all active houses.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmResumeAllDialog = false
                        onSetAllRunning(true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldProfit, contentColor = ObsidianBackground)
                ) {
                    Text("Confirm Resume", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmResumeAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation Dialog: Stop Overflow Burn
    if (showConfirmPauseFinishedDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmPauseFinishedDialog = false },
            containerColor = ObsidianCard,
            title = { Text("Stop Overflow Burn?", color = Color.White) },
            text = { Text("This will immediately pause any 100% finished pets that are still running to prevent wasting Penya.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmPauseFinishedDialog = false
                        onPauseAllFinished()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonBurnAlert, contentColor = Color.White)
                ) {
                    Text("Confirm Pause Finished", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmPauseFinishedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Batch House Setup Dialog (Multi-Select)
    if (showBatchSetupDialog && selectedHouseNumbers.isNotEmpty()) {
        BatchHouseSetupDialog(
            selectedHouseNumbers = selectedHouseNumbers,
            macMaxHours = uiState.settings.macMaxActiveHours,
            onDismiss = { showBatchSetupDialog = false },
            onApply = { s1Hours, s1Run, s2Tier, s2Hours, s2Run, s3Tier, s3Hours, s3Run, macHours ->
                onBatchUpdateHouses(
                    selectedHouseNumbers,
                    s1Hours,
                    s1Run,
                    s2Tier,
                    s2Hours,
                    s2Run,
                    s3Tier,
                    s3Hours,
                    s3Run,
                    macHours
                )
                showBatchSetupDialog = false
            }
        )
    }

    // Detailed Single House Inspector Dialog
    val detailHouse = uiState.selectedHouseDetail
    if (detailHouse != null) {
        HouseDetailDialog(
            houseStatus = detailHouse,
            macMaxHours = uiState.settings.macMaxActiveHours,
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
            onSetSlotRemainingHours = { slotIdx, remHours ->
                onSetSlotRemainingHours(detailHouse.entity.houseNumber, slotIdx, remHours)
            },
            onSetMacRemainingHours = { grade, remHours ->
                onSetMacRemainingHours(detailHouse.entity.houseNumber, grade, remHours)
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
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onToggleSlot: (Int) -> Unit,
    onResetSlot: (Int) -> Unit,
    onAdvanceTier: (Int, PetTier) -> Unit,
    onScheduleAlarm: (Double, String) -> Unit,
    onOpenDetail: () -> Unit
) {
    val houseNum = houseStatus.entity.houseNumber
    val borderColor = when {
        isSelected -> PenyaGold
        houseStatus.hasOverflowWarning -> CrimsonBurnAlert
        houseStatus.hasExpiryWarning -> AmberWarning
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isSelected) 2.dp else 1.2.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onOpenDetail() }
            .testTag("house_card_$houseNum"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ObsidianCard.copy(alpha = 0.95f) else ObsidianCard
        )
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
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PenyaGold,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            checkmarkColor = ObsidianBackground
                        ),
                        modifier = Modifier.testTag("house_${houseNum}_checkbox")
                    )

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
                    onScheduleAlarm(rem, "House #$houseNum: Slot 1 S-Tier Pet (250h Complete)")
                }
            )

            // Slot 2: Pipeline (defaults B, cannot be A or equal to Slot 3)
            val canAdvanceS2 = houseStatus.slot2.tier != PetTier.A &&
                houseStatus.slot2.tier.nextTier() != PetTier.A &&
                houseStatus.slot2.tier.nextTier() != houseStatus.slot3.tier

            SlotControlRow(
                houseNumber = houseNum,
                slot = houseStatus.slot2,
                accentColor = FCoinCyan,
                onToggle = { onToggleSlot(2) },
                onReset = { onResetSlot(2) },
                onAdvanceTier = if (canAdvanceS2) {
                    { onAdvanceTier(2, houseStatus.slot2.tier.nextTier()) }
                } else null,
                onScheduleAlarm = {
                    val rem = houseStatus.slot2.remainingHours.coerceAtLeast(0.05)
                    onScheduleAlarm(rem, "House #$houseNum: Slot 2 Grade ${houseStatus.slot2.tier.code} Ready")
                }
            )

            // Slot 3: Pipeline (defaults C, cannot be A or equal to Slot 2)
            val canAdvanceS3 = houseStatus.slot3.tier != PetTier.A &&
                houseStatus.slot3.tier.nextTier() != PetTier.A &&
                houseStatus.slot3.tier.nextTier() != houseStatus.slot2.tier

            SlotControlRow(
                houseNumber = houseNum,
                slot = houseStatus.slot3,
                accentColor = EmeraldProfit,
                onToggle = { onToggleSlot(3) },
                onReset = { onResetSlot(3) },
                onAdvanceTier = if (canAdvanceS3) {
                    { onAdvanceTier(3, houseStatus.slot3.tier.nextTier()) }
                } else null,
                onScheduleAlarm = {
                    val rem = houseStatus.slot3.remainingHours.coerceAtLeast(0.05)
                    onScheduleAlarm(rem, "House #$houseNum: Slot 3 Grade ${houseStatus.slot3.tier.code} Ready")
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
                        text = "Mac NPC Physical Timers (Grades F–A • 360h Default)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap to set remaining hours",
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
        mac.isCurrentlyTraining -> EmeraldProfit
        else -> FCoinCyan
    }
    Column(
        modifier = modifier
            .background(ObsidianElevated, RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = if (mac.isExpiringSoon || mac.isExpired) pillColor else if (mac.isCurrentlyTraining) EmeraldProfit.copy(alpha = 0.6f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 4.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Mac ${mac.grade.code}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            if (mac.isCurrentlyTraining) {
                Spacer(modifier = Modifier.width(2.dp))
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(EmeraldProfit, RoundedCornerShape(50))
                )
            }
        }
        Text(
            text = String.format(Locale.US, "%.1fh", mac.hoursRemaining),
            style = MaterialTheme.typography.labelSmall,
            color = pillColor,
            fontWeight = if (mac.isCurrentlyTraining) FontWeight.Bold else FontWeight.Normal
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

/**
 * Detailed House Inspector Dialog
 * Sliders replaced with direct numeric input boxes + preset chips.
 * Pet tier selectors for slots 2 & 3 enforce no duplicates in the same house.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HouseDetailDialog(
    houseStatus: HouseComputedStatus,
    macMaxHours: Double,
    onDismiss: () -> Unit,
    onToggleSlot: (Int) -> Unit,
    onResetSlot: (Int) -> Unit,
    onAdjustSlotHours: (Int, Double, Boolean?) -> Unit,
    onSetSlotRemainingHours: (Int, Double) -> Unit,
    onSetMacRemainingHours: (PetTier, Double) -> Unit,
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
                    text = "House #$houseNum Fine Adjustments",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Slot 1: S-Tier Bake (Mac Grade A, 250h)
                item {
                    FineTuningSlotBlock(
                        slot = houseStatus.slot1,
                        houseNumber = houseNum,
                        onToggle = { onToggleSlot(1) },
                        onReset = { onResetSlot(1) },
                        onSetRemainingHours = { rem -> onSetSlotRemainingHours(1, rem) },
                        onAddHours = { delta ->
                            onAdjustSlotHours(1, houseStatus.slot1.elapsedHours + delta, null)
                        },
                        onScheduleAlarm = {
                            onScheduleAlarm(
                                houseStatus.slot1.remainingHours.coerceAtLeast(0.05),
                                "House #$houseNum: Slot 1 S-Tier Bake Complete"
                            )
                        },
                        disallowedTiers = setOf(PetTier.A, PetTier.B, PetTier.C, PetTier.D, PetTier.E, PetTier.F) // Fixed to A bake
                    )
                }

                // Slot 2: Pipeline Slot 2 (Cannot be A, cannot match Slot 3 tier)
                item {
                    FineTuningSlotBlock(
                        slot = houseStatus.slot2,
                        houseNumber = houseNum,
                        onToggle = { onToggleSlot(2) },
                        onReset = { onResetSlot(2) },
                        onSetRemainingHours = { rem -> onSetSlotRemainingHours(2, rem) },
                        onAddHours = { delta ->
                            onAdjustSlotHours(2, houseStatus.slot2.elapsedHours + delta, null)
                        },
                        onScheduleAlarm = {
                            onScheduleAlarm(
                                houseStatus.slot2.remainingHours.coerceAtLeast(0.05),
                                "House #$houseNum: Slot 2 Tier ${houseStatus.slot2.tier.code} Complete"
                            )
                        },
                        disallowedTiers = setOf(PetTier.A, houseStatus.slot3.tier),
                        disallowReason = "In Slot 3",
                        onSelectTier = { tier -> onSetTier(2, tier) }
                    )
                }

                // Slot 3: Pipeline Slot 3 (Cannot be A, cannot match Slot 2 tier)
                item {
                    FineTuningSlotBlock(
                        slot = houseStatus.slot3,
                        houseNumber = houseNum,
                        onToggle = { onToggleSlot(3) },
                        onReset = { onResetSlot(3) },
                        onSetRemainingHours = { rem -> onSetSlotRemainingHours(3, rem) },
                        onAddHours = { delta ->
                            onAdjustSlotHours(3, houseStatus.slot3.elapsedHours + delta, null)
                        },
                        onScheduleAlarm = {
                            onScheduleAlarm(
                                houseStatus.slot3.remainingHours.coerceAtLeast(0.05),
                                "House #$houseNum: Slot 3 Tier ${houseStatus.slot3.tier.code} Complete"
                            )
                        },
                        disallowedTiers = setOf(PetTier.A, houseStatus.slot2.tier),
                        disallowReason = "In Slot 2",
                        onSelectTier = { tier -> onSetTier(3, tier) }
                    )
                }

                // Mac NPC Replacement Timers: Remaining Hours (Defaults to 360h)
                item {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NPC Replacement Timers",
                                style = MaterialTheme.typography.titleSmall,
                                color = PenyaGold
                            )
                            Text(
                                text = "Default 360h (15-day max timer)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            onClick = onRenewAllMacs,
                            modifier = Modifier.testTag("renew_all_macs_button")
                        ) {
                            Icon(Icons.Default.Autorenew, contentDescription = "Renew All", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset All 360h")
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        houseStatus.macTimers.forEach { mac ->
                            FineTuningMacItem(
                                houseNumber = houseNum,
                                mac = mac,
                                maxHours = macMaxHours,
                                onSetRemainingHours = { rem -> onSetMacRemainingHours(mac.grade, rem) },
                                onRenew = { onRenewMacGrade(mac.grade) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PenyaGold, contentColor = ObsidianBackground),
                modifier = Modifier.testTag("close_house_detail_button")
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}

/**
 * Direct numeric input block for fine-tuning a pet slot without sliders.
 * Note: Keyed on (houseNumber, slotIndex) so background timer ticks do not overwrite typed text!
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FineTuningSlotBlock(
    slot: SlotComputedStatus,
    houseNumber: Int,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    onSetRemainingHours: (Double) -> Unit,
    onAddHours: (Double) -> Unit,
    onScheduleAlarm: () -> Unit,
    disallowedTiers: Set<PetTier> = emptySet(),
    disallowReason: String = "",
    onSelectTier: ((PetTier) -> Unit)? = null
) {
    var customRemainingInput by remember(houseNumber, slot.slotIndex) {
        mutableStateOf(String.format(Locale.US, "%.1f", slot.remainingHours))
    }

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
                text = "${CalculationEngine.formatHours(slot.remainingHours)} remaining",
                style = MaterialTheme.typography.labelLarge,
                color = PenyaGold
            )
        }

        // Tier Rank Selector (for Slots 2 & 3) - Prevents 2 pets of same class
        if (onSelectTier != null) {
            Text(
                text = "Pet Rank (Cannot duplicate pets in same house):",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(PetTier.B, PetTier.C, PetTier.D, PetTier.E, PetTier.F).forEach { tier ->
                    val isDisallowed = disallowedTiers.contains(tier)
                    FilterChip(
                        selected = slot.tier == tier,
                        enabled = !isDisallowed,
                        onClick = {
                            onSelectTier(tier)
                            customRemainingInput = String.format(Locale.US, "%.1f", tier.stageDurationHours)
                        },
                        label = {
                            if (isDisallowed) {
                                Text("${tier.code} ($disallowReason)")
                            } else {
                                Text("${tier.code} (${tier.stageDurationHours.toInt()}h)")
                            }
                        }
                    )
                }
            }
        }

        // Direct Numeric Input Box (No Slider!)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Remaining Hours until Rank Up (Stage: ${slot.requiredHours.toInt()}h):",
                style = MaterialTheme.typography.labelSmall,
                color = FCoinCyan
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customRemainingInput,
                    onValueChange = { customRemainingInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Hours Remaining") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        val parsed = customRemainingInput.toDoubleOrNull() ?: slot.remainingHours
                        onSetRemainingHours(parsed)
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Set", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apply")
                }
            }

            // Quick Preset Buttons
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        customRemainingInput = String.format(Locale.US, "%.1f", slot.requiredHours)
                        onSetRemainingHours(slot.requiredHours)
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Full (${slot.requiredHours.toInt()}h)", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = {
                        val half = slot.requiredHours / 2.0
                        customRemainingInput = String.format(Locale.US, "%.1f", half)
                        onSetRemainingHours(half)
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("50%", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = {
                        customRemainingInput = "10.0"
                        onSetRemainingHours(10.0)
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("10h Left", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = {
                        customRemainingInput = "0.0"
                        onSetRemainingHours(0.0)
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("0h (Ready)", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Action Buttons Row
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
                Text("+10h Prog")
            }
            OutlinedButton(
                onClick = {
                    onReset()
                    customRemainingInput = String.format(Locale.US, "%.1f", slot.requiredHours)
                },
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
                Text("Set Alarm")
            }
        }
    }
}

/**
 * Direct numeric input for Mac NPC remaining hours without slider.
 * Note: Keyed on (houseNumber, mac.grade) so background timer ticks do not overwrite typed text!
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FineTuningMacItem(
    houseNumber: Int,
    mac: MacGradeTimerStatus,
    maxHours: Double,
    onSetRemainingHours: (Double) -> Unit,
    onRenew: () -> Unit
) {
    var hoursInputText by remember(houseNumber, mac.grade) {
        mutableStateOf(String.format(Locale.US, "%.1f", mac.hoursRemaining))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ObsidianElevated, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Mac Grade ${mac.grade.code} NPC Replacement",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (mac.isCurrentlyTraining) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = EmeraldProfit.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "TRAINING",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldProfit,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            Text(
                text = "${String.format(Locale.US, "%.1f", mac.hoursRemaining)}h / ${maxHours.toInt()}h left",
                style = MaterialTheme.typography.labelSmall,
                color = if (mac.isExpired) CrimsonBurnAlert else if (mac.isExpiringSoon) AmberWarning else if (mac.isCurrentlyTraining) EmeraldProfit else FCoinCyan,
                fontWeight = FontWeight.Bold
            )
        }

        // Direct Numeric Input Box (No Slider!)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = hoursInputText,
                onValueChange = { hoursInputText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Remaining Hours (0 - 360)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    val parsed = hoursInputText.toDoubleOrNull() ?: mac.hoursRemaining
                    onSetRemainingHours(parsed.coerceIn(0.0, maxHours))
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Apply", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Apply")
            }
        }

        // Quick Preset Buttons
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedButton(
                onClick = {
                    hoursInputText = "360.0"
                    onRenew()
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("360h (Full)", style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(
                onClick = {
                    hoursInputText = "180.0"
                    onSetRemainingHours(180.0)
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("180h", style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(
                onClick = {
                    hoursInputText = "72.0"
                    onSetRemainingHours(72.0)
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("72h", style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(
                onClick = {
                    hoursInputText = "24.0"
                    onSetRemainingHours(24.0)
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("24h Left", style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(
                onClick = {
                    hoursInputText = "0.0"
                    onSetRemainingHours(0.0)
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("0h (Expired)", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/**
 * Dialog to configure a batch of selected houses at once.
 * Includes explicit confirmation before applying.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BatchHouseSetupDialog(
    selectedHouseNumbers: Set<Int>,
    macMaxHours: Double,
    onDismiss: () -> Unit,
    onApply: (
        slot1RemainingHours: Double?,
        slot1Running: Boolean?,
        slot2Tier: PetTier?,
        slot2RemainingHours: Double?,
        slot2Running: Boolean?,
        slot3Tier: PetTier?,
        slot3RemainingHours: Double?,
        slot3Running: Boolean?,
        macRemainingHoursAll: Double?
    ) -> Unit
) {
    // Configurable fields
    var updateSlot1 by remember { mutableStateOf(true) }
    var slot1Running by remember { mutableStateOf(true) }
    var slot1RemainingText by remember { mutableStateOf("250.0") }

    var updateSlot2 by remember { mutableStateOf(true) }
    var slot2Tier by remember { mutableStateOf(PetTier.B) }
    var slot2Running by remember { mutableStateOf(true) }
    var slot2RemainingText by remember { mutableStateOf("75.0") }

    var updateSlot3 by remember { mutableStateOf(true) }
    var slot3Tier by remember { mutableStateOf(PetTier.C) }
    var slot3Running by remember { mutableStateOf(true) }
    var slot3RemainingText by remember { mutableStateOf("50.0") }

    var updateMacs by remember { mutableStateOf(true) }
    var macRemainingText by remember { mutableStateOf("360.0") }

    var showConfirmApplyDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Batch Configure ${selectedHouseNumbers.size} Houses",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    text = "Houses: ${selectedHouseNumbers.sorted().take(12).joinToString(", ", prefix = "#")}${if (selectedHouseNumbers.size > 12) "..." else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = PenyaGold
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Slot 1: S-Tier Bake (250h)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ObsidianElevated, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = updateSlot1,
                                    onCheckedChange = { updateSlot1 = it },
                                    colors = CheckboxDefaults.colors(checkedColor = PenyaGold)
                                )
                                Text("Slot 1: S-Tier Bake (Grade A)", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            if (updateSlot1) {
                                OutlinedButton(
                                    onClick = { slot1Running = !slot1Running },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(if (slot1Running) "Running" else "Paused", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        if (updateSlot1) {
                            OutlinedTextField(
                                value = slot1RemainingText,
                                onValueChange = { slot1RemainingText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                label = { Text("Remaining Hours (Default 250h)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )

                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(onClick = { slot1RemainingText = "250.0" }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text("250h (Full)", style = MaterialTheme.typography.labelSmall)
                                }
                                OutlinedButton(onClick = { slot1RemainingText = "125.0" }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text("125h (Half)", style = MaterialTheme.typography.labelSmall)
                                }
                                OutlinedButton(onClick = { slot1RemainingText = "0.0" }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text("0h (Ready)", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                // Slot 2: Defaults to B, cannot duplicate Slot 3 or A
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ObsidianElevated, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = updateSlot2,
                                    onCheckedChange = { updateSlot2 = it },
                                    colors = CheckboxDefaults.colors(checkedColor = FCoinCyan)
                                )
                                Text("Slot 2: Pipeline Pet", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            if (updateSlot2) {
                                OutlinedButton(
                                    onClick = { slot2Running = !slot2Running },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(if (slot2Running) "Running" else "Paused", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        if (updateSlot2) {
                            Text("Select Tier (Default B, cannot match Slot 3):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(PetTier.B, PetTier.C, PetTier.D, PetTier.E, PetTier.F).forEach { tier ->
                                    val isDuplicate = tier == slot3Tier
                                    FilterChip(
                                        selected = slot2Tier == tier,
                                        enabled = !isDuplicate,
                                        onClick = {
                                            slot2Tier = tier
                                            slot2RemainingText = tier.stageDurationHours.toInt().toString()
                                        },
                                        label = { Text(if (isDuplicate) "${tier.code} (in S3)" else "${tier.code} (${tier.stageDurationHours.toInt()}h)") }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = slot2RemainingText,
                                onValueChange = { slot2RemainingText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                label = { Text("Remaining Hours (Stage: ${slot2Tier.stageDurationHours.toInt()}h)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Slot 3: Defaults to C, cannot duplicate Slot 2 or A
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ObsidianElevated, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = updateSlot3,
                                    onCheckedChange = { updateSlot3 = it },
                                    colors = CheckboxDefaults.colors(checkedColor = EmeraldProfit)
                                )
                                Text("Slot 3: Pipeline Pet", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            if (updateSlot3) {
                                OutlinedButton(
                                    onClick = { slot3Running = !slot3Running },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(if (slot3Running) "Running" else "Paused", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        if (updateSlot3) {
                            Text("Select Tier (Default C, cannot match Slot 2):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(PetTier.B, PetTier.C, PetTier.D, PetTier.E, PetTier.F).forEach { tier ->
                                    val isDuplicate = tier == slot2Tier
                                    FilterChip(
                                        selected = slot3Tier == tier,
                                        enabled = !isDuplicate,
                                        onClick = {
                                            slot3Tier = tier
                                            slot3RemainingText = tier.stageDurationHours.toInt().toString()
                                        },
                                        label = { Text(if (isDuplicate) "${tier.code} (in S2)" else "${tier.code} (${tier.stageDurationHours.toInt()}h)") }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = slot3RemainingText,
                                onValueChange = { slot3RemainingText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                label = { Text("Remaining Hours (Stage: ${slot3Tier.stageDurationHours.toInt()}h)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Mac NPC Replacement Timers (Defaults to 360h)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ObsidianElevated, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = updateMacs,
                                onCheckedChange = { updateMacs = it },
                                colors = CheckboxDefaults.colors(checkedColor = PenyaGold)
                            )
                            Column {
                                Text("Mac NPC Replacement Timers", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Default 360h (15-day full timer)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        if (updateMacs) {
                            OutlinedTextField(
                                value = macRemainingText,
                                onValueChange = { macRemainingText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                label = { Text("Remaining Hours across all Mac NPCs (0-360)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )

                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(onClick = { macRemainingText = "360.0" }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text("360h (Full)", style = MaterialTheme.typography.labelSmall)
                                }
                                OutlinedButton(onClick = { macRemainingText = "180.0" }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text("180h", style = MaterialTheme.typography.labelSmall)
                                }
                                OutlinedButton(onClick = { macRemainingText = "24.0" }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text("24h", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { showConfirmApplyDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = PenyaGold, contentColor = ObsidianBackground),
                modifier = Modifier.testTag("apply_batch_setup_button")
            ) {
                Text("Apply to ${selectedHouseNumbers.size} Houses", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Explicit Confirmation Modal before batch applying
    if (showConfirmApplyDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmApplyDialog = false },
            containerColor = ObsidianCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = PenyaGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Batch Setup", color = Color.White)
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to apply these settings across all ${selectedHouseNumbers.size} selected houses? This will update their pet tiers, remaining hours, and NPC replacement timers.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmApplyDialog = false
                        val s1Hours = if (updateSlot1) slot1RemainingText.toDoubleOrNull() ?: 250.0 else null
                        val s1Run = if (updateSlot1) slot1Running else null

                        val s2T = if (updateSlot2) slot2Tier else null
                        val s2Hours = if (updateSlot2) slot2RemainingText.toDoubleOrNull() ?: slot2Tier.stageDurationHours else null
                        val s2Run = if (updateSlot2) slot2Running else null

                        val s3T = if (updateSlot3) slot3Tier else null
                        val s3Hours = if (updateSlot3) slot3RemainingText.toDoubleOrNull() ?: slot3Tier.stageDurationHours else null
                        val s3Run = if (updateSlot3) slot3Running else null

                        val macHours = if (updateMacs) macRemainingText.toDoubleOrNull()?.coerceIn(0.0, macMaxHours) ?: 360.0 else null

                        onApply(s1Hours, s1Run, s2T, s2Hours, s2Run, s3T, s3Hours, s3Run, macHours)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PenyaGold, contentColor = ObsidianBackground),
                    modifier = Modifier.testTag("confirm_apply_batch_dialog_button")
                ) {
                    Text("Confirm & Apply", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmApplyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
