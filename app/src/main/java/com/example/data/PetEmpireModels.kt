package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PetTier(
    val code: String,
    val displayName: String,
    val stageDurationHours: Double,
    val cumulativeHoursToReach: Double
) {
    F("F", "Grade F (Base)", 10.0, 0.0),
    E("E", "Grade E", 20.0, 10.0),
    D("D", "Grade D", 50.0, 30.0),
    C("C", "Grade C", 50.0, 80.0),
    B("B", "Grade B", 75.0, 130.0),
    A("A", "Grade A (Bake Ready)", 250.0, 205.0);

    fun nextTier(): PetTier = when (this) {
        F -> E
        E -> D
        D -> C
        C -> B
        B -> A
        A -> A
    }

    companion object {
        const val TOTAL_F_TO_A_RAMP_HOURS = 205.0
        const val S_TIER_BAKE_HOURS = 250.0

        fun fromCode(code: String): PetTier =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: F
    }
}

@Entity(tableName = "global_settings")
data class GlobalSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val targetHouseCount: Int = 40, // Default: 40, Range: 1..50
    val sTierSellPrice: Long = 220_000_000L, // Default: 220,000,000 Penya
    val aTierSellPrice: Long = 120_000_000L, // Default: 120,000,000 Penya
    val fCoinExchangeRatePer1000: Long = 500_000_000L, // Default: 500M Penya per 1,000 fCoins (1 fCoin = 500k Penya)
    val penyaToUsdPer200M: Double = 3.09, // Default: $3.09 per 200,000,000 Penya
    val houseDeedCost: Long = 100_000_000L, // Default: 100,000,000 Penya
    val baseFPetCost: Long = 2_000_000L, // Default: 2,000,000 Penya
    val macCostFCoins: Int = 140, // Default: 140 fCoins per grade (F, E, D, C, B, A)
    val macMaxActiveHours: Double = 360.0, // Default: 360 hours (15 days)
    val accumulatedProfitsPenya: Long = 1_900_000_000L, // Current accumulated cycle profits for expansion calculator
    val currentOwnedHousesForPlanner: Int = 20, // Expansion planner current house count
    val targetExpansionHouses: Int = 40, // Expansion planner target house count
    val availableAPetSeeds: Int = 20, // Spare A-tier pets ready for Slot 1 seeding on Harvest Day
    val currentCycleHour: Double = 0.0 // Current empire cycle hour (0..250h) for Harvest Day Sync check
)

@Entity(tableName = "houses")
data class HouseEntity(
    @PrimaryKey val houseNumber: Int, // 1..50
    val customLabel: String = "House #$houseNumber",
    val isSeededWithAPet: Boolean = true,

    // Slot 1: The S-Tier Bake (Mac Grade A, 250-hour target)
    val slot1Running: Boolean = true,
    val slot1AccumulatedHours: Double = 180.0,
    val slot1LastStartedEpochMs: Long? = null,

    // Slot 2: A-Tier Pipeline 1 (Default B)
    val slot2Tier: String = "B",
    val slot2Running: Boolean = true,
    val slot2AccumulatedHours: Double = 45.0,
    val slot2LastStartedEpochMs: Long? = null,

    // Slot 3: A-Tier Pipeline 2 (Default C)
    val slot3Tier: String = "C",
    val slot3Running: Boolean = true,
    val slot3AccumulatedHours: Double = 30.0,
    val slot3LastStartedEpochMs: Long? = null,

    // Physical 360-hour cash shop timers per Mac NPC grade (F, E, D, C, B, A) - Default 0.0 used (360.0h remaining)
    val macFHoursUsed: Double = 0.0,
    val macEHoursUsed: Double = 0.0,
    val macDHoursUsed: Double = 0.0,
    val macCHoursUsed: Double = 0.0,
    val macBHoursUsed: Double = 0.0,
    val macAHoursUsed: Double = 0.0
)
