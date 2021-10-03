package com.diskin.alon.pagoda.weatherinfo.di

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.model.BookmarkLocationRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UnBookmarkLocationRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.util.BookmarkLocationRequestMapper
import com.diskin.alon.pagoda.weatherinfo.appservices.util.LocationMapper
import com.diskin.alon.pagoda.weatherinfo.appservices.util.UnBookmarkLocationRequestMapper
import com.diskin.alon.pagoda.weatherinfo.domain.Coordinates
import com.diskin.alon.pagoda.weatherinfo.domain.Location
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class WeatherApplicationModule {

    @Binds
    abstract fun bindLocationMapper(mapper: LocationMapper): Mapper<PagingData<Location>, PagingData<LocationDto>>

    @Binds
    abstract fun bindUnBookmarkLocationRequestMapper(mapper: UnBookmarkLocationRequestMapper): Mapper<UnBookmarkLocationRequest, Coordinates>

    @Binds
    abstract fun bindBookmarkLocationRequestMapper(mapper: BookmarkLocationRequestMapper): Mapper<BookmarkLocationRequest, Coordinates>
}