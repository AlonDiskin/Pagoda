package com.diskin.alon.pagoda.weatherinfo.di

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.model.FavoriteLocationRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UnfavoriteLocationRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.util.FavoriteLocationRequestMapper
import com.diskin.alon.pagoda.weatherinfo.appservices.util.LocationDtoMapper
import com.diskin.alon.pagoda.weatherinfo.appservices.util.UnfavoriteLocationRequestMapper
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
    abstract fun bindLocationMapper(mapper: LocationDtoMapper): Mapper<PagingData<Location>, PagingData<LocationDto>>

    @Binds
    abstract fun bindUnBookmarkLocationRequestMapper(mapper: UnfavoriteLocationRequestMapper): Mapper<UnfavoriteLocationRequest, Coordinates>

    @Binds
    abstract fun bindBookmarkLocationRequestMapper(mapper: FavoriteLocationRequestMapper): Mapper<FavoriteLocationRequest, Coordinates>
}