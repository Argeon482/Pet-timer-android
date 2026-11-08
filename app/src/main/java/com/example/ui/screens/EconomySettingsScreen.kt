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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.CalculationEngine
import com.example.ui.PetEmpireUiState
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.FCoinCyan
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.PenyaGold
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EconomySettingsScreen(
    uiState: PetEmpireUiState,
    onSaveSettings: (
        targetHouseCount: Int,
        sTierSellPrice: Long,
        aTierSellPrice: Long,
        fCoinExchangeRatePer1000: Long,
        penyaToUsdPer200M: Double,
        houseDeedCost: Long,
        baseFPetCost: Long,
        macCostFCoins: Int,
        macMaxActiveHours: Double,
        accumulatedProfitsPenya: Long
    ) -> Unit,
    onResetDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = uiState.settings
    val summary = uiState.summary

    var targetHouses by remember(s.targetHouseCount) { mutableIntStateOf(s.targetHouseCount) }
    var sTierPriceText by remember(s.sTierSellPrice) { mutableStateOf(s.sTierSellPrice.toString()) }
    var aTierPriceText by remember(s.aTierSellPrice) { mutableStateOf(s.aTierSellPrice.toString()) }
    var fCoinRateText by remember(s.fCoinExchangeRatePer1000) { mutableStateOf(s.fCoinExchangeRatePer1000.toString()) }
    var usdRateText by remember(s.penyaToUsdPer200M) { mutableStateOf(s.penyaToUsdPer200M.toString()) }
    var houseDeedText by remember(s.houseDeedCost) { mutableStateOf(s.houseDeedCost.toString()) }
    var baseFPetText by remember(s.baseFPetCost) { mutableStateOf(s.baseFPetCost.toString()) }
    var macFCoinsText by remember(s.macCostFCoins) { mutableStateOf(s.macCostFCoins.toString()) }
    var macMaxHoursText by remember(s.macMaxActiveHours) { mutableStateOf(s.macMaxActiveHours.toInt().toString()) }
    var accumulatedProfitsText by remember(s.accumulatedProfitsPenya) { mutableStateOf(s.accumulatedProfitsPenya.toString()) }

    fun pushUpdate(
        housesOverride: Int = targetHouses,
        sTierOverride: String = sTierPriceText,
        aTierOverride: String = aTierPriceText,
        fCoinOverride: String = fCoinRateText,
        usdOverride: String = usdRateText,
        deedOverride: String = houseDeedText,
        fPetOverride: String = baseFPetText,
        macFCoinsOverride: String = macFCoinsText,
        macHoursOverride: String = macMaxHoursText,
        profitsOverride: String = accumulatedProfitsText
    ) {
        onSaveSettings(
            housesOverride.coerceIn(1, 50),
            sTierOverride.toLongOrNull() ?: s.sTierSellPrice,
            aTierOverride.toLongOrNull() ?: s.aTierSellPrice,
            fCoinOverride.toLongOrNull() ?: s.fCoinExchangeRatePer1000,
            usdOverride.toDoubleOrNull() ?: s.penyaToUsdPer200M,
            deedOverride.toLongOrNull() ?: s.houseDeedCost,
            fPetOverride.toLongOrNull() ?: s.baseFPetCost,
            macFCoinsOverride.toIntOrNull() ?: s.macCostFCoins,
            macHoursOverride.toDoubleOrNull() ?: s.macMaxActiveHours,
            profitsOverride.toLongOrNull() ?: s.accumulatedProfitsPenya
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("economy_settings_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Live Impact Preview Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FCoinCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Dynamic Variables",
                            tint = FCoinCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Live Server Economy & Mac NPC Pricing",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ObsidianElevated, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "1 Mac NPC (${s.macCostFCoins} fCoins)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CalculationEngine.formatPenyaCompact(summary.macNpcCostPenya),
                                style = MaterialTheme.typography.titleMedium,
                                color = FCoinCyan,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("live_mac_cost_penya_text")
                            )
                            Text(
                                text = "6-Grade Set: ${CalculationEngine.formatPenyaCompact(summary.macFullSet6CostPenya)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Empire Net / Cycle (${summary.activeHouseCount}H)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CalculationEngine.formatPenyaCompact(summary.empireNetProfitCycle),
                                style = MaterialTheme.typography.titleMedium,
                                color = EmeraldProfit,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("live_empire_net_text")
                            )
                            Text(
                                text = String.format(Locale.US, "Burn: %s/hr per Mac", CalculationEngine.formatPenyaCompact(summary.macHourlyBurnPenya.toLong())),
                                style = MaterialTheme.typography.labelSmall,
                                color = PenyaGold
                            )
                        }
                    }

                    // Server Inflation Quick Presets
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onResetDefaults,
                            modifier = Modifier.testTag("reset_default_economy_button")
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = "Reset Defaults", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Standard Default (1k fC = 500M)")
                        }

                        OutlinedButton(
                            onClick = {
                                fCoinRateText = "600000000"
                                sTierPriceText = "145000000"
                                aTierPriceText = "78000000"
                                pushUpdate(
                                    fCoinOverride = "600000000",
                                    sTierOverride = "145000000",
                                    aTierOverride = "78000000"
                                )
                            },
                            modifier = Modifier.testTag("preset_high_inflation_button")
                        ) {
                            Text("High Inflation (1k fC = 600M)")
                        }
                    }
                }
            }
        }

        // Dynamic Variables Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianCard)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "1. Target House Count (1 to 50)",
                        style = MaterialTheme.typography.titleSmall,
                        color = PenyaGold,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Empire Scale: $targetHouses Houses",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Default: 40",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Slider(
                        value = targetHouses.toFloat(),
                        onValueChange = {
                            targetHouses = it.roundToInt().coerceIn(1, 50)
                            pushUpdate(housesOverride = targetHouses)
                        },
                        valueRange = 1f..50f,
                        steps = 48,
                        colors = SliderDefaults.colors(
                            thumbColor = PenyaGold,
                            activeTrackColor = PenyaGold
                        ),
                        modifier = Modifier.testTag("settings_target_houses_slider")
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                    Text(
                        text = "2. Pet Market & Currency Exchange Rates",
                        style = MaterialTheme.typography.titleSmall,
                        color = PenyaGold,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = sTierPriceText,
                        onValueChange = {
                            sTierPriceText = it.filter { c -> c.isDigit() }
                            pushUpdate(sTierOverride = sTierPriceText)
                        },
                        label = { Text("S-Tier Pet Sell Price (Penya • Default: 120,000,000)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_s_tier_price")
                    )

                    OutlinedTextField(
                        value = aTierPriceText,
                        onValueChange = {
                            aTierPriceText = it.filter { c -> c.isDigit() }
                            pushUpdate(aTierOverride = aTierPriceText)
                        },
                        label = { Text("A-Tier Pet Sell Price (Penya • Default: 65,000,000)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_a_tier_price")
                    )

                    OutlinedTextField(
                        value = fCoinRateText,
                        onValueChange = {
                            fCoinRateText = it.filter { c -> c.isDigit() }
                            pushUpdate(fCoinOverride = fCoinRateText)
                        },
                        label = { Text("fCoin Rate: Penya per 1,000 fCoins (Default: 500,000,000)") },
                        supportingText = {
                            Text("1 fCoin = ${CalculationEngine.formatPenyaExact(summary.penyaPerFCoin)}")
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_fcoin_rate")
                    )

                    OutlinedTextField(
                        value = usdRateText,
                        onValueChange = {
                            usdRateText = it.filter { c -> c.isDigit() || c == '.' }
                            pushUpdate(usdOverride = usdRateText)
                        },
                        label = { Text("Penya-to-USD Rate: USD per 200M Penya (Default: 3.09)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_usd_rate")
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                    Text(
                        text = "3. Deeds, Base Pets & Mac NPC Configuration",
                        style = MaterialTheme.typography.titleSmall,
                        color = PenyaGold,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = houseDeedText,
                        onValueChange = {
                            houseDeedText = it.filter { c -> c.isDigit() }
                            pushUpdate(deedOverride = houseDeedText)
                        },
                        label = { Text("House Deed Cost (Penya • Default: 100,000,000)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_house_deed_cost")
                    )

                    OutlinedTextField(
                        value = baseFPetText,
                        onValueChange = {
                            baseFPetText = it.filter { c -> c.isDigit() }
                            pushUpdate(fPetOverride = baseFPetText)
                        },
                        label = { Text("Base F-Tier Pet Cost (Penya • Default: 2,000,000)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_base_f_pet_cost")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = macFCoinsText,
                            onValueChange = {
                                macFCoinsText = it.filter { c -> c.isDigit() }
                                pushUpdate(macFCoinsOverride = macFCoinsText)
                            },
                            label = { Text("Mac Cost (fCoins • Def: 140)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_mac_fcoins")
                        )

                        OutlinedTextField(
                            value = macMaxHoursText,
                            onValueChange = {
                                macMaxHoursText = it.filter { c -> c.isDigit() }
                                pushUpdate(macHoursOverride = macMaxHoursText)
                            },
                            label = { Text("Mac Max Hours (Def: 360h)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_mac_max_hours")
                        )
                    }

                    OutlinedTextField(
                        value = accumulatedProfitsText,
                        onValueChange = {
                            accumulatedProfitsText = it.filter { c -> c.isDigit() }
                            pushUpdate(profitsOverride = accumulatedProfitsText)
                        },
                        label = { Text("Accumulated Cycle Profits (Penya Treasury)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_accumulated_profits")
                    )

                    Button(
                        onClick = { pushUpdate() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PenyaGold,
                            contentColor = ObsidianBackground
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("apply_economy_settings_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Apply")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recalculate All 40-House Projections", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
