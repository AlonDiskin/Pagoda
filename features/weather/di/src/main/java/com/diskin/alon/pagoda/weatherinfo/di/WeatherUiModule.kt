package com.diskin.alon.pagoda.weatherinfo.di

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.appservices.results.AppResult
import com.diskin.alon.pagoda.common.appservices.usecase.UseCase
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.ModelDispatcher
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.WeatherDto
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.*
import com.diskin.alon.pagoda.weatherinfo.presentation.model.*
import com.diskin.alon.pagoda.weatherinfo.presentation.util.*
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
            getLocationWeather: GetLocationWeatherUseCase,
            uiWeatherMapper: Mapper<Observable<AppResult<WeatherDto>>, Observable<AppResult<UiWeather>>>,
            searchLocationUseCase: SearchLocationsUseCase,
            favoriteLocationsUseCase: GetFavoriteLocationsUseCase,
            unfavoriteLocationUseCase: UnfavoriteLocationUseCase,
            favoriteLocationUseCase: FavoriteLocationUseCase,
            uiLocationMapper: Mapper<Observable<PagingData<LocationDto>>,Observable<PagingData<UiLocation>>>
        ): Model {
            val map = HashMap<Class<out ModelRequest<*, *>>,Pair<UseCase<*, *>, Mapper<*, *>?>>()

            map[LocationWeatherModelRequest.UserLocationWeatherModelRequest::class.java] = Pair(getLocationWeather,uiWeatherMapper)
            map[LocationWeatherModelRequest.WorldLocationWeatherModelRequest::class.java] = Pair(getLocationWeather,uiWeatherMapper)
            map[SearchLocationsModelRequest::class.java] = Pair(searchLocationUseCase,uiLocationMapper)
            map[UnfavoriteLocationModelRequest::class.java] = Pair(unfavoriteLocationUseCase,null)
            map[FavoriteLocationModelRequest::class.java] = Pair(favoriteLocationUseCase,null)
            map[FavoriteLocationsModelRequest::class.java] = Pair(favoriteLocationsUseCase,uiLocationMapper)

            return ModelDispatcher(map)
        }
    }

    @Binds
    abstract fun provideUiWeatherMapper(mapper: UiWeatherMapper): Mapper<Observable<AppResult<WeatherDto>>,Observable<AppResult<UiWeather>>>

    @Binds
    abstract fun bindUiLocationMapper(mapper: UiLocationMapper): Mapper<Observable<PagingData<LocationDto>>,Observable<PagingData<UiLocation>>>
}