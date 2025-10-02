package com.example.butterflydetector.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ButterflyEntity::class], version = 2, exportSchema = false)  // Updated version to 2 for isFavorite field
abstract class ButterflyDatabase : RoomDatabase() {

    abstract fun butterflyDao(): ButterflyDao

    companion object {
        @Volatile
        private var INSTANCE: ButterflyDatabase? = null

        fun getDatabase(context: Context): ButterflyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ButterflyDatabase::class.java,
                    "butterfly_database"
                )
                    .fallbackToDestructiveMigration()  // Added fallback for version changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
