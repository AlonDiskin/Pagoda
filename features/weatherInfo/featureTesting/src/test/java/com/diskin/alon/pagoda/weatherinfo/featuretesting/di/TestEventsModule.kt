package com.diskin.alon.pagoda.weatherinfo.featuretesting.di

import com.diskin.alon.pagoda.common.events.WeatherUnitsEventProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mockk.mockk
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestEventsModule {

    @Singleton
    @Provides
    fun provideUnitPrefEventProvider(): WeatherUnitsEventProvider {
        return mockk()
    }
}