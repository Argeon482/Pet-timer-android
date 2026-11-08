package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PetEmpireDao {
    @Query("SELECT * FROM global_settings WHERE id = 1")
    fun observeGlobalSettings(): Flow<GlobalSettingsEntity?>

    @Query("SELECT * FROM global_settings WHERE id = 1")
    suspend fun getGlobalSettingsOnce(): GlobalSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGlobalSettings(settings: GlobalSettingsEntity)

    @Query("SELECT * FROM houses ORDER BY houseNumber ASC")
    fun observeAllHouses(): Flow<List<HouseEntity>>

    @Query("SELECT * FROM houses ORDER BY houseNumber ASC")
    suspend fun getAllHousesOnce(): List<HouseEntity>

    @Query("SELECT * FROM houses WHERE houseNumber = :houseNumber")
    suspend fun getHouseByNumber(houseNumber: Int): HouseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHouse(house: HouseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHouses(houses: List<HouseEntity>)

    @Update
    suspend fun updateHouse(house: HouseEntity)
}
