package com.diskin.alon.pagoda.weatherinfo.di

import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.ModelDispatcher
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherDto
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.GetLocationWeatherUseCase
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.LocationWeatherMapper
import com.diskin.alon.pagoda.weatherinfo.domain.LocationWeather
import com.diskin.alon.pagoda.weatherinfo.presentation.model.WeatherModelRequest.CurrentLocationWeatherModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.model.WeatherModelRequest.LocationWeatherModelRequest
import com.diskin.alon.pagoda.weatherinfo.presentation.util.WeatherInfoModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class WeatherDataModule {

    companion object {

        @WeatherInfoModel
        @Provides
        fun provideModelDispatcherMap(
            getWeatherUseCase: GetLocationWeatherUseCase
        ): Model {
            val map = HashMap<Class<out ModelRequest<*, *>>,Pair<UseCase<*, *>, Mapper<*, *>?>>()

            map[CurrentLocationWeatherModelRequest::class.java] = Pair(getWeatherUseCase,null)
            map[LocationWeatherModelRequest::class.java] = Pair(getWeatherUseCase,null)

            return ModelDispatcher(map)
        }
    }

    @Binds
    abstract fun bindLocationWeatherMapper(mapper: LocationWeatherMapper): Mapper<LocationWeather, LocationWeatherDto>
}