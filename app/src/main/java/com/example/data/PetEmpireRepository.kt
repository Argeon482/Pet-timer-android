package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PetEmpireRepository(private val dao: PetEmpireDao) {

    val globalSettingsFlow: Flow<GlobalSettingsEntity> = dao.observeGlobalSettings()
        .map { it ?: GlobalSettingsEntity() }

    val housesFlow: Flow<List<HouseEntity>> = dao.observeAllHouses()

    suspend fun ensureSeeded(nowEpochMs: Long = System.currentTimeMillis()) {
        val existingSettings = dao.getGlobalSettingsOnce()
        if (existingSettings == null) {
            dao.upsertGlobalSettings(GlobalSettingsEntity())
        } else if (existingSettings.sTierSellPrice == 120_000_000L && existingSettings.aTierSellPrice == 65_000_000L) {
            // Automatically migrate to user's updated 220M S-Tier and 120M A-Tier defaults
            dao.upsertGlobalSettings(
                existingSettings.copy(
                    sTierSellPrice = 220_000_000L,
                    aTierSellPrice = 120_000_000L
                )
            )
        }

        val existingHouses = dao.getAllHousesOnce()
        if (existingHouses.isEmpty()) {
            val initialHouses = (1..50).map { num ->
                HouseEntity(
                    houseNumber = num,
                    customLabel = "House #$num",
                    isSeededWithAPet = true,
                    // Slot 1: Grade A (250h S-tier bake)
                    slot1Running = true,
                    slot1AccumulatedHours = 0.0,
                    slot1LastStartedEpochMs = nowEpochMs,
                    // Slot 2: Grade B (75h)
                    slot2Tier = "B",
                    slot2Running = true,
                    slot2AccumulatedHours = 0.0,
                    slot2LastStartedEpochMs = nowEpochMs,
                    // Slot 3: Grade C (50h)
                    slot3Tier = "C",
                    slot3Running = true,
                    slot3AccumulatedHours = 0.0,
                    slot3LastStartedEpochMs = nowEpochMs,
                    // Physical 360-hour Mac NPC replacement timers (0h used = 360h remaining default)
                    macFHoursUsed = 0.0,
                    macEHoursUsed = 0.0,
                    macDHoursUsed = 0.0,
                    macCHoursUsed = 0.0,
                    macBHoursUsed = 0.0,
                    macAHoursUsed = 0.0
                )
            }
            dao.upsertHouses(initialHouses)
        } else {
            // Clean up any legacy houses that had duplicate tiers in the same house
            var needsCleanup = false
            val cleaned = existingHouses.map { house ->
                if (house.slot2Tier == house.slot3Tier || house.slot2Tier == "A" || house.slot3Tier == "A") {
                    needsCleanup = true
                    house.copy(slot2Tier = "B", slot3Tier = "C")
                } else {
                    house
                }
            }
            if (needsCleanup) {
                dao.upsertHouses(cleaned)
            }
        }
    }

    suspend fun updateGlobalSettings(transform: (GlobalSettingsEntity) -> GlobalSettingsEntity) {
        val current = dao.getGlobalSettingsOnce() ?: GlobalSettingsEntity()
        dao.upsertGlobalSettings(transform(current))
    }

    suspend fun resetGlobalSettingsToDefaults() {
        dao.upsertGlobalSettings(GlobalSettingsEntity())
    }

    suspend fun updateHouse(houseNumber: Int, transform: (HouseEntity) -> HouseEntity) {
        val current = dao.getHouseByNumber(houseNumber) ?: return
        dao.upsertHouse(transform(current))
    }

    suspend fun updateAllHouses(transform: (HouseEntity) -> HouseEntity) {
        val all = dao.getAllHousesOnce()
        dao.upsertHouses(all.map(transform))
    }
}
