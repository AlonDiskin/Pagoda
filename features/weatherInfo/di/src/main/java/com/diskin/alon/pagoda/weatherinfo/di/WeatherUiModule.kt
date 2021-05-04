package com.diskin.alon.pagoda.weatherinfo.di

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.ModelDispatcher
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherDto
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.ProvideLocationWeatherUseCase
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiWeather
import com.diskin.alon.pagoda.weatherinfo.presentation.model.WeatherModelRequest.CurrentLocationWeatherModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.model.WeatherModelRequest.LocationWeatherModelRequest
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
            getWeatherUseCase: ProvideLocationWeatherUseCase,
            uiWeatherMapper: Mapper<Observable<AppResult<LocationWeatherDto>>, Observable<AppResult<UiWeather>>>
        ): Model {
            val map = HashMap<Class<out ModelRequest<*, *>>,Pair<UseCase<*, *>, Mapper<*, *>?>>()

            map[CurrentLocationWeatherModelRequest::class.java] = Pair(getWeatherUseCase,uiWeatherMapper)
            map[LocationWeatherModelRequest::class.java] = Pair(getWeatherUseCase,uiWeatherMapper)

            return ModelDispatcher(map)
        }
    }

    @Binds
    abstract fun provideUiWeatherMapper(mapper: UiWeatherMapper): Mapper<Observable<AppResult<LocationWeatherDto>>,Observable<AppResult<UiWeather>>>
}