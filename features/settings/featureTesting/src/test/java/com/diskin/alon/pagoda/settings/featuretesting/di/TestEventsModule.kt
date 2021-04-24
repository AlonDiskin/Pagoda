package com.diskin.alon.pagoda.settings.featuretesting.di

import com.diskin.alon.pagoda.common.eventcontracts.AppEventPublisher
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
    fun provideTemperatureUnitPrepProvider(): AppEventPublisher<TemperatureUnitPref> {
        return mockk()
    }

    @Singleton
    @Provides
    fun provideWindSpeedUnitPrepProvider(): AppEventPublisher<WindSpeedUnitPref> {
        return mockk()
    }

    @Singleton
    @Provides
    fun provideTimeFormatPrepProvider(): AppEventPublisher<TimeFormatPref> {
        return mockk()
    }
}