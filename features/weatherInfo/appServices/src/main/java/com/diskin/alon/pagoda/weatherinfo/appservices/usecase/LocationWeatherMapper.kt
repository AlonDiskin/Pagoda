package com.diskin.alon.pagoda.weatherinfo.appservices.usecase

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.model.*
import com.diskin.alon.pagoda.weatherinfo.domain.LocationWeather
import com.diskin.alon.pagoda.weatherinfo.domain.UnitSystem
import com.diskin.alon.pagoda.weatherinfo.domain.UvIndex
import com.diskin.alon.pagoda.weatherinfo.domain.WeatherDescription
import javax.inject.Inject

class LocationWeatherMapper @Inject constructor() : Mapper<LocationWeather, LocationWeatherDto> {
    override fun map(source: LocationWeather): LocationWeatherDto {
        return LocationWeatherDto(
            source.name,
            source.country,
            source.timeZone,
            source.currentTemp,
            source.feelTemp,
            source.minTemp,
            source.maxTemp,
            WeatherConditionDto(
                mapCondition(source.condition.description),
                source.condition.isDay
            ),
            source.humidity,
            source.windSpeed,
            source.sunrise,
            source.sunset,
            mapUnitSystem(source.unitSystem),
            mapUvIndex(source.uvIndex()),
            source.hourlyForecast.map {
                HourForecastDto(
                    it.hour,
                    WeatherConditionDto(
                        mapCondition(it.condition.description),
                        it.condition.isDay
                    ),
                    it.temp
                )
            },
            source.dailyForecast.map {
                DayForecastDto(
                    it.dayOfWeek,
                    WeatherConditionDto(
                        mapCondition(it.condition.description),
                        it.condition.isDay
                    ),
                    it.minTemp,
                    it.maxTemp
                )
            }
        )
    }

    private fun mapCondition(condition: WeatherDescription): WeatherDescriptionDto {
        return WeatherDescriptionDto.values()[condition.ordinal]
    }

    private fun mapUnitSystem(unitSystem: UnitSystem): UnitSystemDto {
        return UnitSystemDto.values()[unitSystem.ordinal]
    }

    private fun mapUvIndex(uvIndex: UvIndex): UvIndexDto {
        return UvIndexDto.values()[uvIndex.ordinal]
    }
}