package com.diskin.alon.pagoda.weatherinfo.di

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.data.local.implementations.UserLocationProviderImpl
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.data.local.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.data.local.util.UserLocationMapper
import com.google.android.gms.location.LocationResult
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WeatherLocationModule {

    @Singleton
    @Binds
    abstract fun bindLocationProvider(provider: UserLocationProviderImpl): UserLocationProvider

    @Singleton
    @Binds
    abstract fun bindUserLocationMapper(mapper: UserLocationMapper): Mapper<LocationResult, UserLocation>
}