package com.diskin.alon.pagoda.weatherinfo.appservices.usecase

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.appservices.toData
import com.diskin.alon.pagoda.common.appservices.toResult
import com.diskin.alon.pagoda.common.eventcontracts.AppEventProvider
import com.diskin.alon.pagoda.common.eventcontracts.settings.TemperatureUnitPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.UnitPrefSystem
import com.diskin.alon.pagoda.common.eventcontracts.settings.WindSpeedUnitPref
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.WeatherRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.*
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherRequest.CurrentLocationRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherRequest.LocationRequest
import com.diskin.alon.pagoda.weatherinfo.domain.UvIndex
import com.diskin.alon.pagoda.weatherinfo.domain.WeatherDescription
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Coordinate app operations to provide weather info for a world location/current user location.
 */
class ProvideLocationWeatherUseCase @Inject constructor(
    private val weatherRepo: WeatherRepository,
    private val locationProvider: UserLocationProvider,
    private val tempUnitPrefProvider: AppEventProvider<TemperatureUnitPref>,
    private val windSpeedUnitPrefProvider: AppEventProvider<WindSpeedUnitPref>,
    private val timeFormatPrefProvider: AppEventProvider<TimeFormatPref>
) : UseCase<LocationWeatherRequest, Observable<AppResult<LocationWeatherDto>>> {

    override fun execute(param: LocationWeatherRequest): Observable<AppResult<LocationWeatherDto>> {
        val weather =  when(param) {
            // Provide weather data for current user location
            is CurrentLocationRequest -> locationProvider
                .getCurrentLocation().toData()
                .switchMap { getWeather(it.lat,it.lon) }

            // Provide weather data for world location
            is LocationRequest -> getWeather(param.lat,param.lon)
        }

        return weather.toResult()
    }

    private fun getWeather(lat: Double, lon: Double): Observable<LocationWeatherDto> {
        return Observable.combineLatest(
            weatherRepo.get(lat,lon).toData(),
            tempUnitPrefProvider.get().map { mapUnitPrefSystem(it.system) },
            windSpeedUnitPrefProvider.get().map { mapUnitPrefSystem(it.system) },
            timeFormatPrefProvider.get().map { mapTimeFormatPref(it.format) },
            { weather,tempUnit,windUnit,timeFormat ->
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

                LocationWeatherDto(
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
                    }
                )
            })
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