package com.diskin.alon.pagoda.weatherinfo.appservices.usecase

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.toAppResult
import com.diskin.alon.pagoda.common.eventcontracts.AppEventProvider
import com.diskin.alon.pagoda.common.eventcontracts.settings.TemperatureUnitPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.UnitPrefSystem
import com.diskin.alon.pagoda.common.eventcontracts.settings.WindSpeedUnitPref
import com.diskin.alon.pagoda.weatherinfo.appservices.model.*
import com.diskin.alon.pagoda.weatherinfo.domain.UvIndex
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import com.diskin.alon.pagoda.weatherinfo.domain.WeatherDescription
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Transform a weather to observable that emit a mapped value of weather, according
 * to app setting configuration for weather units.
 */
class WeatherUnitsSettingMapper @Inject constructor(
    private val tempUnitPrefProvider: AppEventProvider<TemperatureUnitPref>,
    private val windSpeedUnitPrefProvider: AppEventProvider<WindSpeedUnitPref>,
    private val timeFormatPrefProvider: AppEventProvider<TimeFormatPref>
) {

    fun mapWeather(weather: Weather): Observable<AppResult<WeatherDto>> {
        return Observable.combineLatest(
            tempUnitPrefProvider.get().map { mapUnitPrefSystem(it.system) },
            windSpeedUnitPrefProvider.get().map { mapUnitPrefSystem(it.system) },
            timeFormatPrefProvider.get().map { mapTimeFormatPref(it.format) },
            { tempUnit,windUnit,timeFormat ->
                val windSpeed = when(windUnit) {
                    UnitSystemDto.METRIC -> {
                        weather.toMetric()
                        weather.windSpeed
                    }

                    UnitSystemDto.IMPERIAL -> {
                        weather.toImperial()
                        weather.windSpeed
                    }
                }

                when(tempUnit) {
                    UnitSystemDto.METRIC -> {
                        weather.toMetric()
                    }

                    UnitSystemDto.IMPERIAL -> {
                        weather.toImperial()
                    }
                }

                WeatherDto(
                    weather.name,
                    weather.country,
                    weather.timeZone,
                    timeFormat,
                    tempUnit,
                    windUnit,
                    weather.currentTemp,
                    weather.feelTemp,
                    weather.minTemp,
                    weather.maxTemp,
                    WeatherConditionDto(
                        mapCondition(weather.condition.description),
                        weather.condition.isDay
                    ),
                    weather.humidity,
                    windSpeed,
                    weather.sunrise,
                    weather.sunset,
                    mapUvIndex(weather.uvIndex()),
                    weather.hourlyForecast.map {
                        HourForecastDto(
                            it.hour,
                            WeatherConditionDto(
                                mapCondition(it.condition.description),
                                it.condition.isDay
                            ),
                            it.temp
                        )
                    },
                    weather.dailyForecast.map {
                        DayForecastDto(
                            it.dayOfWeek,
                            WeatherConditionDto(
                                mapCondition(it.condition.description),
                                it.condition.isDay
                            ),
                            it.minTemp,
                            it.maxTemp
                        )
                    },
                    weather.updated
                )
            })
            .toAppResult()
    }

    private fun mapUnitPrefSystem(prefSystem: UnitPrefSystem): UnitSystemDto {
        return when(prefSystem) {
            UnitPrefSystem.METRIC -> UnitSystemDto.METRIC
            UnitPrefSystem.IMPERIAL -> UnitSystemDto.IMPERIAL
        }
    }

    private fun mapTimeFormatPref(prefFormat: TimeFormatPref.HourFormat): TimeFormatDto {
        return when(prefFormat) {
            TimeFormatPref.HourFormat.HOUR_12 -> TimeFormatDto.HOUR_12
            TimeFormatPref.HourFormat.HOUR_24 -> TimeFormatDto.HOUR_24
        }
    }

    private fun mapCondition(condition: WeatherDescription): WeatherDescriptionDto {
        return WeatherDescriptionDto.values()[condition.ordinal]
    }

    private fun mapUvIndex(uvIndex: UvIndex): UvIndexDto {
        return UvIndexDto.values()[uvIndex.ordinal]
    }
}