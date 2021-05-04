package com.diskin.alon.pagoda.settings.di

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.settings.data.implementation.WeatherAlertMapper
import com.diskin.alon.pagoda.settings.data.implementation.WeatherAlertProviderImpl
import com.diskin.alon.pagoda.settings.data.local.CurrentLocationProvider
import com.diskin.alon.pagoda.settings.data.local.CurrentLocationProviderImpl
import com.diskin.alon.pagoda.settings.data.model.ApiWeatherAlertResponse
import com.diskin.alon.pagoda.settings.infrastructure.interfaces.WeatherAlertProvider
import com.diskin.alon.pagoda.settings.infrastructure.model.WeatherAlert
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsDataModule {

    @Singleton
    @Binds
    abstract fun bindWeatherAlertProvider(provider: WeatherAlertProviderImpl): WeatherAlertProvider

    @Singleton
    @Binds
    abstract fun bindCurrentLocationProvider(provider: CurrentLocationProviderImpl): CurrentLocationProvider

    @Singleton
    @Binds
    abstract fun bindWeatherAlertMapper(mapper: WeatherAlertMapper): Mapper<ApiWeatherAlertResponse, WeatherAlert>
}