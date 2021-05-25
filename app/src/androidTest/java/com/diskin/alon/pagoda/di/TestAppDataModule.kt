package com.diskin.alon.pagoda.di

import android.app.Application
import androidx.room.Room
import com.diskin.alon.pagoda.locations.data.local.BookmarkedLocationDao
import com.diskin.alon.pagoda.locations.data.local.LocationDao
import com.diskin.alon.pagoda.util.TestDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestAppDataModule {

    @Singleton
    @Provides
    fun provideDatabase(app: Application): TestDatabase {
        return Room.inMemoryDatabaseBuilder(
            app, TestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Singleton
    @Provides
    fun provideLocationsDao(database: TestDatabase): LocationDao {
        return database.locationDao()
    }

    @Singleton
    @Provides
    fun provideBookmarkedLocationsDao(database: TestDatabase): BookmarkedLocationDao {
        return database.bookmarkedDao()
    }
}