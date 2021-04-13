package com.diskin.alon.pagoda.weatherinfo.di

import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.ModelDispatcher
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherDto
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.GetCurrentWeatherUseCase
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.LocationWeatherMapper
import com.diskin.alon.pagoda.weatherinfo.domain.LocationWeather
import com.diskin.alon.pagoda.weatherinfo.presentation.model.CurrentWeatherModelRequest
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
            getWeatherUseCase: GetCurrentWeatherUseCase
        ): Model {
            val map = HashMap<Class<out ModelRequest<*, *>>,Pair<UseCase<*, *>, Mapper<*, *>?>>()

            map[CurrentWeatherModelRequest::class.java] = Pair(getWeatherUseCase,null)

            return ModelDispatcher(map)
        }
    }

    @Binds
    abstract fun bindLocationWeatherMapper(mapper: LocationWeatherMapper): Mapper<LocationWeather, LocationWeatherDto>
}