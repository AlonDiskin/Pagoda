package com.diskin.alon.pagoda.locations.di

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.locations.data.implementations.LocationRepositoryImpl
import com.diskin.alon.pagoda.locations.data.local.LocationEntity
import com.diskin.alon.pagoda.locations.data.local.LocationMapper
import com.diskin.alon.pagoda.locations.domain.Location
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationsDataModule {

    @Singleton
    @Binds
    abstract fun provideLocationRepository(repository: LocationRepositoryImpl): LocationRepository

    @Singleton
    @Binds
    abstract fun provideLocationMapper(mapper: LocationMapper): Mapper<PagingData<LocationEntity>, PagingData<Location>>
}