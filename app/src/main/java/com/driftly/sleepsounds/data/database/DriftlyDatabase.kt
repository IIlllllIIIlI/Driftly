package com.driftly.sleepsounds.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MixEntity::class], version = 1, exportSchema = false)
abstract class DriftlyDatabase : RoomDatabase() {
    abstract fun mixDao(): MixDao
}
