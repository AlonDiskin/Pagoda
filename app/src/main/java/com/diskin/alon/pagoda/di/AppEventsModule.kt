package com.diskin.alon.pagoda.di

import com.diskin.alon.pagoda.common.eventcontracts.AppEventProvider
import com.diskin.alon.pagoda.common.eventcontracts.AppEventPublisher
import com.diskin.alon.pagoda.common.eventcontracts.settings.TemperatureUnitPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.WindSpeedUnitPref
import com.diskin.alon.pagoda.common.events.TemperatureUnitPrefEventHandler
import com.diskin.alon.pagoda.common.events.TimeFormatPrefEventHandler
import com.diskin.alon.pagoda.common.events.WindSpeedUnitPrefEventHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppEventsModule {

    @Singleton
    @Binds
    abstract fun bindTempUnitPrefProvider(provider: TemperatureUnitPrefEventHandler): AppEventProvider<TemperatureUnitPref>

    @Singleton
    @Binds
    abstract fun bindTempUnitPrefPublisher(provider: TemperatureUnitPrefEventHandler): AppEventPublisher<TemperatureUnitPref>

    @Singleton
    @Binds
    abstract fun bindWindSpeedUnitPrefProvider(provider: WindSpeedUnitPrefEventHandler): AppEventProvider<WindSpeedUnitPref>

    @Singleton
    @Binds
    abstract fun bindWindSpeedUnitPrefPublisher(provider: WindSpeedUnitPrefEventHandler): AppEventPublisher<WindSpeedUnitPref>

    @Singleton
    @Binds
    abstract fun bindTimeFormatPrefProvider(provider: TimeFormatPrefEventHandler): AppEventProvider<TimeFormatPref>

    @Singleton
    @Binds
    abstract fun bindTimeFormatPrefPublisher(provider: TimeFormatPrefEventHandler): AppEventPublisher<TimeFormatPref>
}