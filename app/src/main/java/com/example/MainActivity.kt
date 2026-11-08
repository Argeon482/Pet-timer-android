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
import com.example.util.AlarmHelper

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
                        val (count, desc) = AlarmHelper.scheduleBatchAlarms(context, actions)
                        if (onlyChanged && count > 0) {
                            viewModel.clearAllChangedTimers()
                        }
                        viewModel.notifyAlarmScheduled(
                            if (count > 0) desc else if (onlyChanged) "No changed timers pending alarms." else "No active timers currently pending alarms."
                        )
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
            onScheduleCountdownTimer = { hours, label ->
                showClockDialog = false
                val ok = AlarmHelper.scheduleCountdownTimer(context, hours, label)
                viewModel.notifyAlarmScheduled(
                    if (ok) "Clock countdown timer started (${CalculationEngine.formatHours(hours)}) • $label"
                    else "Countdown timer set for ${CalculationEngine.formatHours(hours)} • $label"
                )
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
    onScheduleCountdownTimer: (Double, String) -> Unit
) {
    var customHoursText by remember { mutableStateOf("4.5") }
    var customLabelText by remember { mutableStateOf("Flyff Pet Empire • Pause Finished Mac NPC") }

    val parsedHours = customHoursText.toDoubleOrNull()?.coerceAtLeast(0.05) ?: 1.0
    val (targetHour, targetMin) = AlarmHelper.computeTargetAlarmTime(parsedHours)
    val previewClockTime = AlarmHelper.formatTime24(targetHour, targetMin)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessAlarm, contentDescription = "Clock Alarm", tint = PenyaGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Android Clock App Alarm Scheduler", color = Color.White)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Set a system Clock alarm so you never forget to manually pause a finished pet and burn 360-hour Mac NPC fCoins:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = customHoursText,
                    onValueChange = { customHoursText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Hours Until Action (Target: $previewClockTime)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clock_dialog_hours_input")
                )

                OutlinedTextField(
                    value = customLabelText,
                    onValueChange = { customLabelText = it },
                    label = { Text("Alarm Label in Clock App") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clock_dialog_label_input")
                )

                Text(
                    text = "Quick Flyff Pet Tier Presets:",
                    style = MaterialTheme.typography.labelMedium,
                    color = PenyaGold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(onClick = {
                        customHoursText = "15.0"
                        customLabelText = "Flyff • Pause Grade F Mac NPC (15h Complete)"
                    }) {
                        Text("Grade F (15h)")
                    }
                    OutlinedButton(onClick = {
                        customHoursText = "25.0"
                        customLabelText = "Flyff • Pause Grade E Mac NPC (25h Complete)"
                    }) {
                        Text("Grade E (25h)")
                    }
                    OutlinedButton(onClick = {
                        customHoursText = "12.0"
                        customLabelText = "Flyff • Check ${uiState.summary.activeHouseCount}-House S-Tier Bake"
                    }) {
                        Text("12h Check")
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
                        Text("Set Clock Alarm", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onScheduleCountdownTimer(parsedHours, customLabelText) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_clock_timer_button")
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = "Timer", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Set Timer", color = FCoinCyan)
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
