package com.example.data

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.floor
import kotlin.math.max

data class SlotComputedStatus(
    val slotIndex: Int,
    val title: String,
    val tier: PetTier,
    val isRunning: Boolean,
    val elapsedHours: Double,
    val requiredHours: Double,
    val progressFraction: Float,
    val remainingHours: Double,
    val isFinished: Boolean,
    val overflowHours: Double, // Wasted hours burned past 100% while still running
    val wastedFCoins: Double,
    val wastedPenya: Long,
    val pipelineOverallProgress: Float // For Slots 2 & 3 (out of 205h F->A) or Slot 1 (out of 250h A->S)
)

data class MacGradeTimerStatus(
    val grade: PetTier,
    val hoursUsed: Double,
    val maxHours: Double,
    val hoursRemaining: Double,
    val usageFraction: Float,
    val isExpiringSoon: Boolean, // < 30h remaining
    val isExpired: Boolean,
    val isCurrentlyTraining: Boolean = false
)

data class HouseComputedStatus(
    val entity: HouseEntity,
    val slot1: SlotComputedStatus,
    val slot2: SlotComputedStatus,
    val slot3: SlotComputedStatus,
    val macTimers: List<MacGradeTimerStatus>,
    val totalOverflowHours: Double,
    val totalWastedPenya: Long,
    val totalWastedFCoins: Double,
    val hasOverflowWarning: Boolean,
    val hasExpiryWarning: Boolean,
    val activeRunningSlotsCount: Int,
    val nextActionHoursRemaining: Double?,
    val nextActionDescription: String
)

data class EmpireFinancialSummary(
    val activeHouseCount: Int,
    val penyaPerFCoin: Long,
    val macNpcCostPenya: Long, // 140 fCoins * penyaPerFCoin (70M at default)
    val macFullSet6CostPenya: Long, // 6 grades * macNpcCostPenya (420M at default)
    val macHourlyBurnPenya: Double, // macNpcCostPenya / 360.0

    // Single House / 250h Cycle ("Peace of Mind" cycle: 1 S-tier + 1 extra A-tier sold)
    val grossRevenuePerHouseCycle: Long, // Default: 185,000,000 Penya
    val macBurnCostPerHouseCycle: Long, // Default: 131,250,000 Penya (675h active Mac burn across F..A)
    val fPetCostPerHouseCycle: Long, // Default: 6,250,000 Penya (3.125 base F-pets amortized per cycle)
    val operatingCostPerHouseCycle: Long, // Default: 137,500,000 Penya
    val netProfitPerHouseCycle: Long, // Default: 47,500,000 Penya
    val dailyNetProfitPerHouse: Long, // Default: 4,560,000 Penya / day

    // Empire Totals (Scaled by activeHouseCount, Default 40 Houses)
    val empireGrossRevenueCycle: Long, // Default: 7.40B Penya
    val empireOperatingCostCycle: Long, // Default: 5.50B Penya
    val empireNetProfitCycle: Long, // Default: 1.90B Penya
    val empireDailyNetProfit: Long, // Default: 182.4M Penya / day
    val empireMonthlyNetProfit: Long, // Default: 5.472B Penya / 30 days
    val empireFCoinsBurnedPerCycle: Double, // 262.5 fCoins/house * 40 = 10,500 fCoins/cycle
    val empireActiveHourlyFCoinBurn: Double, // Live burn rate based on currently running slots

    // Live Wasted Timer Penalties across all active houses
    val totalOverflowHoursAcrossEmpire: Double,
    val totalWastedFCoinsAcrossEmpire: Double,
    val totalWastedPenyaAcrossEmpire: Long,
    val housesWithWarningsCount: Int,

    // Dynamic Expansion Power Calculator
    val newHouseSetupCostPenya: Long, // 1 House Deed + 6 Mac NPCs (F..A) + 2 Base F-Pets
    val newHouseDeedPlusCoreMacCostPenya: Long, // 1 House Deed + 6 Mac NPCs (100M + 420M = 520M at default)
    val affordableNewHousesNow: Int,
    val leftoverPenyaAfterExpansion: Long,
    val penyaNeededForNextHouse: Long
)

data class ExpansionPlanEvaluation(
    val currentHouses: Int,
    val targetHouses: Int,
    val additionalHousesNeeded: Int,
    val totalDeedsPenya: Long,
    val totalMacSetsPenya: Long,
    val totalMacSetsFCoins: Int,
    val totalBaseFPetsPenya: Long,
    val totalCapitalPenya: Long,
    val totalDeedsUsd: Double,
    val totalMacSetsUsd: Double,
    val totalCapitalUsd: Double,
    val isCycleAtHourZero: Boolean,
    val hoursUntilNextHarvestSync: Double,
    val availableAPetSeeds: Int,
    val seedsShortfall: Int,
    val isFullySeededReady: Boolean,
    val rampUpHoursSavedBySeeding: Double,
    val unseededPenaltyHours: Double
)

object CalculationEngine {
    const val PEACE_OF_MIND_CYCLE_HOURS = 250.0
    const val PEACE_OF_MIND_CYCLE_DAYS = PEACE_OF_MIND_CYCLE_HOURS / 24.0 // 10.416667 days
    const val ACTIVE_MAC_BURN_HOURS_PER_HOUSE_CYCLE = 675.0 // 1.875 x 360h Mac timers burned across F..A per 250h cycle
    const val F_PET_CONSUMPTION_PER_HOUSE_CYCLE = 3.125 // Base F-tier pets consumed per 250h cycle

    fun computePenyaPerFCoin(settings: GlobalSettingsEntity): Long {
        return (settings.fCoinExchangeRatePer1000 / 1000L).coerceAtLeast(1L)
    }

    fun penyaToUsd(penya: Long, settings: GlobalSettingsEntity): Double {
        return (penya.toDouble() / 200_000_000.0) * settings.penyaToUsdPer200M
    }

    fun computeHouseStatus(
        house: HouseEntity,
        settings: GlobalSettingsEntity,
        nowEpochMs: Long
    ): HouseComputedStatus {
        val penyaPerFCoin = computePenyaPerFCoin(settings)
        val maxMacHours = settings.macMaxActiveHours.coerceAtLeast(1.0)
        val fCoinsPerMacHour = settings.macCostFCoins.toDouble() / maxMacHours
        val penyaPerMacHour = fCoinsPerMacHour * penyaPerFCoin.toDouble()

        fun liveHours(accumulated: Double, running: Boolean, startedMs: Long?): Double {
            if (!running || startedMs == null) return accumulated.coerceAtLeast(0.0)
            val deltaHours = ((nowEpochMs - startedMs).coerceAtLeast(0L)).toDouble() / 3_600_000.0
            return (accumulated + deltaHours).coerceAtLeast(0.0)
        }

        // Slot 1: S-Tier Bake (Grade A Mac NPC, 250h required)
        val s1Elapsed = liveHours(house.slot1AccumulatedHours, house.slot1Running, house.slot1LastStartedEpochMs)
        val s1Required = PetTier.S_TIER_BAKE_HOURS
        val s1Overflow = if (s1Elapsed > s1Required && house.slot1Running) s1Elapsed - s1Required else 0.0
        val s1WastedFCoins = s1Overflow * fCoinsPerMacHour
        val s1WastedPenya = (s1Overflow * penyaPerMacHour).toLong()
        val slot1Status = SlotComputedStatus(
            slotIndex = 1,
            title = "Slot 1 • S-Tier Bake (Mac A)",
            tier = PetTier.A,
            isRunning = house.slot1Running,
            elapsedHours = s1Elapsed,
            requiredHours = s1Required,
            progressFraction = (s1Elapsed / s1Required).toFloat().coerceIn(0f, 1f),
            remainingHours = max(0.0, s1Required - s1Elapsed),
            isFinished = s1Elapsed >= s1Required,
            overflowHours = s1Overflow,
            wastedFCoins = s1WastedFCoins,
            wastedPenya = s1WastedPenya,
            pipelineOverallProgress = (s1Elapsed / s1Required).toFloat().coerceIn(0f, 1f)
        )

        // Slot 2: A-Tier Pipeline
        val s2Tier = PetTier.fromCode(house.slot2Tier)
        val s2Elapsed = liveHours(house.slot2AccumulatedHours, house.slot2Running, house.slot2LastStartedEpochMs)
        val s2Required = if (s2Tier == PetTier.A) 0.0 else s2Tier.stageDurationHours
        val s2Overflow = if (s2Tier == PetTier.A && house.slot2Running) {
            s2Elapsed
        } else if (s2Elapsed > s2Required && house.slot2Running) {
            s2Elapsed - s2Required
        } else {
            0.0
        }
        val s2Cumulative = if (s2Tier == PetTier.A) {
            PetTier.TOTAL_F_TO_A_RAMP_HOURS
        } else {
            (s2Tier.cumulativeHoursToReach + s2Elapsed.coerceAtMost(s2Required))
        }
        val slot2Status = SlotComputedStatus(
            slotIndex = 2,
            title = "Slot 2 • A-Tier Pipeline",
            tier = s2Tier,
            isRunning = house.slot2Running,
            elapsedHours = s2Elapsed,
            requiredHours = s2Required,
            progressFraction = if (s2Required <= 0.0) 1f else (s2Elapsed / s2Required).toFloat().coerceIn(0f, 1f),
            remainingHours = max(0.0, s2Required - s2Elapsed),
            isFinished = s2Tier == PetTier.A || s2Elapsed >= s2Required,
            overflowHours = s2Overflow,
            wastedFCoins = s2Overflow * fCoinsPerMacHour,
            wastedPenya = (s2Overflow * penyaPerMacHour).toLong(),
            pipelineOverallProgress = (s2Cumulative / PetTier.TOTAL_F_TO_A_RAMP_HOURS).toFloat().coerceIn(0f, 1f)
        )

        // Slot 3: A-Tier Pipeline
        val s3Tier = PetTier.fromCode(house.slot3Tier)
        val s3Elapsed = liveHours(house.slot3AccumulatedHours, house.slot3Running, house.slot3LastStartedEpochMs)
        val s3Required = if (s3Tier == PetTier.A) 0.0 else s3Tier.stageDurationHours
        val s3Overflow = if (s3Tier == PetTier.A && house.slot3Running) {
            s3Elapsed
        } else if (s3Elapsed > s3Required && house.slot3Running) {
            s3Elapsed - s3Required
        } else {
            0.0
        }
        val s3Cumulative = if (s3Tier == PetTier.A) {
            PetTier.TOTAL_F_TO_A_RAMP_HOURS
        } else {
            (s3Tier.cumulativeHoursToReach + s3Elapsed.coerceAtMost(s3Required))
        }
        val slot3Status = SlotComputedStatus(
            slotIndex = 3,
            title = "Slot 3 • A-Tier Pipeline",
            tier = s3Tier,
            isRunning = house.slot3Running,
            elapsedHours = s3Elapsed,
            requiredHours = s3Required,
            progressFraction = if (s3Required <= 0.0) 1f else (s3Elapsed / s3Required).toFloat().coerceIn(0f, 1f),
            remainingHours = max(0.0, s3Required - s3Elapsed),
            isFinished = s3Tier == PetTier.A || s3Elapsed >= s3Required,
            overflowHours = s3Overflow,
            wastedFCoins = s3Overflow * fCoinsPerMacHour,
            wastedPenya = (s3Overflow * penyaPerMacHour).toLong(),
            pipelineOverallProgress = (s3Cumulative / PetTier.TOTAL_F_TO_A_RAMP_HOURS).toFloat().coerceIn(0f, 1f)
        )

        fun sessionHours(running: Boolean, startedMs: Long?): Double {
            if (!running || startedMs == null) return 0.0
            return ((nowEpochMs - startedMs).coerceAtLeast(0L)).toDouble() / 3_600_000.0
        }

        val s1Session = sessionHours(house.slot1Running, house.slot1LastStartedEpochMs)
        val s2Session = sessionHours(house.slot2Running, house.slot2LastStartedEpochMs)
        val s3Session = sessionHours(house.slot3Running, house.slot3LastStartedEpochMs)

        // Mac NPC Replacement Timers count down as that particular ranked pet is being trained.
        // When active/training it counts down; when inactive it doesn't.
        // It only counts as not training when stopped by the user, NOT when the pet finishes training.
        val isFTraining = (s2Tier == PetTier.F && house.slot2Running) || (s3Tier == PetTier.F && house.slot3Running)
        val isETraining = (s2Tier == PetTier.E && house.slot2Running) || (s3Tier == PetTier.E && house.slot3Running)
        val isDTraining = (s2Tier == PetTier.D && house.slot2Running) || (s3Tier == PetTier.D && house.slot3Running)
        val isCTraining = (s2Tier == PetTier.C && house.slot2Running) || (s3Tier == PetTier.C && house.slot3Running)
        val isBTraining = (s2Tier == PetTier.B && house.slot2Running) || (s3Tier == PetTier.B && house.slot3Running)
        val isATraining = house.slot1Running || (s2Tier == PetTier.A && house.slot2Running) || (s3Tier == PetTier.A && house.slot3Running)

        val liveMacFUsed = house.macFHoursUsed +
            (if (s2Tier == PetTier.F) s2Session else 0.0) +
            (if (s3Tier == PetTier.F) s3Session else 0.0)

        val liveMacEUsed = house.macEHoursUsed +
            (if (s2Tier == PetTier.E) s2Session else 0.0) +
            (if (s3Tier == PetTier.E) s3Session else 0.0)

        val liveMacDUsed = house.macDHoursUsed +
            (if (s2Tier == PetTier.D) s2Session else 0.0) +
            (if (s3Tier == PetTier.D) s3Session else 0.0)

        val liveMacCUsed = house.macCHoursUsed +
            (if (s2Tier == PetTier.C) s2Session else 0.0) +
            (if (s3Tier == PetTier.C) s3Session else 0.0)

        val liveMacBUsed = house.macBHoursUsed +
            (if (s2Tier == PetTier.B) s2Session else 0.0) +
            (if (s3Tier == PetTier.B) s3Session else 0.0)

        val liveMacAUsed = house.macAHoursUsed + s1Session +
            (if (s2Tier == PetTier.A) s2Session else 0.0) +
            (if (s3Tier == PetTier.A) s3Session else 0.0)

        val macGrades = listOf(
            Triple(PetTier.F, liveMacFUsed, isFTraining),
            Triple(PetTier.E, liveMacEUsed, isETraining),
            Triple(PetTier.D, liveMacDUsed, isDTraining),
            Triple(PetTier.C, liveMacCUsed, isCTraining),
            Triple(PetTier.B, liveMacBUsed, isBTraining),
            Triple(PetTier.A, liveMacAUsed, isATraining)
        ).map { (tier, used, isTraining) ->
            val clampedUsed = used.coerceAtLeast(0.0)
            val remaining = max(0.0, maxMacHours - clampedUsed)
            MacGradeTimerStatus(
                grade = tier,
                hoursUsed = clampedUsed,
                maxHours = maxMacHours,
                hoursRemaining = remaining,
                usageFraction = (clampedUsed / maxMacHours).toFloat().coerceIn(0f, 1f),
                isExpiringSoon = remaining in 0.01..30.0,
                isExpired = remaining <= 0.0,
                isCurrentlyTraining = isTraining
            )
        }

        val totalOverflow = slot1Status.overflowHours + slot2Status.overflowHours + slot3Status.overflowHours
        val totalWastedPenya = slot1Status.wastedPenya + slot2Status.wastedPenya + slot3Status.wastedPenya
        val totalWastedFCoins = slot1Status.wastedFCoins + slot2Status.wastedFCoins + slot3Status.wastedFCoins
        val hasOverflowWarning = totalOverflow > 0.01 ||
            (slot1Status.isFinished && slot1Status.isRunning) ||
            (slot2Status.isFinished && slot2Status.isRunning) ||
            (slot3Status.isFinished && slot3Status.isRunning)
        val hasExpiryWarning = macGrades.any { it.isExpiringSoon || it.isExpired }

        val runningSlots = listOf(slot1Status, slot2Status, slot3Status).filter { it.isRunning }
        val activeNeedAction = runningSlots.filter { it.remainingHours > 0.0 }.minByOrNull { it.remainingHours }

        val (nextHours, nextDesc) = when {
            hasOverflowWarning -> 0.0 to "PAUSE NOW: Finished pet burning Mac timer!"
            activeNeedAction != null -> activeNeedAction.remainingHours to
                "Slot ${activeNeedAction.slotIndex} (${activeNeedAction.tier.code}) finishes in ${formatHours(activeNeedAction.remainingHours)}"
            else -> null to "All slots paused or idle"
        }

        return HouseComputedStatus(
            entity = house,
            slot1 = slot1Status,
            slot2 = slot2Status,
            slot3 = slot3Status,
            macTimers = macGrades,
            totalOverflowHours = totalOverflow,
            totalWastedPenya = totalWastedPenya,
            totalWastedFCoins = totalWastedFCoins,
            hasOverflowWarning = hasOverflowWarning,
            hasExpiryWarning = hasExpiryWarning,
            activeRunningSlotsCount = runningSlots.size,
            nextActionHoursRemaining = nextHours,
            nextActionDescription = nextDesc
        )
    }

    fun computeEmpireSummary(
        settings: GlobalSettingsEntity,
        computedHouses: List<HouseComputedStatus>
    ): EmpireFinancialSummary {
        val activeHousesCount = settings.targetHouseCount.coerceIn(1, 50)
        val activeHouses = computedHouses.take(activeHousesCount)

        val penyaPerFCoin = computePenyaPerFCoin(settings)
        val maxMacHours = settings.macMaxActiveHours.coerceAtLeast(1.0)
        val macNpcCostPenya = settings.macCostFCoins.toLong() * penyaPerFCoin
        val macFullSet6CostPenya = macNpcCostPenya * 6L
        val macHourlyBurnPenya = macNpcCostPenya.toDouble() / maxMacHours

        // Per-House / 250h Cycle ("Peace of Mind" cycle)
        val grossRevenuePerHouse = settings.sTierSellPrice + settings.aTierSellPrice
        val macBurnCostPerHouse = ((ACTIVE_MAC_BURN_HOURS_PER_HOUSE_CYCLE / maxMacHours) *
            settings.macCostFCoins.toDouble() * penyaPerFCoin.toDouble()).toLong()
        val fPetCostPerHouse = (F_PET_CONSUMPTION_PER_HOUSE_CYCLE * settings.baseFPetCost.toDouble()).toLong()
        val operatingCostPerHouse = macBurnCostPerHouse + fPetCostPerHouse
        val netProfitPerHouse = grossRevenuePerHouse - operatingCostPerHouse
        val dailyNetProfitPerHouse = (netProfitPerHouse.toDouble() / PEACE_OF_MIND_CYCLE_DAYS).toLong()

        // Empire Totals
        val empireGross = grossRevenuePerHouse * activeHousesCount.toLong()
        val empireOperating = operatingCostPerHouse * activeHousesCount.toLong()
        val empireNet = netProfitPerHouse * activeHousesCount.toLong()
        val empireDailyNet = dailyNetProfitPerHouse * activeHousesCount.toLong()
        val empireMonthlyNet = empireDailyNet * 30L

        val fCoinsPerHouseCycle = (ACTIVE_MAC_BURN_HOURS_PER_HOUSE_CYCLE / maxMacHours) * settings.macCostFCoins.toDouble()
        val empireFCoinsPerCycle = fCoinsPerHouseCycle * activeHousesCount.toDouble()

        val totalRunningSlots = activeHouses.sumOf { it.activeRunningSlotsCount }
        val fCoinsPerSlotHour = settings.macCostFCoins.toDouble() / maxMacHours
        val activeHourlyFCoinBurn = totalRunningSlots.toDouble() * fCoinsPerSlotHour

        val totalOverflowHours = activeHouses.sumOf { it.totalOverflowHours }
        val totalWastedFCoins = activeHouses.sumOf { it.totalWastedFCoins }
        val totalWastedPenya = activeHouses.sumOf { it.totalWastedPenya }
        val warningsCount = activeHouses.count { it.hasOverflowWarning || it.hasExpiryWarning }

        // Dynamic Expansion Power Calculator:
        // Cost of 1 new House Deed + 6-Grade Mac NPC Set (F, E, D, C, B, A)
        val newHouseDeedPlusMacSet = (settings.houseDeedCost + macFullSet6CostPenya).coerceAtLeast(1L)
        val newHouseFullSetupWithFPets = newHouseDeedPlusMacSet + (2L * settings.baseFPetCost)
        val affordableHouses = floor(settings.accumulatedProfitsPenya.toDouble() / newHouseDeedPlusMacSet.toDouble())
            .toInt()
            .coerceAtLeast(0)
        val leftoverPenya = (settings.accumulatedProfitsPenya - (affordableHouses.toLong() * newHouseDeedPlusMacSet))
            .coerceAtLeast(0L)
        val penyaNeededForNext = (newHouseDeedPlusMacSet - leftoverPenya).coerceAtLeast(0L)

        return EmpireFinancialSummary(
            activeHouseCount = activeHousesCount,
            penyaPerFCoin = penyaPerFCoin,
            macNpcCostPenya = macNpcCostPenya,
            macFullSet6CostPenya = macFullSet6CostPenya,
            macHourlyBurnPenya = macHourlyBurnPenya,
            grossRevenuePerHouseCycle = grossRevenuePerHouse,
            macBurnCostPerHouseCycle = macBurnCostPerHouse,
            fPetCostPerHouseCycle = fPetCostPerHouse,
            operatingCostPerHouseCycle = operatingCostPerHouse,
            netProfitPerHouseCycle = netProfitPerHouse,
            dailyNetProfitPerHouse = dailyNetProfitPerHouse,
            empireGrossRevenueCycle = empireGross,
            empireOperatingCostCycle = empireOperating,
            empireNetProfitCycle = empireNet,
            empireDailyNetProfit = empireDailyNet,
            empireMonthlyNetProfit = empireMonthlyNet,
            empireFCoinsBurnedPerCycle = empireFCoinsPerCycle,
            empireActiveHourlyFCoinBurn = activeHourlyFCoinBurn,
            totalOverflowHoursAcrossEmpire = totalOverflowHours,
            totalWastedFCoinsAcrossEmpire = totalWastedFCoins,
            totalWastedPenyaAcrossEmpire = totalWastedPenya,
            housesWithWarningsCount = warningsCount,
            newHouseSetupCostPenya = newHouseFullSetupWithFPets,
            newHouseDeedPlusCoreMacCostPenya = newHouseDeedPlusMacSet,
            affordableNewHousesNow = affordableHouses,
            leftoverPenyaAfterExpansion = leftoverPenya,
            penyaNeededForNextHouse = penyaNeededForNext
        )
    }

    fun evaluateExpansionPlan(settings: GlobalSettingsEntity): ExpansionPlanEvaluation {
        val current = settings.currentOwnedHousesForPlanner.coerceIn(0, 50)
        val target = settings.targetExpansionHouses.coerceIn(1, 50)
        val delta = max(0, target - current)

        val penyaPerFCoin = computePenyaPerFCoin(settings)
        val macSet6CostPenya = 6L * settings.macCostFCoins.toLong() * penyaPerFCoin

        val totalDeedsPenya = delta.toLong() * settings.houseDeedCost
        val totalMacSetsPenya = delta.toLong() * macSet6CostPenya
        val totalMacSetsFCoins = delta * 6 * settings.macCostFCoins
        val totalBaseFPetsPenya = delta.toLong() * 2L * settings.baseFPetCost
        val totalCapitalPenya = totalDeedsPenya + totalMacSetsPenya + totalBaseFPetsPenya

        val isHourZero = settings.currentCycleHour <= 0.5 || settings.currentCycleHour >= 249.5
        val hoursUntilHarvest = if (isHourZero) 0.0 else max(0.0, PEACE_OF_MIND_CYCLE_HOURS - settings.currentCycleHour)

        val seedsAvailable = settings.availableAPetSeeds.coerceAtLeast(0)
        val shortfall = max(0, delta - seedsAvailable)
        val seededHousesCount = minOf(delta, seedsAvailable)
        val rampUpSavedHours = seededHousesCount * PetTier.TOTAL_F_TO_A_RAMP_HOURS
        val unseededPenaltyHours = shortfall * PetTier.TOTAL_F_TO_A_RAMP_HOURS

        return ExpansionPlanEvaluation(
            currentHouses = current,
            targetHouses = target,
            additionalHousesNeeded = delta,
            totalDeedsPenya = totalDeedsPenya,
            totalMacSetsPenya = totalMacSetsPenya,
            totalMacSetsFCoins = totalMacSetsFCoins,
            totalBaseFPetsPenya = totalBaseFPetsPenya,
            totalCapitalPenya = totalCapitalPenya,
            totalDeedsUsd = penyaToUsd(totalDeedsPenya, settings),
            totalMacSetsUsd = penyaToUsd(totalMacSetsPenya, settings),
            totalCapitalUsd = penyaToUsd(totalCapitalPenya, settings),
            isCycleAtHourZero = isHourZero,
            hoursUntilNextHarvestSync = hoursUntilHarvest,
            availableAPetSeeds = seedsAvailable,
            seedsShortfall = shortfall,
            isFullySeededReady = isHourZero && shortfall == 0,
            rampUpHoursSavedBySeeding = rampUpSavedHours,
            unseededPenaltyHours = unseededPenaltyHours
        )
    }

    fun formatPenyaCompact(penya: Long): String {
        val absVal = kotlin.math.abs(penya.toDouble())
        val sign = if (penya < 0) "-" else ""
        return when {
            absVal >= 1_000_000_000.0 -> String.format(Locale.US, "%s%.2fB Penya", sign, absVal / 1_000_000_000.0)
            absVal >= 1_000_000.0 -> String.format(Locale.US, "%s%.2fM Penya", sign, absVal / 1_000_000.0)
            absVal >= 1_000.0 -> String.format(Locale.US, "%s%.1fK Penya", sign, absVal / 1_000.0)
            else -> "$sign$penya Penya"
        }
    }

    fun formatPenyaExact(penya: Long): String {
        return NumberFormat.getIntegerInstance(Locale.US).format(penya) + " Penya"
    }

    fun formatUsd(usd: Double): String {
        return String.format(Locale.US, "$%,.2f", usd)
    }

    fun formatHours(hours: Double): String {
        val totalMinutes = (hours * 60.0).toInt()
        val h = totalMinutes / 60
        val m = kotlin.math.abs(totalMinutes % 60)
        return if (m == 0) "${h}h" else "${h}h ${m}m"
    }
}
