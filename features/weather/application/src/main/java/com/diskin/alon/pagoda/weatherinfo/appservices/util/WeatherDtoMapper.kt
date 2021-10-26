package com.diskin.alon.pagoda.weatherinfo.appservices.util

import com.diskin.alon.pagoda.weatherinfo.appservices.model.*
import com.diskin.alon.pagoda.weatherinfo.domain.UvIndex
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import com.diskin.alon.pagoda.weatherinfo.domain.WeatherCondition
import com.diskin.alon.pagoda.weatherinfo.domain.WeatherDescription
import javax.inject.Inject

class WeatherDtoMapper @Inject constructor() {

    fun map(weather: Weather,tempUnit: UnitSystemDto, windUnit: UnitSystemDto ,timeFormat: TimeFormatDto): WeatherDto {
        return WeatherDto(
            weather.name,
            weather.country,
            weather.timeZone,
            timeFormat,
            tempUnit,
            windUnit,
            mapCurrentTemp(weather,tempUnit),
            mapFeelTemp(weather,tempUnit),
            mapMinTemp(weather,tempUnit),
            mapMaxTemp(weather,tempUnit),
            mapCondition(weather.condition),
            weather.humidity,
            mapWindSpeed(weather,windUnit),
            weather.sunrise,
            weather.sunset,
            mapUvIndex(weather.uvIndex()),
            mapHourlyForecast(weather,tempUnit),
            mapDailyForecast(weather,tempUnit),
            weather.updated
        )
    }

    private fun mapDailyForecast(weather: Weather, tempUnit: UnitSystemDto): List<DayForecastDto> {
        when(tempUnit) {
            UnitSystemDto.METRIC -> weather.toMetric()
            UnitSystemDto.IMPERIAL -> weather.toImperial()
        }

        return weather.dailyForecast.map {
            DayForecastDto(
                it.dayOfWeek,
                mapCondition(it.condition),
                it.minTemp,
                it.maxTemp
            )
        }
    }

    private fun mapHourlyForecast(weather: Weather, tempUnit: UnitSystemDto): List<HourForecastDto> {
        when(tempUnit) {
            UnitSystemDto.METRIC -> weather.toMetric()
            UnitSystemDto.IMPERIAL -> weather.toImperial()
        }

        return weather.hourlyForecast.map {
            HourForecastDto(
                it.hour,
                mapCondition(it.condition),
                it.temp
            )
        }
    }

    private fun mapWindSpeed(weather: Weather, windUnit: UnitSystemDto): Double {
        return when(windUnit) {
            UnitSystemDto.METRIC -> {
                weather.toMetric()
                weather.windSpeed
            }

            UnitSystemDto.IMPERIAL -> {
                weather.toImperial()
                weather.windSpeed
            }
        }
    }

    private fun mapMaxTemp(weather: Weather, tempUnit: UnitSystemDto): Double {
        return when(tempUnit) {
            UnitSystemDto.METRIC -> {
                weather.toMetric()
                weather.maxTemp
            }

            UnitSystemDto.IMPERIAL -> {
                weather.toImperial()
                weather.maxTemp
            }
        }
    }

    private fun mapMinTemp(weather: Weather, tempUnit: UnitSystemDto): Double {
        return when(tempUnit) {
            UnitSystemDto.METRIC -> {
                weather.toMetric()
                weather.minTemp
            }

            UnitSystemDto.IMPERIAL -> {
                weather.toImperial()
                weather.minTemp
            }
        }
    }

    private fun mapFeelTemp(weather: Weather, tempUnit: UnitSystemDto): Double {
        return when(tempUnit) {
            UnitSystemDto.METRIC -> {
                weather.toMetric()
                weather.feelTemp
            }

            UnitSystemDto.IMPERIAL -> {
                weather.toImperial()
                weather.feelTemp
            }
        }
    }

    private fun mapCurrentTemp(weather: Weather, tempUnit: UnitSystemDto): Double {
        return when(tempUnit) {
            UnitSystemDto.METRIC -> {
                weather.toMetric()
                weather.currentTemp
            }

            UnitSystemDto.IMPERIAL -> {
                weather.toImperial()
                weather.currentTemp
            }
        }
    }

    private fun mapCondition(condition: WeatherCondition): WeatherConditionDto {
        return WeatherConditionDto(
            mapDescription(condition.description),
            condition.isDay
        )
    }

    private fun mapDescription(description: WeatherDescription): WeatherDescriptionDto {
        return WeatherDescriptionDto.values()[description.ordinal]
    }

    private fun mapUvIndex(uvIndex: UvIndex): UvIndexDto {
        return UvIndexDto.values()[uvIndex.ordinal]
    }
}