package com.diskin.alon.pagoda.weatherinfo.data.implementations

import com.diskin.alon.pagoda.common.shared.AppDataProvider
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.settings.shared.TempUnit
import com.diskin.alon.pagoda.settings.shared.TimeFormat
import com.diskin.alon.pagoda.settings.shared.WindSpeedUnit
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.SettingsRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.TimeFormatDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UnitSystemDto
import io.reactivex.Observable
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val tempUnitPrefProvider: AppDataProvider<Observable<TempUnit>>,
    private val windSpeedUnitPrefProvider: AppDataProvider<Observable<WindSpeedUnit>>,
    private val timeFormatPrefProvider: AppDataProvider<Observable<TimeFormat>>,
    private val tempUnitMapper: Mapper<TempUnit,UnitSystemDto>,
    private val windSpeedUnitMapper: Mapper<WindSpeedUnit,UnitSystemDto>,
    private val timeFormatMapper: Mapper<TimeFormat,TimeFormatDto>
) : SettingsRepository {

    override fun getTempUnit(): Observable<UnitSystemDto> {
        return tempUnitPrefProvider.get()
            .map(tempUnitMapper::map)
    }

    override fun getWindSpeedUnit(): Observable<UnitSystemDto> {
        return windSpeedUnitPrefProvider.get()
            .map(windSpeedUnitMapper::map)
    }

    override fun getTimeFormat(): Observable<TimeFormatDto> {
        return timeFormatPrefProvider.get()
            .map(timeFormatMapper::map)
    }
}