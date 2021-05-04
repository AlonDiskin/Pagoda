package com.diskin.alon.pagoda.settings.di

import com.diskin.alon.pagoda.common.eventcontracts.settings.TemperatureUnitPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.WindSpeedUnitPref
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.settings.appservices.model.AlertInfo
import com.diskin.alon.pagoda.settings.appservices.model.ScheduleAlertRequest
import com.diskin.alon.pagoda.settings.appservices.model.WeatherUnit.*
import com.diskin.alon.pagoda.settings.appservices.util.SchedulingRequestMapper
import com.diskin.alon.pagoda.settings.appservices.util.TempUnitEventMapper
import com.diskin.alon.pagoda.settings.appservices.util.TimeFormatEventMapper
import com.diskin.alon.pagoda.settings.appservices.util.WindSpeedUnitEventMapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class SettingsServicesModule {

    @Binds
    abstract fun bindTempUnitEventMapper(mapper: TempUnitEventMapper): Mapper<Temperature, TemperatureUnitPref>

    @Binds
    abstract fun bindWindSpeedUnitEventMapper(mapper: WindSpeedUnitEventMapper): Mapper<WindSpeed, WindSpeedUnitPref>

    @Binds
    abstract fun bindTimeFormatEventMapper(mapper: TimeFormatEventMapper): Mapper<TimeFormat, TimeFormatPref>

    @Binds
    abstract fun bindWeatherAlertRequestMapper(mapper: SchedulingRequestMapper): Mapper<ScheduleAlertRequest, AlertInfo>
}