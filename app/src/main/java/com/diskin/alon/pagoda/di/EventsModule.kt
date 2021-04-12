package com.diskin.alon.pagoda.di

import com.diskin.alon.pagoda.common.events.WeatherUnitsEventHandler
import com.diskin.alon.pagoda.common.events.WeatherUnitsEventProvider
import com.diskin.alon.pagoda.common.events.WeatherUnitsEventPublisher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EventsModule {

    @Singleton
    @Binds
    abstract fun bindUnitsPrefProvider(handler: WeatherUnitsEventHandler): WeatherUnitsEventProvider

    @Singleton
    @Binds
    abstract fun bindUnitsPrefPublisher(handler: WeatherUnitsEventHandler): WeatherUnitsEventPublisher
}