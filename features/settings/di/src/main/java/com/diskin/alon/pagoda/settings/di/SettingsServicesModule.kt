package com.diskin.alon.pagoda.settings.di

import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.eventcontracts.settings.TemperatureUnitPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.WindSpeedUnitPref
import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.common.presentation.ModelDispatcher
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.settings.appservices.TempUnitEventMapper
import com.diskin.alon.pagoda.settings.appservices.TimeFormatEventMapper
import com.diskin.alon.pagoda.settings.appservices.UpdateWeatherUnitUseCase
import com.diskin.alon.pagoda.settings.appservices.WeatherUnit.*
import com.diskin.alon.pagoda.settings.appservices.WindSpeedUnitEventMapper
import com.diskin.alon.pagoda.settings.presentation.SettingsModel
import com.diskin.alon.pagoda.settings.presentation.UpdateWeatherUnitModelRequest
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class SettingsServicesModule {

    companion object {

        @SettingsModel
        @Provides
        fun provideModelDispatcherMap(
            updateWeatherUnitUseCase: UpdateWeatherUnitUseCase
        ): Model {
            val map = HashMap<Class<out ModelRequest<*, *>>,Pair<UseCase<*, *>, Mapper<*, *>?>>()

            map[UpdateWeatherUnitModelRequest::class.java] = Pair(updateWeatherUnitUseCase,null)

            return ModelDispatcher(map)
        }
    }

    @Binds
    abstract fun bindTempUnitEventMapper(mapper: TempUnitEventMapper): Mapper<Temperature, TemperatureUnitPref>

    @Binds
    abstract fun bindWindSpeedUnitEventMapper(mapper: WindSpeedUnitEventMapper): Mapper<WindSpeed, WindSpeedUnitPref>

    @Binds
    abstract fun bindTimeFormatEventMapper(mapper: TimeFormatEventMapper): Mapper<TimeFormat, TimeFormatPref>
}