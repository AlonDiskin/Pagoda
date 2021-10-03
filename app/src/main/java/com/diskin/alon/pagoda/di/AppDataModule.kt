package com.diskin.alon.pagoda.di

import android.app.Application
import androidx.room.Room
import com.diskin.alon.pagoda.AppDatabase
import com.diskin.alon.pagoda.MIGRATION_1_2
import com.diskin.alon.pagoda.MIGRATION_2_3
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.CurrentWeatherDao
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.LocationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppDataModule {

    @Singleton
    @Provides
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(app,
            AppDatabase::class.java, "pagoda-db")
            .createFromAsset("pagoda.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    @Singleton
    @Provides
    fun provideLocationDao(database: AppDatabase): LocationDao {
        return database.locationDao()
    }

    @Singleton
    @Provides
    fun provideCurrentWeatherDao(database: AppDatabase): CurrentWeatherDao {
        return database.currentWeatherDao()
    }
}