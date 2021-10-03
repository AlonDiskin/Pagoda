package com.diskin.alon.pagoda.weatherinfo.di

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.ModelDispatcher
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.WeatherDto
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.*
import com.diskin.alon.pagoda.weatherinfo.presentation.model.*
import com.diskin.alon.pagoda.weatherinfo.presentation.util.BookmarkedLocationMapper
import com.diskin.alon.pagoda.weatherinfo.presentation.util.SearchedLocationMapper
import com.diskin.alon.pagoda.weatherinfo.presentation.util.UiWeatherMapper
import com.diskin.alon.pagoda.weatherinfo.presentation.util.WeatherModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.reactivex.Observable

@Module
@InstallIn(ViewModelComponent::class)
abstract class WeatherUiModule {

    companion object {

        @WeatherModel
        @Provides
        fun provideModelDispatcherMap(
            getLocationWeather: GetWorldLocationWeatherUseCase,
            getUserLocationWeather: GetUserLocationWeatherUseCase,
            uiWeatherMapper: Mapper<Observable<AppResult<WeatherDto>>, Observable<AppResult<UiWeather>>>,
            searchLocationUseCase: SearchLocationsUseCase,
            browseBookmarkedLocationsUseCase: BrowseBookmarkedLocationsUseCase,
            unbookmarkLocationUseCase: UnBookmarkLocationUseCase,
            bookmarkLocationUseCase: BookmarkLocationUseCase,
            searchedLocationMapper: Mapper<Observable<PagingData<LocationDto>>,Observable<PagingData<UiLocationSearchResult>>>,
            bookmarkedLocationMapper: Mapper<Observable<PagingData<LocationDto>>,Observable<PagingData<UiBookmarkedLocation>>>
        ): Model {
            val map = HashMap<Class<out ModelRequest<*, *>>,Pair<UseCase<*, *>, Mapper<*, *>?>>()

            map[UserLocationWeatherModelRequest::class.java] = Pair(getUserLocationWeather,uiWeatherMapper)
            map[WorldLocationWeatherModelRequest::class.java] = Pair(getLocationWeather,uiWeatherMapper)
            map[SearchLocationsModelRequest::class.java] = Pair(searchLocationUseCase,searchedLocationMapper)
            map[BookmarkedLocationsModelRequest::class.java] = Pair(browseBookmarkedLocationsUseCase,bookmarkedLocationMapper)
            map[UnBookmarkLocationModelRequest::class.java] = Pair(unbookmarkLocationUseCase,null)
            map[BookmarkLocationModelRequest::class.java] = Pair(bookmarkLocationUseCase,null)

            return ModelDispatcher(map)
        }
    }

    @Binds
    abstract fun provideUiWeatherMapper(mapper: UiWeatherMapper): Mapper<Observable<AppResult<WeatherDto>>,Observable<AppResult<UiWeather>>>

    @Binds
    abstract fun bindSearchLocationMapper(mapper: SearchedLocationMapper): Mapper<Observable<PagingData<LocationDto>>,Observable<PagingData<UiLocationSearchResult>>>

    @Binds
    abstract fun bindBookmarkedLocationMapper(mapper: BookmarkedLocationMapper): Mapper<Observable<PagingData<LocationDto>>,Observable<PagingData<UiBookmarkedLocation>>>
}