package com.diskin.alon.pagoda.weatherinfo.di

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.infrastructure.LocationErrorHandler
import com.diskin.alon.pagoda.weatherinfo.infrastructure.LocationErrorHandlerImpl
import com.diskin.alon.pagoda.weatherinfo.infrastructure.LocationMapper
import com.diskin.alon.pagoda.weatherinfo.infrastructure.UserLocationProviderImpl
import com.google.android.gms.location.LocationResult
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class InfrastructureModule {

    @Singleton
    @Binds
    abstract fun bindLocationErrorHandler(handler: LocationErrorHandlerImpl): LocationErrorHandler

    @Singleton
    @Binds
    abstract fun bindLocationMapper(mapper: LocationMapper): Mapper<LocationResult, UserLocation>

    @Singleton
    @Binds
    abstract fun bindUserLocationProvider(provider: UserLocationProviderImpl): UserLocationProvider
}