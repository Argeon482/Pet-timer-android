package com.example

import com.example.data.CalculationEngine
import com.example.data.GlobalSettingsEntity
import com.example.data.HouseEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun default40HousePeaceOfMindCycle_matchesSpecification() {
        val settings = GlobalSettingsEntity()
        val summary = CalculationEngine.computeEmpireSummary(settings, emptyList())

        // Per-House / Cycle (250 hours / 10.42 days)
        assertEquals(185_000_000L, summary.grossRevenuePerHouseCycle)
        assertEquals(137_500_000L, summary.operatingCostPerHouseCycle)
        assertEquals(47_500_000L, summary.netProfitPerHouseCycle)
        assertEquals(4_560_000L, summary.dailyNetProfitPerHouse)

        // 40-House Empire Totals
        assertEquals(7_400_000_000L, summary.empireGrossRevenueCycle)
        assertEquals(5_500_000_000L, summary.empireOperatingCostCycle)
        assertEquals(1_900_000_000L, summary.empireNetProfitCycle)
        assertEquals(182_400_000L, summary.empireDailyNetProfit)
        assertEquals(5_472_000_000L, summary.empireMonthlyNetProfit)
    }

    @Test
    fun wastedTimerOverflow_calculatesPenyaLossAccurately() {
        val settings = GlobalSettingsEntity()
        val house = HouseEntity(
            houseNumber = 1,
            slot1Running = true,
            slot1AccumulatedHours = 268.0, // 18h overflow past 250h!
            slot1LastStartedEpochMs = null,
            slot2Running = false,
            slot3Running = false
        )
        val status = CalculationEngine.computeHouseStatus(house, settings, 0L)
        assertTrue(status.hasOverflowWarning)
        assertEquals(18.0, status.slot1.overflowHours, 0.001)
        // 18h out of 360h = 5% of 140 fCoins = 7 fCoins = 3,500,000 Penya wasted
        assertEquals(3_500_000L, status.slot1.wastedPenya)
    }
}
