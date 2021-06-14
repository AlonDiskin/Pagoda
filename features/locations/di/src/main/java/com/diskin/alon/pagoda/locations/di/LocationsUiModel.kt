package com.diskin.alon.pagoda.locations.di

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.ModelDispatcher
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.model.LocationDto
import com.diskin.alon.pagoda.locations.appservices.usecase.BookmarkLocationUseCase
import com.diskin.alon.pagoda.locations.appservices.usecase.BrowseBookmarkdLocationsUseCase
import com.diskin.alon.pagoda.locations.appservices.usecase.UnBookmarkLocationUseCase
import com.diskin.alon.pagoda.locations.appservices.usecase.SearchLocationsUseCase
import com.diskin.alon.pagoda.locations.presentation.model.*
import com.diskin.alon.pagoda.locations.presentation.util.BookmarkedLocationMapper
import com.diskin.alon.pagoda.locations.presentation.util.LocationsModel
import com.diskin.alon.pagoda.locations.presentation.util.SearchedLocationMapper
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
            searchLocationUseCase: SearchLocationsUseCase,
            browseBookmarkedLocationsUseCase: BrowseBookmarkdLocationsUseCase,
            unbookmarkLocationUseCase: UnBookmarkLocationUseCase,
            bookmarkLocationUseCase: BookmarkLocationUseCase,
            searchedLocationMapper: Mapper<Observable<PagingData<LocationDto>>,Observable<PagingData<UiLocationSearchResult>>>,
            bookmarkedLocationMapper: Mapper<Observable<PagingData<LocationDto>>,Observable<PagingData<UiBookmarkedLocation>>>
        ): Model {
            val map = HashMap<Class<out ModelRequest<*, *>>,Pair<UseCase<*, *>, Mapper<*, *>?>>()

            map[SearchLocationsModelRequest::class.java] = Pair(searchLocationUseCase,searchedLocationMapper)
            map[BookmarkedLocationsModelRequest::class.java] = Pair(browseBookmarkedLocationsUseCase,bookmarkedLocationMapper)
            map[UnBookmarkLocationModelRequest::class.java] = Pair(unbookmarkLocationUseCase,null)
            map[BookmarkLocationModelRequest::class.java] = Pair(bookmarkLocationUseCase,null)

            return ModelDispatcher(map)
        }
    }

    @Binds
    abstract fun bindSearchLocationMapper(mapper: SearchedLocationMapper): Mapper<Observable<PagingData<LocationDto>>,Observable<PagingData<UiLocationSearchResult>>>

    @Binds
    abstract fun bindBookmarkedLocationMapper(mapper: BookmarkedLocationMapper): Mapper<Observable<PagingData<LocationDto>>,Observable<PagingData<UiBookmarkedLocation>>>
}