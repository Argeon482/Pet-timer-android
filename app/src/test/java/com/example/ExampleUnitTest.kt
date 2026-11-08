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

    @Test
    fun petTierHours_matchUserSpecification() {
        assertEquals(250.0, com.example.data.PetTier.A.stageDurationHours, 0.001)
        assertEquals(75.0, com.example.data.PetTier.B.stageDurationHours, 0.001)
        assertEquals(50.0, com.example.data.PetTier.C.stageDurationHours, 0.001)
        assertEquals(50.0, com.example.data.PetTier.D.stageDurationHours, 0.001)
        assertEquals(20.0, com.example.data.PetTier.E.stageDurationHours, 0.001)
        assertEquals(10.0, com.example.data.PetTier.F.stageDurationHours, 0.001)
        // Total F-to-A ramp-up hours = 10 + 20 + 50 + 50 + 75 = 205h
        assertEquals(205.0, com.example.data.PetTier.TOTAL_F_TO_A_RAMP_HOURS, 0.001)
    }

    @Test
    fun alarmConsolidation_reducesSameClassTimersWithinOneHourToEarliest() {
        val raw = listOf(
            com.example.util.AlarmActionItem(
                hoursFromNow = 12.0,
                label = "House #1: Grade C → B Ready",
                categoryKey = "tier_C_to_B"
            ),
            com.example.util.AlarmActionItem(
                hoursFromNow = 12.2,
                label = "House #2: Grade C → B Ready",
                categoryKey = "tier_C_to_B"
            ),
            com.example.util.AlarmActionItem(
                hoursFromNow = 12.5,
                label = "House #3: Grade C → B Ready",
                categoryKey = "tier_C_to_B"
            ),
            com.example.util.AlarmActionItem(
                hoursFromNow = 12.9,
                label = "House #4: Grade C → B Ready",
                categoryKey = "tier_C_to_B"
            ),
            // Distinct category at 12.1h: should NOT merge with C->B
            com.example.util.AlarmActionItem(
                hoursFromNow = 12.1,
                label = "House #5: Grade B → A Ready",
                categoryKey = "tier_B_to_A"
            ),
            // Grade C at 15.0h: >1 hour after 12.0h -> separate cluster
            com.example.util.AlarmActionItem(
                hoursFromNow = 15.0,
                label = "House #6: Grade C → B Ready",
                categoryKey = "tier_C_to_B"
            )
        )

        val consolidated = com.example.util.AlarmHelper.consolidateAlarmItems(raw)
        // Expected:
        // 1. Earliest C->B cluster: hours = 12.0, clusterCount = 4
        // 2. B->A item: hours = 12.1, clusterCount = 1
        // 3. Later C->B item: hours = 15.0, clusterCount = 1
        assertEquals(3, consolidated.size)

        val cluster1 = consolidated.first { it.categoryKey == "tier_C_to_B" && it.hoursFromNow < 13.0 }
        assertEquals(12.0, cluster1.hoursFromNow, 0.001)
        assertEquals(4, cluster1.clusterCount)

        val clusterB = consolidated.first { it.categoryKey == "tier_B_to_A" }
        assertEquals(12.1, clusterB.hoursFromNow, 0.001)
        assertEquals(1, clusterB.clusterCount)

        val clusterLaterC = consolidated.first { it.categoryKey == "tier_C_to_B" && it.hoursFromNow > 14.0 }
        assertEquals(15.0, clusterLaterC.hoursFromNow, 0.001)
        assertEquals(1, clusterLaterC.clusterCount)
    }
}
