package com.diskin.alon.pagoda

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.CurrentWeatherDao
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.LocationDao
import com.diskin.alon.pagoda.weatherinfo.data.local.model.CurrentWeatherEntity
import com.diskin.alon.pagoda.weatherinfo.data.local.model.LocationEntity
import com.diskin.alon.pagoda.weatherinfo.data.local.util.Converters

@Database(entities = [LocationEntity::class,CurrentWeatherEntity::class],
    version = 4,
    autoMigrations = [AutoMigration(from = 3, to = 4)],
    exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase(){

    abstract fun locationDao(): LocationDao

    abstract fun currentWeatherDao(): CurrentWeatherDao
}