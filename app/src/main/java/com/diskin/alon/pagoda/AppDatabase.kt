package com.diskin.alon.pagoda

import androidx.room.Database
import androidx.room.RoomDatabase
import com.diskin.alon.pagoda.locations.data.local.LocationDao
import com.diskin.alon.pagoda.locations.data.local.LocationEntity

@Database(entities = [LocationEntity::class],
    version = 3,
    exportSchema = true)
abstract class AppDatabase : RoomDatabase(){

    abstract fun locationDao(): LocationDao
}