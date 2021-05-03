package com.diskin.alon.pagoda.locations.di

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.ModelDispatcher
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.model.LocationSearchResult
import com.diskin.alon.pagoda.locations.appservices.usecase.LocationMapper
import com.diskin.alon.pagoda.locations.appservices.usecase.SearchLocationsUseCase
import com.diskin.alon.pagoda.locations.domain.Location
import com.diskin.alon.pagoda.locations.presentation.model.SearchModelRequest
import com.diskin.alon.pagoda.locations.presentation.util.SearchLocationsModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class LocationsServicesModule {

    companion object {

        @SearchLocationsModel
        @Provides
        fun provideModelDispatcherMap(
            searchUseCase: SearchLocationsUseCase
        ): Model {
            val map = HashMap<Class<out ModelRequest<*, *>>,Pair<UseCase<*, *>, Mapper<*, *>?>>()

            map[SearchModelRequest::class.java] = Pair(searchUseCase,null)

            return ModelDispatcher(map)
        }
    }

    @Binds
    abstract fun bindLocationMapper(mapper: LocationMapper): Mapper<PagingData<Location>, PagingData<LocationSearchResult>>
}