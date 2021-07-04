package com.diskin.alon.pagoda.weatherinfo.featuretesting.di

import com.diskin.alon.pagoda.common.eventcontracts.AppEventProvider
import com.diskin.alon.pagoda.common.eventcontracts.settings.TemperatureUnitPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.WindSpeedUnitPref
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
    fun provideTemperatureUnitPrepProvider(): AppEventProvider<TemperatureUnitPref> {
        return mockk()
    }

    @Singleton
    @Provides
    fun provideWindSpeedUnitPrepProvider(): AppEventProvider<WindSpeedUnitPref> {
        return mockk()
    }

    @Singleton
    @Provides
    fun provideTimeFormatPrepProvider(): AppEventProvider<TimeFormatPref> {
        return mockk()
    }
}