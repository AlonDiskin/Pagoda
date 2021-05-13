package com.diskin.alon.pagoda.weatherinfo.di

import com.diskin.alon.pagoda.common.util.Mapper2
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.WeatherRepository
import com.diskin.alon.pagoda.weatherinfo.data.implementations.WeatherRepositoryImpl
import com.diskin.alon.pagoda.weatherinfo.data.model.ApiLocationResponse
import com.diskin.alon.pagoda.weatherinfo.data.model.ApiWeatherResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.RemoteWeatherMapper
import com.diskin.alon.pagoda.weatherinfo.data.remote.RemoteWeatherStore
import com.diskin.alon.pagoda.weatherinfo.data.remote.RemoteWeatherStoreImpl
import com.diskin.alon.pagoda.weatherinfo.domain.LocationWeather
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WeatherDataModule {

    @Singleton
    @Binds
    abstract fun bindRemoteWeatherStore(store: RemoteWeatherStoreImpl): RemoteWeatherStore

    @Singleton
    @Binds
    abstract fun bindRemoteWeatherMapper(mapper: RemoteWeatherMapper): Mapper2<ApiWeatherResponse, ApiLocationResponse, LocationWeather>

    @Singleton
    @Binds
    abstract fun bindWeatherRepository(repository: WeatherRepositoryImpl): WeatherRepository
}