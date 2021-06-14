package com.diskin.alon.pagoda.util

import androidx.room.Database
import androidx.room.RoomDatabase
import com.diskin.alon.pagoda.locations.data.local.LocationDao
import com.diskin.alon.pagoda.locations.data.local.LocationEntity

@Database(entities = [LocationEntity::class], version = 1, exportSchema = false)
abstract class TestDatabase : RoomDatabase(){

    abstract fun locationDao(): LocationDao
}