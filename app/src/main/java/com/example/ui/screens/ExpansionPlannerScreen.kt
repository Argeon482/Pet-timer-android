package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SyncLock
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CalculationEngine
import com.example.ui.PetEmpireUiState
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningContainer
import com.example.ui.theme.CrimsonBurnAlert
import com.example.ui.theme.CrimsonBurnContainer
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.EmeraldProfitContainer
import com.example.ui.theme.FCoinCyan
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.PenyaGold
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpansionPlannerScreen(
    uiState: PetEmpireUiState,
    onUpdatePlannerSettings: (currentHouses: Int, targetHouses: Int, availableSeeds: Int, currentCycleHour: Double) -> Unit,
    onApplyTargetToEmpire: (Int) -> Unit,
    onSyncEmpireHourZero: () -> Unit,
    onScheduleClockAlarm: (Double, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = uiState.settings
    val eval = uiState.expansionEvaluation

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("expansion_planner_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Target Scaler Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("target_scaler_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Expansion Target Scaler",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = "Scale from ${eval.currentHouses} → ${eval.targetHouses} houses (+${eval.additionalHousesNeeded} new houses)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PenyaGold
                            )
                        }
                        Surface(
                            color = PenyaGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "+${eval.additionalHousesNeeded} Houses",
                                style = MaterialTheme.typography.titleMedium,
                                color = PenyaGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("additional_houses_badge")
                            )
                        }
                    }

                    // Current House Count Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Current Owned Houses",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                            Text(
                                text = "${eval.currentHouses} Houses",
                                style = MaterialTheme.typography.labelLarge,
                                color = FCoinCyan
                            )
                        }
                        Slider(
                            value = eval.currentHouses.toFloat(),
                            onValueChange = { newVal ->
                                val c = newVal.roundToInt().coerceIn(0, 50)
                                val t = maxOf(c, eval.targetHouses).coerceIn(1, 50)
                                onUpdatePlannerSettings(c, t, eval.availableAPetSeeds, settings.currentCycleHour)
                            },
                            valueRange = 0f..50f,
                            steps = 49,
                            colors = SliderDefaults.colors(
                                thumbColor = FCoinCyan,
                                activeTrackColor = FCoinCyan
                            ),
                            modifier = Modifier.testTag("slider_current_houses")
                        )
                    }

                    // Target House Count Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Target Expansion Scale (Up to 50)",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                            Text(
                                text = "${eval.targetHouses} Houses",
                                style = MaterialTheme.typography.labelLarge,
                                color = PenyaGold
                            )
                        }
                        Slider(
                            value = eval.targetHouses.toFloat(),
                            onValueChange = { newVal ->
                                val t = newVal.roundToInt().coerceIn(1, 50)
                                onUpdatePlannerSettings(eval.currentHouses, t, eval.availableAPetSeeds, settings.currentCycleHour)
                            },
                            valueRange = 1f..50f,
                            steps = 48,
                            colors = SliderDefaults.colors(
                                thumbColor = PenyaGold,
                                activeTrackColor = PenyaGold
                            ),
                            modifier = Modifier.testTag("slider_target_houses")
                        )
                    }

                    // Quick Scale Preset Buttons
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onUpdatePlannerSettings(10, 20, 10, 0.0) },
                            modifier = Modifier.testTag("preset_10_to_20")
                        ) {
                            Text("10 → 20 Houses")
                        }
                        OutlinedButton(
                            onClick = { onUpdatePlannerSettings(20, 40, 20, 0.0) },
                            modifier = Modifier.testTag("preset_20_to_40")
                        ) {
                            Text("20 → 40 Houses (Standard)")
                        }
                        OutlinedButton(
                            onClick = { onUpdatePlannerSettings(0, 40, 40, 0.0) },
                            modifier = Modifier.testTag("preset_0_to_40")
                        ) {
                            Text("0 → 40 Fresh Empire")
                        }
                    }
                }
            }
        }

        // 2. Dynamic Capital Requirement Breakdown (Penya & USD Cash Injection)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PenyaGold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .testTag("capital_breakdown_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CurrencyExchange,
                            contentDescription = "Capital Breakdown",
                            tint = PenyaGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Dynamic Capital Requirement Breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Live rates: ${CalculationEngine.formatUsd(settings.penyaToUsdPer200M)} / 200M Penya • 1k fCoins = ${CalculationEngine.formatPenyaCompact(settings.fCoinExchangeRatePer1000)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                    CapitalItemRow(
                        title = "House Deeds (${eval.additionalHousesNeeded} × ${CalculationEngine.formatPenyaCompact(settings.houseDeedCost)})",
                        penyaFormatted = CalculationEngine.formatPenyaCompact(eval.totalDeedsPenya),
                        subDetail = "USD Cash Eq: ${CalculationEngine.formatUsd(eval.totalDeedsUsd)}",
                        accentColor = PenyaGold
                    )

                    CapitalItemRow(
                        title = "Mac NPC Sets (${eval.additionalHousesNeeded} × 6 Grades = ${String.format(Locale.US, "%,d", eval.totalMacSetsFCoins)} fCoins)",
                        penyaFormatted = CalculationEngine.formatPenyaCompact(eval.totalMacSetsPenya),
                        subDetail = "USD Cash Eq: ${CalculationEngine.formatUsd(eval.totalMacSetsUsd)}",
                        accentColor = FCoinCyan
                    )

                    CapitalItemRow(
                        title = "Initial Pipeline F-Tier Pets (${eval.additionalHousesNeeded * 2} × ${CalculationEngine.formatPenyaCompact(settings.baseFPetCost)})",
                        penyaFormatted = CalculationEngine.formatPenyaCompact(eval.totalBaseFPetsPenya),
                        subDetail = "2 Base F-Pets per new house (Slots 2 & 3)",
                        accentColor = EmeraldProfit
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ObsidianElevated, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Capital Required (+${eval.additionalHousesNeeded} Houses)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CalculationEngine.formatPenyaCompact(eval.totalCapitalPenya),
                                style = MaterialTheme.typography.headlineMedium,
                                color = PenyaGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("total_expansion_penya_text")
                            )
                            Text(
                                text = CalculationEngine.formatPenyaExact(eval.totalCapitalPenya),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = EmeraldProfitContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.border(1.dp, EmeraldProfit, RoundedCornerShape(14.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "USD Cash Injection",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = CalculationEngine.formatUsd(eval.totalCapitalUsd),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = EmeraldProfit,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("total_expansion_usd_text")
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. "Harvest Day" Synchronization & A-Pet Seeding Trick Validator
        item {
            val statusBorder = if (eval.isFullySeededReady) EmeraldProfit else AmberWarning
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, statusBorder, RoundedCornerShape(20.dp))
                    .testTag("harvest_day_sync_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.SyncLock,
                                contentDescription = "Harvest Day Sync",
                                tint = statusBorder,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Harvest Day Synchronization Check",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Enforces Hour 0 Cycle Alignment + A-Pet Slot 1 Seeding Trick (Bypasses 205h F-to-A Ramp-Up)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Cycle Hour Control (Hour 0 to 250h)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ObsidianElevated, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Current Empire Cycle Clock",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                            Text(
                                text = if (eval.isCycleAtHourZero) {
                                    "Hour 0.0 / 250h (HARVEST DAY WINDOW)"
                                } else {
                                    String.format(Locale.US, "Hour %.0f / 250h (Wait %.0fh for Sync)", settings.currentCycleHour, eval.hoursUntilNextHarvestSync)
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = if (eval.isCycleAtHourZero) EmeraldProfit else AmberWarning
                            )
                        }
                        Slider(
                            value = settings.currentCycleHour.toFloat(),
                            onValueChange = { newHour ->
                                onUpdatePlannerSettings(
                                    eval.currentHouses,
                                    eval.targetHouses,
                                    eval.availableAPetSeeds,
                                    newHour.roundToInt().toDouble()
                                )
                            },
                            valueRange = 0f..250f,
                            colors = SliderDefaults.colors(
                                thumbColor = if (eval.isCycleAtHourZero) EmeraldProfit else AmberWarning,
                                activeTrackColor = if (eval.isCycleAtHourZero) EmeraldProfit else AmberWarning
                            ),
                            modifier = Modifier.testTag("slider_cycle_hour")
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    onUpdatePlannerSettings(eval.currentHouses, eval.targetHouses, eval.availableAPetSeeds, 0.0)
                                },
                                modifier = Modifier.testTag("set_hour_zero_button")
                            ) {
                                Text("Set Hour 0 (Harvest Day)")
                            }
                            if (!eval.isCycleAtHourZero) {
                                Button(
                                    onClick = {
                                        onScheduleClockAlarm(
                                            eval.hoursUntilNextHarvestSync,
                                            "Flyff Harvest Day Sync: Start ${eval.additionalHousesNeeded} New Houses at Hour 0!"
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PenyaGold,
                                        contentColor = ObsidianBackground
                                    ),
                                    modifier = Modifier.testTag("schedule_harvest_sync_alarm_button")
                                ) {
                                    Icon(Icons.Default.AccessAlarm, contentDescription = "Alarm", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Alarm in ${eval.hoursUntilNextHarvestSync.toInt()}h")
                                }
                            }
                        }
                    }

                    // A-Pet Seed Inventory Control
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ObsidianElevated, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Spare A-Tier Pets Ready for Slot 1 Seeding",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                            Text(
                                text = "${eval.availableAPetSeeds} / ${eval.additionalHousesNeeded} Needed",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (eval.seedsShortfall == 0) EmeraldProfit else CrimsonBurnAlert
                            )
                        }
                        Slider(
                            value = eval.availableAPetSeeds.toFloat().coerceIn(0f, 50f),
                            onValueChange = { newSeeds ->
                                onUpdatePlannerSettings(
                                    eval.currentHouses,
                                    eval.targetHouses,
                                    newSeeds.roundToInt(),
                                    settings.currentCycleHour
                                )
                            },
                            valueRange = 0f..50f,
                            steps = 49,
                            colors = SliderDefaults.colors(
                                thumbColor = FCoinCyan,
                                activeTrackColor = FCoinCyan
                            ),
                            modifier = Modifier.testTag("slider_available_seeds")
                        )
                        Text(
                            text = "Tip: Each existing house produces 1 extra A-tier pet per cycle (${eval.currentHouses} houses = ${eval.currentHouses} spare A-pets per harvest).",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Verification Summary Box
                    val summaryBg = when {
                        eval.isFullySeededReady -> EmeraldProfitContainer.copy(alpha = 0.7f)
                        !eval.isCycleAtHourZero -> AmberWarningContainer.copy(alpha = 0.75f)
                        else -> CrimsonBurnContainer.copy(alpha = 0.75f)
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(summaryBg, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (eval.isFullySeededReady) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                                contentDescription = "Synchronization Status",
                                tint = if (eval.isFullySeededReady) EmeraldProfit else AmberWarning,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    eval.isFullySeededReady ->
                                        "READY FOR HARVEST DAY LAUNCH: Hour 0 Aligned & 100% A-Pet Seeded!"
                                    !eval.isCycleAtHourZero ->
                                        "HOLD EXPANSION: Wait ${eval.hoursUntilNextHarvestSync.toInt()}h until Hour 0 Harvest Day!"
                                    else ->
                                        "SEED SHORTFALL: Missing ${eval.seedsShortfall} A-Tier Pets for Slot 1!"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("harvest_sync_verdict_text")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = "Ramp-up saved",
                                    tint = FCoinCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "205h F-to-A Ramp-Up Saved:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "${eval.rampUpHoursSavedBySeeding.toInt()} House-Hours Saved",
                                style = MaterialTheme.typography.labelLarge,
                                color = EmeraldProfit
                            )
                        }

                        if (eval.seedsShortfall > 0) {
                            Text(
                                text = "Warning: Starting ${eval.seedsShortfall} unseeded houses forces a 205-hour F→A ramp-up delay (${eval.unseededPenaltyHours.toInt()} total house-hours desynchronized).",
                                style = MaterialTheme.typography.labelSmall,
                                color = CrimsonBurnAlert
                            )
                        }
                    }

                    // Execute Synchronized Expansion Button
                    Button(
                        onClick = {
                            onApplyTargetToEmpire(eval.targetHouses)
                            onSyncEmpireHourZero()
                        },
                        enabled = eval.additionalHousesNeeded >= 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PenyaGold,
                            contentColor = ObsidianBackground
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("execute_harvest_expansion_button")
                    ) {
                        Icon(Icons.Default.RocketLaunch, contentDescription = "Launch Expansion", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Commit ${eval.targetHouses}-House Scale & Sync Hour 0",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CapitalItemRow(
    title: String,
    penyaFormatted: String,
    subDetail: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subDetail,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = penyaFormatted,
            style = MaterialTheme.typography.titleMedium,
            color = accentColor,
            fontWeight = FontWeight.Bold
        )
    }
}
