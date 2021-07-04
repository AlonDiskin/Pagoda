package com.diskin.alon.pagoda.weatherinfo.di

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.ModelDispatcher
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.model.WeatherDto
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.GetWorldLocationWeatherUseCase
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.GetUserLocationWeatherUseCase
import com.diskin.alon.pagoda.weatherinfo.presentation.model.WorldLocationWeatherModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiWeather
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UserLocationWeatherModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.util.UiWeatherMapper
import com.diskin.alon.pagoda.weatherinfo.presentation.util.WeatherInfoModel
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

        @WeatherInfoModel
        @Provides
        fun provideModelDispatcherMap(
            getLocationWeather: GetWorldLocationWeatherUseCase,
            getUserLocationWeather: GetUserLocationWeatherUseCase,
            uiWeatherMapper: Mapper<Observable<AppResult<WeatherDto>>, Observable<AppResult<UiWeather>>>
        ): Model {
            val map = HashMap<Class<out ModelRequest<*, *>>,Pair<UseCase<*, *>, Mapper<*, *>?>>()

            map[UserLocationWeatherModelRequest::class.java] = Pair(getUserLocationWeather,uiWeatherMapper)
            map[WorldLocationWeatherModelRequest::class.java] = Pair(getLocationWeather,uiWeatherMapper)

            return ModelDispatcher(map)
        }
    }

    @Binds
    abstract fun provideUiWeatherMapper(mapper: UiWeatherMapper): Mapper<Observable<AppResult<WeatherDto>>,Observable<AppResult<UiWeather>>>
}