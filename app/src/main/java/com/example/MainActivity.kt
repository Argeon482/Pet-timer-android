package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.CalculationEngine
import com.example.data.PetEmpireDatabase
import com.example.data.PetEmpireRepository
import com.example.ui.HouseFilter
import com.example.ui.PetEmpireUiState
import com.example.ui.PetEmpireViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EconomySettingsScreen
import com.example.ui.screens.ExpansionPlannerScreen
import com.example.ui.screens.HousesTrackerScreen
import com.example.ui.theme.CrimsonBurnAlert
import com.example.ui.theme.FCoinCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.PenyaGold
import com.example.ui.components.AlarmBatchQueueDialog
import com.example.util.AlarmActionItem
import com.example.util.AlarmHelper
import java.util.Locale

enum class EmpireTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD(
        "dashboard",
        R.string.nav_dashboard,
        Icons.Filled.AccountBalance,
        Icons.Outlined.AccountBalance,
        "nav_tab_dashboard"
    ),
    HOUSES(
        "houses",
        R.string.nav_houses,
        Icons.Filled.HomeWork,
        Icons.Outlined.HomeWork,
        "nav_tab_houses"
    ),
    EXPANSION(
        "expansion",
        R.string.nav_expansion,
        Icons.Filled.RocketLaunch,
        Icons.Outlined.RocketLaunch,
        "nav_tab_expansion"
    ),
    SETTINGS(
        "settings",
        R.string.nav_settings,
        Icons.Filled.Tune,
        Icons.Outlined.Tune,
        "nav_tab_settings"
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = PetEmpireDatabase.getInstance(applicationContext)
        val repository = PetEmpireRepository(database.petEmpireDao())

        setContent {
            MyApplicationTheme {
                val viewModel: PetEmpireViewModel = viewModel(
                    factory = PetEmpireViewModel.Factory(repository)
                )
                PetEmpireApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PetEmpireApp(
    viewModel: PetEmpireViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var currentTab by rememberSaveable { mutableStateOf(EmpireTab.DASHBOARD) }
    var showClockDialog by rememberSaveable { mutableStateOf(false) }
    var showAlarmQueueDialog by rememberSaveable { mutableStateOf(false) }
    var queueItems by remember { mutableStateOf<List<AlarmActionItem>>(emptyList()) }
    var queueOnlyChanged by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = currentTab != EmpireTab.DASHBOARD) {
        currentTab = EmpireTab.DASHBOARD
    }

    LaunchedEffect(uiState.lastAlarmMessage) {
        val msg = uiState.lastAlarmMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearAlarmMessage()
        }
    }

    fun triggerClockAlarm(hoursFromNow: Double, label: String) {
        val res = AlarmHelper.scheduleClockAlarm(
            context = context,
            hoursFromNow = hoursFromNow,
            label = label,
            skipUi = false
        )
        val feedback = if (res.launchedClockApp) {
            "Clock alarm opened for ${res.formattedTime} • $label"
        } else {
            "Alarm calculated for ${res.formattedTime} (+${CalculationEngine.formatHours(hoursFromNow)}) • $label"
        }
        viewModel.notifyAlarmScheduled(feedback)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ObsidianBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.dashboard_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.summary.activeHouseCount} Houses • Net ${CalculationEngine.formatPenyaCompact(uiState.summary.empireNetProfitCycle)}/cycle",
                            style = MaterialTheme.typography.labelSmall,
                            color = PenyaGold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showClockDialog = true },
                        modifier = Modifier.testTag("top_bar_clock_alarm_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessAlarm,
                            contentDescription = stringResource(R.string.action_set_alarm),
                            tint = PenyaGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianSurface,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = ObsidianSurface,
                tonalElevation = 8.dp
            ) {
                EmpireTab.entries.forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            if (tab == EmpireTab.HOUSES && uiState.summary.housesWithWarningsCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = CrimsonBurnAlert) {
                                            Text(
                                                text = uiState.summary.housesWithWarningsCount.toString(),
                                                color = Color.White
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = stringResource(tab.labelRes)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = stringResource(tab.labelRes),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ObsidianBackground,
                            selectedTextColor = PenyaGold,
                            indicatorColor = PenyaGold,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            EmpireTab.DASHBOARD -> {
                DashboardScreen(
                    uiState = uiState,
                    onPauseAllFinished = { viewModel.pauseAllFinishedPets() },
                    onInspectWarnings = {
                        viewModel.setHouseFilter(HouseFilter.WARNINGS_ONLY)
                        currentTab = EmpireTab.HOUSES
                    },
                    onAddCompletedCycleProfit = { viewModel.addCompletedCycleProfitToTreasury() },
                    onUpdateAccumulatedProfits = { viewModel.updateAccumulatedProfits(it) },
                    onQuickScheduleAlarm = { hours, label -> triggerClockAlarm(hours, label) },
                    onNavigateToExpansion = { currentTab = EmpireTab.EXPANSION },
                    onNavigateToSettings = { currentTab = EmpireTab.SETTINGS },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            EmpireTab.HOUSES -> {
                HousesTrackerScreen(
                    uiState = uiState,
                    onSelectFilter = { viewModel.setHouseFilter(it) },
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onToggleSlotRunning = { houseNum, slotIdx ->
                        viewModel.toggleSlotRunning(houseNum, slotIdx)
                    },
                    onResetSlotTimer = { houseNum, slotIdx ->
                        viewModel.resetSlotTimer(houseNum, slotIdx)
                    },
                    onAdjustSlotHours = { houseNum, slotIdx, newHours, keepRunning ->
                        viewModel.adjustSlotHours(houseNum, slotIdx, newHours, keepRunning)
                    },
                    onSetSlotRemainingHours = { houseNum, slotIdx, remHours ->
                        viewModel.setSlotRemainingHours(houseNum, slotIdx, remHours)
                    },
                    onSetMacRemainingHours = { houseNum, grade, remHours ->
                        viewModel.setMacRemainingHours(houseNum, grade, remHours)
                    },
                    onSetPipelineSlotTier = { houseNum, slotIdx, tier ->
                        viewModel.setPipelineSlotTier(houseNum, slotIdx, tier)
                    },
                    onRenewMacGrade = { houseNum, grade ->
                        viewModel.renewMacGradeTimer(houseNum, grade)
                    },
                    onRenewAllMacGrades = { houseNum ->
                        viewModel.renewAllMacTimersForHouse(houseNum)
                    },
                    onOpenHouseDetail = { viewModel.selectHouseForDetail(it) },
                    onPauseAllFinished = { viewModel.pauseAllFinishedPets() },
                    onSyncAllHourZero = { viewModel.synchronizeAllHousesToHarvestDayHourZero() },
                    onSetAllRunning = { viewModel.setAllHousesRunningState(it) },
                    onScheduleClockAlarm = { hours, label -> triggerClockAlarm(hours, label) },
                    onScheduleAllEmpireAlarms = { onlyChanged ->
                        val actions = viewModel.getAllEmpireAlarmActionItems(onlyChanged = onlyChanged)
                        if (actions.isEmpty()) {
                            viewModel.notifyAlarmScheduled(
                                if (onlyChanged) "No changed timers pending alarms." else "No active timers currently pending alarms."
                            )
                        } else {
                            queueItems = actions
                            queueOnlyChanged = onlyChanged
                            showAlarmQueueDialog = true
                        }
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            EmpireTab.EXPANSION -> {
                ExpansionPlannerScreen(
                    uiState = uiState,
                    onUpdatePlannerSettings = { cur, tgt, seeds, hour ->
                        viewModel.updateExpansionPlannerSettings(cur, tgt, seeds, hour)
                    },
                    onApplyTargetToEmpire = { viewModel.updateTargetHouseCount(it) },
                    onSyncEmpireHourZero = { viewModel.synchronizeAllHousesToHarvestDayHourZero() },
                    onScheduleClockAlarm = { hours, label -> triggerClockAlarm(hours, label) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            EmpireTab.SETTINGS -> {
                EconomySettingsScreen(
                    uiState = uiState,
                    onSaveSettings = { houses, sPrice, aPrice, fCoinRate, usdRate, deed, fPet, macFC, macHours, profits ->
                        viewModel.updateEconomySettings(
                            targetHouseCount = houses,
                            sTierSellPrice = sPrice,
                            aTierSellPrice = aPrice,
                            fCoinExchangeRatePer1000 = fCoinRate,
                            penyaToUsdPer200M = usdRate,
                            houseDeedCost = deed,
                            baseFPetCost = fPet,
                            macCostFCoins = macFC,
                            macMaxActiveHours = macHours,
                            accumulatedProfitsPenya = profits
                        )
                    },
                    onResetDefaults = { viewModel.resetGlobalSettings() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    if (showClockDialog) {
        ClockAlarmCenterDialog(
            uiState = uiState,
            onDismiss = { showClockDialog = false },
            onScheduleClockAlarm = { hours, label ->
                showClockDialog = false
                triggerClockAlarm(hours, label)
            },
            onScheduleCalendarReminder = { hours, label ->
                showClockDialog = false
                val ok = AlarmHelper.scheduleCalendarReminder(context, hours, label)
                val targetCal = AlarmHelper.computeTargetCalendar(hours)
                val dt = AlarmHelper.formatTargetDateTime(targetCal)
                viewModel.notifyAlarmScheduled(
                    if (ok) "Calendar reminder opened for $dt • $label"
                    else "Calendar reminder scheduled for $dt"
                )
            }
        )
    }

    if (showAlarmQueueDialog && queueItems.isNotEmpty()) {
        AlarmBatchQueueDialog(
            items = queueItems,
            isOnlyChanged = queueOnlyChanged,
            onDismiss = { showAlarmQueueDialog = false },
            onSingleAlarmScheduled = { item ->
                viewModel.notifyAlarmScheduled("Alarm pre-filled: ${item.label}")
            },
            onAllAlarmsFinished = {
                if (queueOnlyChanged) {
                    viewModel.clearAllChangedTimers()
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClockAlarmCenterDialog(
    uiState: PetEmpireUiState,
    onDismiss: () -> Unit,
    onScheduleClockAlarm: (Double, String) -> Unit,
    onScheduleCalendarReminder: (Double, String) -> Unit
) {
    val earliestRemaining: Double = remember(uiState.activeHouses) {
        uiState.activeHouses
            .mapNotNull { it.nextActionHoursRemaining }
            .filter { it > 0.05 }
            .minOrNull() ?: 10.0
    }
    var customHoursText by remember(earliestRemaining) {
        mutableStateOf(String.format(Locale.US, "%.1f", earliestRemaining))
    }
    var customLabelText by remember { mutableStateOf("Flyff Pet Empire: Check Mac NPC Timers") }

    val parsedHours = customHoursText.toDoubleOrNull()?.coerceAtLeast(0.05) ?: earliestRemaining
    val targetCal = AlarmHelper.computeTargetCalendar(parsedHours)
    val previewDateText = AlarmHelper.formatTargetDateTime(targetCal)
    val relativeDuration = AlarmHelper.formatRelativeDuration(parsedHours)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessAlarm, contentDescription = "Clock Alarm", tint = PenyaGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Flyff Alarm & Calendar Scheduler", color = Color.White)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Target Date: $previewDateText (in $relativeDuration)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = PenyaGold
                )

                OutlinedTextField(
                    value = customHoursText,
                    onValueChange = { inputStr: String ->
                        customHoursText = inputStr.filter { ch: Char -> ch.isDigit() || ch == '.' }
                    },
                    label = { Text("Hours Remaining (+${CalculationEngine.formatHours(parsedHours)})") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clock_dialog_hours_input")
                )

                OutlinedTextField(
                    value = customLabelText,
                    onValueChange = { inputStr: String -> customLabelText = inputStr },
                    label = { Text("Alarm Label in Clock App") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clock_dialog_label_input")
                )

                Text(
                    text = "Quick Flyff Pet Stage Duration Presets:",
                    style = MaterialTheme.typography.labelMedium,
                    color = PenyaGold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(onClick = {
                        customHoursText = "10.0"
                        customLabelText = "Flyff Pet Empire: Grade F (10h Complete)"
                    }) {
                        Text("F (10h)")
                    }
                    OutlinedButton(onClick = {
                        customHoursText = "20.0"
                        customLabelText = "Flyff Pet Empire: Grade E (20h Complete)"
                    }) {
                        Text("E (20h)")
                    }
                    OutlinedButton(onClick = {
                        customHoursText = "50.0"
                        customLabelText = "Flyff Pet Empire: Grade D (50h Complete)"
                    }) {
                        Text("D (50h)")
                    }
                    OutlinedButton(onClick = {
                        customHoursText = "50.0"
                        customLabelText = "Flyff Pet Empire: Grade C (50h Complete)"
                    }) {
                        Text("C (50h)")
                    }
                    OutlinedButton(onClick = {
                        customHoursText = "75.0"
                        customLabelText = "Flyff Pet Empire: Grade B (75h Complete)"
                    }) {
                        Text("B (75h)")
                    }
                    OutlinedButton(onClick = {
                        customHoursText = "250.0"
                        customLabelText = "Flyff Pet Empire: S-Tier Bake (250h Complete)"
                    }) {
                        Text("S-Bake (250h)")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onScheduleClockAlarm(parsedHours, customLabelText) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PenyaGold,
                            contentColor = ObsidianBackground
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_clock_alarm_button")
                    ) {
                        Icon(Icons.Default.AccessAlarm, contentDescription = "Alarm", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clock Alarm", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onScheduleCalendarReminder(parsedHours, customLabelText) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_calendar_reminder_button")
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Calendar Event", color = FCoinCyan)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
