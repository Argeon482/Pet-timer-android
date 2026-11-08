package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CalculationEngine
import com.example.ui.PetEmpireUiState
import com.example.ui.theme.AmberWarning
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
fun DashboardScreen(
    uiState: PetEmpireUiState,
    onPauseAllFinished: () -> Unit,
    onInspectWarnings: () -> Unit,
    onAddCompletedCycleProfit: () -> Unit,
    onUpdateAccumulatedProfits: (Long) -> Unit,
    onQuickScheduleAlarm: (Double, String) -> Unit,
    onNavigateToExpansion: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary = uiState.summary
    val settings = uiState.settings
    var showEditTreasuryDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_banner_card"),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(215.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_empire_hero_1794174060591),
                        contentDescription = stringResource(R.string.hero_banner_desc),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        ObsidianBackground.copy(alpha = 0.25f),
                                        ObsidianBackground.copy(alpha = 0.78f),
                                        ObsidianBackground.copy(alpha = 0.96f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = PenyaGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.border(1.dp, PenyaGold.copy(alpha = 0.6f), RoundedCornerShape(50))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HomeWork,
                                        contentDescription = "Active Houses",
                                        tint = PenyaGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${summary.activeHouseCount} Active Houses • 250h Cycle",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = PenyaGold
                                    )
                                }
                            }

                            Surface(
                                color = FCoinCyan.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.border(1.dp, FCoinCyan.copy(alpha = 0.5f), RoundedCornerShape(50))
                            ) {
                                Text(
                                    text = "1 fCoin = ${CalculationEngine.formatPenyaCompact(summary.penyaPerFCoin).removeSuffix(" Penya")}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FCoinCyan,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = stringResource(R.string.dashboard_title),
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Net Yield: ${CalculationEngine.formatPenyaCompact(summary.empireNetProfitCycle)} / cycle (${CalculationEngine.formatPenyaCompact(summary.empireDailyNetProfit)}/day)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EmeraldProfit,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val nextHours = uiState.activeHouses
                                            .mapNotNull { it.nextActionHoursRemaining }
                                            .filter { it > 0.05 }
                                            .minOrNull() ?: 6.0
                                        onQuickScheduleAlarm(
                                            nextHours,
                                            "Flyff Pet Empire: Check Mac NPC Timers (${summary.activeHouseCount} Houses)"
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PenyaGold,
                                        contentColor = ObsidianBackground
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    modifier = Modifier.testTag("hero_set_alarm_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessAlarm,
                                        contentDescription = "Set Clock Alarm",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Set Next Pet Alarm", style = MaterialTheme.typography.labelLarge)
                                }

                                OutlinedButton(
                                    onClick = onNavigateToSettings,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    modifier = Modifier.testTag("hero_economy_settings_button")
                                ) {
                                    Text("Rates & Variables", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Wasted Timer Penalty & Expiry Alert Banner (Core Logic 2A)
        if (summary.housesWithWarningsCount > 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, CrimsonBurnAlert, RoundedCornerShape(20.dp))
                        .testTag("wasted_timer_warning_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CrimsonBurnContainer.copy(alpha = 0.85f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = "Burn Warning",
                                    tint = CrimsonBurnAlert,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Mac NPC Overflow Burn & Expiry Alert",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                color = CrimsonBurnAlert,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "${summary.housesWithWarningsCount} Houses",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "Mac NPCs do NOT pause automatically at 100%. Overflow burn against the 360h timer is actively draining fCoins without adding pet progress:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ObsidianBackground.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Overflow Burned",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f hrs (%.2f fCoins)", summary.totalOverflowHoursAcrossEmpire, summary.totalWastedFCoinsAcrossEmpire),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = AmberWarning,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Wasted Penya Loss",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "-${CalculationEngine.formatPenyaCompact(summary.totalWastedPenyaAcrossEmpire)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CrimsonBurnAlert,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("wasted_penya_loss_text")
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onPauseAllFinished,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CrimsonBurnAlert,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pause_all_finished_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PauseCircle,
                                    contentDescription = "Pause Finished",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pause Finished Pets")
                            }

                            OutlinedButton(
                                onClick = onInspectWarnings,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("inspect_warnings_button")
                            ) {
                                Text("Inspect ${summary.housesWithWarningsCount} Houses")
                            }
                        }
                    }
                }
            }
        }

        // 3. Empire Totals Grid (40-House Scale)
        item {
            SectionHeader(
                title = "${stringResource(R.string.section_empire_totals)} (${summary.activeHouseCount} Houses)",
                subtitle = "Peace of Mind 250h (10.42d) Cycle • 1 S-Tier + 1 Extra Sold A-Tier / House"
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricSummaryCard(
                        title = stringResource(R.string.label_gross_revenue),
                        primaryValue = CalculationEngine.formatPenyaCompact(summary.empireGrossRevenueCycle),
                        secondaryValue = CalculationEngine.formatPenyaExact(summary.empireGrossRevenueCycle),
                        accentColor = PenyaGold,
                        icon = Icons.Default.Savings,
                        testTag = "card_empire_gross_revenue",
                        modifier = Modifier.weight(1f)
                    )
                    MetricSummaryCard(
                        title = stringResource(R.string.label_operating_cost),
                        primaryValue = CalculationEngine.formatPenyaCompact(summary.empireOperatingCostCycle),
                        secondaryValue = CalculationEngine.formatPenyaExact(summary.empireOperatingCostCycle),
                        accentColor = AmberWarning,
                        icon = Icons.Default.Bolt,
                        testTag = "card_empire_operating_cost",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricSummaryCard(
                        title = stringResource(R.string.label_net_profit_cycle),
                        primaryValue = CalculationEngine.formatPenyaCompact(summary.empireNetProfitCycle),
                        secondaryValue = "USD Eq: ${CalculationEngine.formatUsd(CalculationEngine.penyaToUsd(summary.empireNetProfitCycle, settings))}",
                        accentColor = EmeraldProfit,
                        icon = Icons.Default.TrendingUp,
                        testTag = "card_empire_net_profit_cycle",
                        modifier = Modifier.weight(1f)
                    )
                    MetricSummaryCard(
                        title = stringResource(R.string.label_daily_net_profit),
                        primaryValue = "${CalculationEngine.formatPenyaCompact(summary.empireDailyNetProfit)}/d",
                        secondaryValue = "${CalculationEngine.formatPenyaCompact(summary.dailyNetProfitPerHouse)} / house / day",
                        accentColor = EmeraldProfit,
                        icon = Icons.Default.AutoAwesome,
                        testTag = "card_empire_daily_net_profit",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricSummaryCard(
                        title = stringResource(R.string.label_monthly_net_profit),
                        primaryValue = CalculationEngine.formatPenyaCompact(summary.empireMonthlyNetProfit),
                        secondaryValue = "30-Day Baseline (${CalculationEngine.formatUsd(CalculationEngine.penyaToUsd(summary.empireMonthlyNetProfit, settings))})",
                        accentColor = PenyaGold,
                        icon = Icons.Default.TrendingUp,
                        testTag = "card_empire_monthly_net_profit",
                        modifier = Modifier.weight(1f)
                    )
                    MetricSummaryCard(
                        title = stringResource(R.string.label_active_fcoin_burn),
                        primaryValue = String.format(Locale.US, "%,.0f fCoins/cyc", summary.empireFCoinsBurnedPerCycle),
                        secondaryValue = String.format(Locale.US, "Live: %.1f fCoins/hr active", summary.empireActiveHourlyFCoinBurn),
                        accentColor = FCoinCyan,
                        icon = Icons.Default.Bolt,
                        testTag = "card_empire_fcoin_burn",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Dynamic Expansion Power Calculator (Module 1 Core Feature)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PenyaGold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .testTag("expansion_power_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PenyaGold.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddBusiness,
                                    contentDescription = "Expansion Power",
                                    tint = PenyaGold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.section_expansion_power),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Deed + 6-Grade Mac NPC Set = ${CalculationEngine.formatPenyaCompact(summary.newHouseDeedPlusCoreMacCostPenya)} / house",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { showEditTreasuryDialog = true },
                            modifier = Modifier.testTag("edit_treasury_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Accumulated Profits",
                                tint = PenyaGold
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Accumulated Cycle Profits",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CalculationEngine.formatPenyaCompact(settings.accumulatedProfitsPenya),
                                style = MaterialTheme.typography.headlineMedium,
                                color = PenyaGold,
                                modifier = Modifier.testTag("accumulated_profits_value")
                            )
                            Text(
                                text = CalculationEngine.formatPenyaExact(settings.accumulatedProfitsPenya),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = EmeraldProfit.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.border(1.dp, EmeraldProfit, RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "+${summary.affordableNewHousesNow} Houses",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = EmeraldProfit,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("affordable_houses_badge")
                                )
                                Text(
                                    text = "Purchasable Next Cycle",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    val progressToNextHouse = (
                        settings.accumulatedProfitsPenya % summary.newHouseDeedPlusCoreMacCostPenya.coerceAtLeast(1L)
                        ).toFloat() / summary.newHouseDeedPlusCoreMacCostPenya.coerceAtLeast(1L).toFloat()

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Leftover after ${summary.affordableNewHousesNow} houses: ${CalculationEngine.formatPenyaCompact(summary.leftoverPenyaAfterExpansion)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Need ${CalculationEngine.formatPenyaCompact(summary.penyaNeededForNextHouse)} for House #${summary.affordableNewHousesNow + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = FCoinCyan
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progressToNextHouse.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50)),
                            color = EmeraldProfit,
                            trackColor = ObsidianElevated
                        )
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAddCompletedCycleProfit,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldProfit,
                                contentColor = ObsidianBackground
                            ),
                            modifier = Modifier.testTag("add_cycle_profit_button")
                        ) {
                            Text("+1 Full Cycle Profit (+${CalculationEngine.formatPenyaCompact(summary.empireNetProfitCycle).removeSuffix(" Penya")})")
                        }

                        OutlinedButton(
                            onClick = onNavigateToExpansion,
                            modifier = Modifier.testTag("open_expansion_planner_button")
                        ) {
                            Text("Harvest Day Seeding Planner")
                        }
                    }
                }
            }
        }

        // 5. Single House "Peace of Mind" Cycle Breakdown Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("single_house_breakdown_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.section_per_house_yield),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Produces 1 S-Tier Pet (${CalculationEngine.formatPenyaCompact(settings.sTierSellPrice)}) + 1 Extra Sold A-Tier Pet (${CalculationEngine.formatPenyaCompact(settings.aTierSellPrice)}) every 250 hours (10.42 days).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                    BreakdownRow(
                        label = "Gross Revenue / House / Cycle",
                        value = CalculationEngine.formatPenyaExact(summary.grossRevenuePerHouseCycle),
                        valueColor = PenyaGold
                    )
                    BreakdownRow(
                        label = "Mac Timer Burn (F–A Grades vs 360h max)",
                        value = "-${CalculationEngine.formatPenyaExact(summary.macBurnCostPerHouseCycle)}",
                        valueColor = AmberWarning
                    )
                    BreakdownRow(
                        label = "Base F-Tier Pets Consumed (3.125 / cycle)",
                        value = "-${CalculationEngine.formatPenyaExact(summary.fPetCostPerHouseCycle)}",
                        valueColor = AmberWarning
                    )
                    BreakdownRow(
                        label = "Total Operating Cost / House / Cycle",
                        value = CalculationEngine.formatPenyaExact(summary.operatingCostPerHouseCycle),
                        valueColor = Color.White
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    BreakdownRow(
                        label = "Net Profit / House / Cycle",
                        value = CalculationEngine.formatPenyaExact(summary.netProfitPerHouseCycle),
                        valueColor = EmeraldProfit,
                        bold = true
                    )
                    BreakdownRow(
                        label = "Daily Net Profit / House",
                        value = "${CalculationEngine.formatPenyaExact(summary.dailyNetProfitPerHouse)} / day",
                        valueColor = FCoinCyan,
                        bold = true
                    )
                }
            }
        }
    }

    if (showEditTreasuryDialog) {
        var inputMillions by remember(settings.accumulatedProfitsPenya) {
            mutableStateOf((settings.accumulatedProfitsPenya / 1_000_000L).toString())
        }
        AlertDialog(
            onDismissRequest = { showEditTreasuryDialog = false },
            title = { Text("Update Accumulated Cycle Profits") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter your current accumulated Penya treasury (in Millions of Penya, e.g. 1900 = 1.90 Billion Penya):",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = inputMillions,
                        onValueChange = { inputMillions = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Treasury (Millions of Penya)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("treasury_millions_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val m = inputMillions.toLongOrNull() ?: 0L
                        onUpdateAccumulatedProfits(m * 1_000_000L)
                        showEditTreasuryDialog = false
                    },
                    modifier = Modifier.testTag("save_treasury_button")
                ) {
                    Text("Save Treasury")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTreasuryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MetricSummaryCard(
    title: String,
    primaryValue: String,
    secondaryValue: String,
    accentColor: Color,
    icon: ImageVector,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, accentColor.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = primaryValue,
                style = MaterialTheme.typography.titleLarge,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = secondaryValue,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    valueColor: Color,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = if (bold) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
        )
    }
}
