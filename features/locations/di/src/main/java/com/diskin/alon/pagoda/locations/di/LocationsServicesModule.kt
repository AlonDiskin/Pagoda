package com.diskin.alon.pagoda.locations.di

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.model.BookmarkLocationRequest
import com.diskin.alon.pagoda.locations.appservices.model.UnBookmarkLocationRequest
import com.diskin.alon.pagoda.locations.appservices.model.LocationDto
import com.diskin.alon.pagoda.locations.appservices.util.BookmarkLocationRequestMapper
import com.diskin.alon.pagoda.locations.appservices.util.UnBookmarkLocationRequestMapper
import com.diskin.alon.pagoda.locations.appservices.util.LocationMapper
import com.diskin.alon.pagoda.locations.domain.Coordinates
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

    @Binds
    abstract fun bindUnBookmarkLocationRequestMapper(mapper: UnBookmarkLocationRequestMapper): Mapper<UnBookmarkLocationRequest, Coordinates>

    @Binds
    abstract fun bindBookmarkLocationRequestMapper(mapper: BookmarkLocationRequestMapper): Mapper<BookmarkLocationRequest, Coordinates>
}