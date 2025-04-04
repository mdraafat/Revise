package com.raafat.revise

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Aya::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ayaDao(): AyaDao
}