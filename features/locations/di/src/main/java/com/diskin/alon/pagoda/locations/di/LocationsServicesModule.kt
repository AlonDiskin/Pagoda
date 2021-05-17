package com.diskin.alon.pagoda.locations.di

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.model.LocationDto
import com.diskin.alon.pagoda.locations.appservices.usecase.LocationMapper
import com.diskin.alon.pagoda.locations.domain.Location
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class LocationsServicesModule {

    @Binds
    abstract fun bindLocationMapper(mapper: LocationMapper): Mapper<PagingData<Location>, PagingData<LocationDto>>
}