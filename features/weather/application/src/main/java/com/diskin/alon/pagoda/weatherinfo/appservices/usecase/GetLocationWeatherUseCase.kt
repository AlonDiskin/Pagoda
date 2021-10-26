package com.diskin.alon.pagoda.weatherinfo.appservices.usecase

import com.diskin.alon.pagoda.common.appservices.results.AppResult
import com.diskin.alon.pagoda.common.appservices.usecase.UseCase
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.SettingsRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.WeatherRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.model.WeatherDto
import com.diskin.alon.pagoda.weatherinfo.appservices.util.WeatherDtoMapper
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Coordinate app operations to provide weather info for specific location.
 */
class GetLocationWeatherUseCase @Inject constructor(
    private val weatherRepo: WeatherRepository,
    private val settingsRepo: SettingsRepository,
    private val weatherMapper: WeatherDtoMapper
) : UseCase<LocationWeatherRequest, Observable<AppResult<WeatherDto>>> {

    override fun execute(param: LocationWeatherRequest): Observable<AppResult<WeatherDto>> {
        return Observable.combineLatest(
            settingsRepo.getTempUnit(),
            settingsRepo.getWindSpeedUnit(),
            settingsRepo.getTimeFormat(),
            mapRequest(param),
            { tempUnit,windUnit,timeFormat,weather ->
                when(weather) {
                    is AppResult.Success -> AppResult.Success(weatherMapper
                        .map(weather.data,tempUnit, windUnit, timeFormat))
                    is AppResult.Error -> AppResult.Error(weather.error)
                    is AppResult.Loading -> AppResult.Loading()
                }
            })
    }

    private fun mapRequest(request: LocationWeatherRequest): Observable<AppResult<Weather>> {
        return when(request) {
            is LocationWeatherRequest.UserLocationWeatherRequest -> weatherRepo.getCurrentLocationWeather()
            is LocationWeatherRequest.WorldLocationWeatherRequest -> weatherRepo.getLocationWeather(request.lat,request.lon)
        }
    }
}