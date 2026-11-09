package com.example

import com.example.data.CalculationEngine
import com.example.data.GlobalSettingsEntity
import com.example.data.HouseEntity
import com.example.data.PetTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun default40HousePeaceOfMindCycle_matchesUpdatedDefaults() {
        val settings = GlobalSettingsEntity()
        assertEquals(220_000_000L, settings.sTierSellPrice)
        assertEquals(120_000_000L, settings.aTierSellPrice)

        val summary = CalculationEngine.computeEmpireSummary(settings, emptyList())

        // Per-House / Cycle (250 hours / 10.42 days)
        assertEquals(340_000_000L, summary.grossRevenuePerHouseCycle)
        assertEquals(137_500_000L, summary.operatingCostPerHouseCycle)
        assertEquals(202_500_000L, summary.netProfitPerHouseCycle)
        assertEquals(19_440_000L, summary.dailyNetProfitPerHouse)

        // 40-House Empire Totals
        assertEquals(13_600_000_000L, summary.empireGrossRevenueCycle)
        assertEquals(5_500_000_000L, summary.empireOperatingCostCycle)
        assertEquals(8_100_000_000L, summary.empireNetProfitCycle)
        assertEquals(777_600_000L, summary.empireDailyNetProfit)
        assertEquals(23_328_000_000L, summary.empireMonthlyNetProfit)
    }

    @Test
    fun defaultHouseEntity_hasSlotsABCAnd360HourMacTimers() {
        val house = HouseEntity(houseNumber = 1)
        // Slot 1 is the S bake (Grade A)
        // Slot 2 must default to B
        // Slot 3 must default to C
        assertEquals("B", house.slot2Tier)
        assertEquals("C", house.slot3Tier)
        assertNotEquals(house.slot2Tier, house.slot3Tier)
        assertNotEquals("A", house.slot2Tier)
        assertNotEquals("A", house.slot3Tier)

        // NPC replacement hours used must default to 0.0 (which equals 360h remaining)
        assertEquals(0.0, house.macFHoursUsed, 0.001)
        assertEquals(0.0, house.macEHoursUsed, 0.001)
        assertEquals(0.0, house.macDHoursUsed, 0.001)
        assertEquals(0.0, house.macCHoursUsed, 0.001)
        assertEquals(0.0, house.macBHoursUsed, 0.001)
        assertEquals(0.0, house.macAHoursUsed, 0.001)

        val settings = GlobalSettingsEntity()
        val status = CalculationEngine.computeHouseStatus(house, settings, 0L)
        status.macTimers.forEach { mac ->
            assertEquals(360.0, mac.hoursRemaining, 0.001)
        }
    }

    @Test
    fun macReplacementTimers_countDownWhenParticularRankIsTraining_andStayInactiveOtherwise() {
        val settings = GlobalSettingsEntity()
        val startMs = 1_000_000L
        val tenHoursLaterMs = startMs + (10L * 3_600_000L) // 10 hours later

        val house = HouseEntity(
            houseNumber = 1,
            // Slot 1: Training Grade A pet, currently running for 10h
            slot1Running = true,
            slot1AccumulatedHours = 0.0,
            slot1LastStartedEpochMs = startMs,
            // Slot 2: Grade B pet, currently running for 10h
            slot2Tier = "B",
            slot2Running = true,
            slot2AccumulatedHours = 0.0,
            slot2LastStartedEpochMs = startMs,
            // Slot 3: Grade C pet, but PAUSED / INACTIVE
            slot3Tier = "C",
            slot3Running = false,
            slot3AccumulatedHours = 0.0,
            slot3LastStartedEpochMs = null
        )

        val status = CalculationEngine.computeHouseStatus(house, settings, tenHoursLaterMs)
        val macA = status.macTimers.first { it.grade == PetTier.A }
        val macB = status.macTimers.first { it.grade == PetTier.B }
        val macC = status.macTimers.first { it.grade == PetTier.C }
        val macD = status.macTimers.first { it.grade == PetTier.D }
        val macE = status.macTimers.first { it.grade == PetTier.E }
        val macF = status.macTimers.first { it.grade == PetTier.F }

        // Grade A is actively training: counted down from 360h to 350h
        assertTrue(macA.isCurrentlyTraining)
        assertEquals(350.0, macA.hoursRemaining, 0.001)
        assertEquals(10.0, macA.hoursUsed, 0.001)

        // Grade B is actively training: counted down from 360h to 350h
        assertTrue(macB.isCurrentlyTraining)
        assertEquals(350.0, macB.hoursRemaining, 0.001)
        assertEquals(10.0, macB.hoursUsed, 0.001)

        // Grade C is paused (not training): did NOT count down
        assertFalse(macC.isCurrentlyTraining)
        assertEquals(360.0, macC.hoursRemaining, 0.001)

        // Grades D, E, F are inactive: did NOT count down
        assertFalse(macD.isCurrentlyTraining)
        assertEquals(360.0, macD.hoursRemaining, 0.001)
        assertFalse(macE.isCurrentlyTraining)
        assertEquals(360.0, macE.hoursRemaining, 0.001)
        assertFalse(macF.isCurrentlyTraining)
        assertEquals(360.0, macF.hoursRemaining, 0.001)
    }

    @Test
    fun macReplacementTimers_continueToCountDownWhenPetFinishesTraining_untilManuallyStopped() {
        val settings = GlobalSettingsEntity()
        val startMs = 1_000_000L
        // Grade B takes 75h to complete. Let's run for 80h (pet is finished at 100%, 5h overflow).
        val eightyHoursLaterMs = startMs + (80L * 3_600_000L)

        val house = HouseEntity(
            houseNumber = 1,
            slot1Running = false,
            slot2Tier = "B",
            slot2Running = true, // Still running! Not stopped by user.
            slot2AccumulatedHours = 0.0,
            slot2LastStartedEpochMs = startMs,
            slot3Running = false
        )

        val status = CalculationEngine.computeHouseStatus(house, settings, eightyHoursLaterMs)
        val macB = status.macTimers.first { it.grade == PetTier.B }

        // Pet has finished training (80h >= 75h required)
        assertTrue(status.slot2.isFinished)
        // Mac NPC did NOT stop when pet finished; it counted down the full 80 hours because user did not stop it!
        assertTrue(macB.isCurrentlyTraining)
        assertEquals(280.0, macB.hoursRemaining, 0.001) // 360h - 80h = 280h
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
        assertEquals(250.0, PetTier.A.stageDurationHours, 0.001)
        assertEquals(75.0, PetTier.B.stageDurationHours, 0.001)
        assertEquals(50.0, PetTier.C.stageDurationHours, 0.001)
        assertEquals(50.0, PetTier.D.stageDurationHours, 0.001)
        assertEquals(20.0, PetTier.E.stageDurationHours, 0.001)
        assertEquals(10.0, PetTier.F.stageDurationHours, 0.001)
        // Total F-to-A ramp-up hours = 10 + 20 + 50 + 50 + 75 = 205h
        assertEquals(205.0, PetTier.TOTAL_F_TO_A_RAMP_HOURS, 0.001)
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
            com.example.util.AlarmActionItem(
                hoursFromNow = 12.1,
                label = "House #5: Grade B → A Ready",
                categoryKey = "tier_B_to_A"
            ),
            com.example.util.AlarmActionItem(
                hoursFromNow = 15.0,
                label = "House #6: Grade C → B Ready",
                categoryKey = "tier_C_to_B"
            )
        )

        val consolidated = com.example.util.AlarmHelper.consolidateAlarmItems(raw)
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
