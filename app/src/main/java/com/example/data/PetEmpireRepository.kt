package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PetEmpireRepository(private val dao: PetEmpireDao) {

    val globalSettingsFlow: Flow<GlobalSettingsEntity> = dao.observeGlobalSettings()
        .map { it ?: GlobalSettingsEntity() }

    val housesFlow: Flow<List<HouseEntity>> = dao.observeAllHouses()

    suspend fun ensureSeeded(nowEpochMs: Long = System.currentTimeMillis()) {
        if (dao.getGlobalSettingsOnce() == null) {
            dao.upsertGlobalSettings(GlobalSettingsEntity())
        }
        val existingHouses = dao.getAllHousesOnce()
        if (existingHouses.isEmpty()) {
            val initialHouses = (1..50).map { num ->
                when (num) {
                    // Houses 37 & 38 demonstrate finished pet overflow warning (Mac NPC still burning!)
                    37 -> HouseEntity(
                        houseNumber = num,
                        customLabel = "House #$num • Overflow Alert",
                        isSeededWithAPet = true,
                        slot1Running = true,
                        slot1AccumulatedHours = 256.5, // 6.5 hours overflow past 250h!
                        slot1LastStartedEpochMs = nowEpochMs,
                        slot2Tier = "A",
                        slot2Running = true,
                        slot2AccumulatedHours = 4.0, // 4.0 hours overflow on finished A-tier!
                        slot2LastStartedEpochMs = nowEpochMs,
                        slot3Tier = "B",
                        slot3Running = true,
                        slot3AccumulatedHours = 62.0,
                        slot3LastStartedEpochMs = nowEpochMs,
                        macFHoursUsed = 120.0,
                        macEHoursUsed = 140.0,
                        macDHoursUsed = 165.0,
                        macCHoursUsed = 190.0,
                        macBHoursUsed = 230.0,
                        macAHoursUsed = 256.5
                    )
                    38 -> HouseEntity(
                        houseNumber = num,
                        customLabel = "House #$num • Bake Complete",
                        isSeededWithAPet = true,
                        slot1Running = true,
                        slot1AccumulatedHours = 253.0, // 3.0h overflow
                        slot1LastStartedEpochMs = nowEpochMs,
                        slot2Tier = "B",
                        slot2Running = true,
                        slot2AccumulatedHours = 68.0,
                        slot2LastStartedEpochMs = nowEpochMs,
                        slot3Tier = "B",
                        slot3Running = true,
                        slot3AccumulatedHours = 64.0,
                        slot3LastStartedEpochMs = nowEpochMs,
                        macFHoursUsed = 115.0,
                        macEHoursUsed = 135.0,
                        macDHoursUsed = 160.0,
                        macCHoursUsed = 185.0,
                        macBHoursUsed = 220.0,
                        macAHoursUsed = 253.0
                    )
                    // Houses 39 & 40 demonstrate Mac NPC approaching 360-hour 15-day expiration
                    39, 40 -> HouseEntity(
                        houseNumber = num,
                        customLabel = "House #$num • Mac Expiring",
                        isSeededWithAPet = true,
                        slot1Running = true,
                        slot1AccumulatedHours = 212.0,
                        slot1LastStartedEpochMs = nowEpochMs,
                        slot2Tier = "B",
                        slot2Running = true,
                        slot2AccumulatedHours = 54.0,
                        slot2LastStartedEpochMs = nowEpochMs,
                        slot3Tier = "C",
                        slot3Running = true,
                        slot3AccumulatedHours = 49.0,
                        slot3LastStartedEpochMs = nowEpochMs,
                        macFHoursUsed = 280.0,
                        macEHoursUsed = 310.0,
                        macDHoursUsed = 325.0,
                        macCHoursUsed = 334.0,
                        macBHoursUsed = 342.0, // Only 18h left on 360h Mac B!
                        macAHoursUsed = 346.0  // Only 14h left on 360h Mac A!
                    )
                    else -> {
                        val staggeredHours = 180.0 + ((num % 6) * 8.0)
                        HouseEntity(
                            houseNumber = num,
                            customLabel = "House #$num",
                            isSeededWithAPet = true,
                            slot1Running = true,
                            slot1AccumulatedHours = staggeredHours,
                            slot1LastStartedEpochMs = nowEpochMs,
                            slot2Tier = if (num % 2 == 0) "B" else "C",
                            slot2Running = true,
                            slot2AccumulatedHours = 42.0 + (num % 5) * 4.0,
                            slot2LastStartedEpochMs = nowEpochMs,
                            slot3Tier = if (num % 3 == 0) "B" else "D",
                            slot3Running = true,
                            slot3AccumulatedHours = 28.0 + (num % 4) * 3.0,
                            slot3LastStartedEpochMs = nowEpochMs,
                            macFHoursUsed = 85.0 + (num % 10) * 5.0,
                            macEHoursUsed = 110.0 + (num % 10) * 5.0,
                            macDHoursUsed = 135.0 + (num % 10) * 5.0,
                            macCHoursUsed = 160.0 + (num % 10) * 5.0,
                            macBHoursUsed = 185.0 + (num % 10) * 5.0,
                            macAHoursUsed = staggeredHours
                        )
                    }
                }
            }
            dao.upsertHouses(initialHouses)
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
