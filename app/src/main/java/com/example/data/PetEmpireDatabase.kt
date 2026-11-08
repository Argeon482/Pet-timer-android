package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [GlobalSettingsEntity::class, HouseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PetEmpireDatabase : RoomDatabase() {
    abstract fun petEmpireDao(): PetEmpireDao

    companion object {
        @Volatile
        private var INSTANCE: PetEmpireDatabase? = null

        fun getInstance(context: Context): PetEmpireDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PetEmpireDatabase::class.java,
                    "flyff_pet_empire.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
