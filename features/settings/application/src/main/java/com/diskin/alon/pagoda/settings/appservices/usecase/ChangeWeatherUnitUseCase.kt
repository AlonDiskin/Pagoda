package com.diskin.alon.pagoda.settings.appservices.usecase

import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.eventcontracts.AppEventPublisher
import com.diskin.alon.pagoda.common.eventcontracts.settings.TemperatureUnitPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.WindSpeedUnitPref
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.settings.appservices.model.WeatherUnit
import javax.inject.Inject

/**
 * Coordinate app operations to change app weather data units type.
 */
class ChangeWeatherUnitUseCase @Inject constructor(
    private val tempUnitPublisher: AppEventPublisher<TemperatureUnitPref>,
    private val windSpeedUnitPublisher: AppEventPublisher<WindSpeedUnitPref>,
    private val timeFormatPublisher: AppEventPublisher<TimeFormatPref>,
    private val tempUnitMapper: Mapper<WeatherUnit.Temperature,TemperatureUnitPref>,
    private val windSpeedUnitMapper: Mapper<WeatherUnit.WindSpeed,WindSpeedUnitPref>,
    private val timeFormatMapper: Mapper<WeatherUnit.TimeFormat,TimeFormatPref>
) : UseCase<WeatherUnit,Unit> {

    override fun execute(param: WeatherUnit) {
        when(param) {
            is WeatherUnit.Temperature -> tempUnitPublisher.publish(tempUnitMapper.map(param))
            is WeatherUnit.WindSpeed -> windSpeedUnitPublisher.publish(windSpeedUnitMapper.map(param))
            is WeatherUnit.TimeFormat -> timeFormatPublisher.publish(timeFormatMapper.map(param))
        }
    }
}