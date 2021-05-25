package com.diskin.alon.pagoda.locations.di

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.ModelDispatcher
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.model.LocationDto
import com.diskin.alon.pagoda.locations.appservices.usecase.BrowseSavedLocationsUseCase
import com.diskin.alon.pagoda.locations.appservices.usecase.DeleteSavedLocationUseCase
import com.diskin.alon.pagoda.locations.appservices.usecase.SearchLocationsUseCase
import com.diskin.alon.pagoda.locations.presentation.model.DeleteSavedLocationModelRequest
import com.diskin.alon.pagoda.locations.presentation.model.SavedLocationsModelRequest
import com.diskin.alon.pagoda.locations.presentation.model.SearchLocationsModelRequest
import com.diskin.alon.pagoda.locations.presentation.model.UiLocation
import com.diskin.alon.pagoda.locations.presentation.util.LocationMapper
import com.diskin.alon.pagoda.locations.presentation.util.LocationsModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.reactivex.Observable

@Module
@InstallIn(ViewModelComponent::class)
abstract class LocationsUiModel {

    companion object {

        @LocationsModel
        @Provides
        fun provideModelDispatcherMap(
            searchUseCase: SearchLocationsUseCase,
            savedLocationsUseCase: BrowseSavedLocationsUseCase,
            deleteLocationUseCase: DeleteSavedLocationUseCase,
            uiLocationMapper: Mapper<Observable<PagingData<LocationDto>>, Observable<PagingData<UiLocation>>>
        ): Model {
            val map = HashMap<Class<out ModelRequest<*, *>>,Pair<UseCase<*, *>, Mapper<*, *>?>>()

            map[SearchLocationsModelRequest::class.java] = Pair(searchUseCase,uiLocationMapper)
            map[SavedLocationsModelRequest::class.java] = Pair(savedLocationsUseCase,uiLocationMapper)
            map[DeleteSavedLocationModelRequest::class.java] = Pair(deleteLocationUseCase,null)

            return ModelDispatcher(map)
        }
    }

    @Binds
    abstract fun bindUiLocationMapper(mapper: LocationMapper): Mapper<Observable<PagingData<LocationDto>>, Observable<PagingData<UiLocation>>>
}