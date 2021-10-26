package com.diskin.alon.pagoda.weatherinfo.di

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.common.util.Mapper2
import com.diskin.alon.pagoda.settings.shared.TempUnit
import com.diskin.alon.pagoda.settings.shared.TimeFormat
import com.diskin.alon.pagoda.settings.shared.WindSpeedUnit
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.SettingsRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.WeatherRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.TimeFormatDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UnitSystemDto
import com.diskin.alon.pagoda.weatherinfo.data.implementations.LocationRepositoryImpl
import com.diskin.alon.pagoda.weatherinfo.data.implementations.SettingsRepositoryImpl
import com.diskin.alon.pagoda.weatherinfo.data.implementations.WeatherRepositoryImpl
import com.diskin.alon.pagoda.weatherinfo.data.local.implementations.WeatherCacheImpl
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.WeatherCache
import com.diskin.alon.pagoda.weatherinfo.data.local.model.CurrentWeatherEntity
import com.diskin.alon.pagoda.weatherinfo.data.local.model.LocationEntity
import com.diskin.alon.pagoda.weatherinfo.data.local.util.*
import com.diskin.alon.pagoda.weatherinfo.data.remote.implementations.WeatherAlertProviderImpl
import com.diskin.alon.pagoda.weatherinfo.data.remote.implementations.WeatherStoreImpl
import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.WeatherAlertProvider
import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.WeatherStore
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiLocationResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiWeatherAlertResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiWeatherResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.WeatherAlert
import com.diskin.alon.pagoda.weatherinfo.data.remote.util.RemoteWeatherMapper
import com.diskin.alon.pagoda.weatherinfo.data.remote.util.WeatherAlertMapper
import com.diskin.alon.pagoda.weatherinfo.domain.Location
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
    abstract fun bindWeatherAlertProvider(store: WeatherAlertProviderImpl): WeatherAlertProvider

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

    @Singleton
    @Binds
    abstract fun provideLocationRepository(repository: LocationRepositoryImpl): LocationRepository

    @Singleton
    @Binds
    abstract fun provideLocationMapper(mapper: LocationMapper): Mapper<PagingData<LocationEntity>, PagingData<Location>>

    @Singleton
    @Binds
    abstract fun provideWeatherAlertMapper(mapper: WeatherAlertMapper): Mapper<ApiWeatherAlertResponse, WeatherAlert>

    @Singleton
    @Binds
    abstract fun bindSettingsRepository(repo: SettingsRepositoryImpl): SettingsRepository

    @Singleton
    @Binds
    abstract fun bindTempUnitMapper(mapper: TempUnitMapper): Mapper<TempUnit, UnitSystemDto>

    @Singleton
    @Binds
    abstract fun bindWindSpeedUnitMapper(mapper: WindSpeedUnitMapper): Mapper<WindSpeedUnit,UnitSystemDto>

    @Singleton
    @Binds
    abstract fun bindTimeFormatMapper(mapper: TimeFormatMapper): Mapper<TimeFormat, TimeFormatDto>
}