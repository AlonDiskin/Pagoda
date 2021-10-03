package com.diskin.alon.pagoda.weatherinfo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.CurrentWeatherDao
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.LocationDao
import com.diskin.alon.pagoda.weatherinfo.data.local.model.CurrentWeatherEntity
import com.diskin.alon.pagoda.weatherinfo.data.local.model.LocationEntity
import com.diskin.alon.pagoda.weatherinfo.data.local.util.Converters

@Database(entities = [CurrentWeatherEntity::class, LocationEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TestDatabase : RoomDatabase(){

    abstract fun currentWeatherDao(): CurrentWeatherDao

    abstract fun locationDao(): LocationDao
}