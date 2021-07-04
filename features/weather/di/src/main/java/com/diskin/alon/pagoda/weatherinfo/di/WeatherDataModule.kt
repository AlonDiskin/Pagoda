package com.diskin.alon.pagoda.weatherinfo.di

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.common.util.Mapper2
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.WeatherRepository
import com.diskin.alon.pagoda.weatherinfo.data.implementations.WeatherRepositoryImpl
import com.diskin.alon.pagoda.weatherinfo.data.local.implementations.WeatherCacheImpl
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.WeatherCache
import com.diskin.alon.pagoda.weatherinfo.data.local.model.CurrentWeatherEntity
import com.diskin.alon.pagoda.weatherinfo.data.local.util.CurrentWeatherEntityMapper
import com.diskin.alon.pagoda.weatherinfo.data.local.util.LocalWeatherMapper
import com.diskin.alon.pagoda.weatherinfo.data.remote.implementations.WeatherStoreImpl
import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.WeatherStore
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiLocationResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiWeatherResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.util.RemoteWeatherMapper
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
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
    abstract fun bindWeatherRepository(repository: WeatherRepositoryImpl): WeatherRepository

    @Singleton
    @Binds
    abstract fun bindWeatherStore(store: WeatherStoreImpl): WeatherStore

    @Singleton
    @Binds
    abstract fun bindRemoteWeatherMapper(mapper: RemoteWeatherMapper): Mapper2<ApiWeatherResponse, ApiLocationResponse, Weather>

    @Singleton
    @Binds
    abstract fun bindWeatherCache(cache: WeatherCacheImpl): WeatherCache

    @Singleton
    @Binds
    abstract fun bindCurrentWeatherEntityMapper(mapper: CurrentWeatherEntityMapper): Mapper<Weather, CurrentWeatherEntity>

    @Singleton
    @Binds
    abstract fun bindLocalWeatherMapper(mapper: LocalWeatherMapper): Mapper<CurrentWeatherEntity,Weather>
}