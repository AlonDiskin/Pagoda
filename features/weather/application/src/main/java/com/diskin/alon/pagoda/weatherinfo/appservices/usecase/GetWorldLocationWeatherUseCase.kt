package com.diskin.alon.pagoda.weatherinfo.appservices.usecase

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.appservices.flatMapAppResult
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.WeatherRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.WorldLocationWeatherRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.model.WeatherDto
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Coordinate app operations to provide weather info for a world location.
 */
class GetWorldLocationWeatherUseCase @Inject constructor(
    private val weatherRepo: WeatherRepository,
    private val weatherUnitsMapper: WeatherUnitsSettingMapper
) : UseCase<WorldLocationWeatherRequest, Observable<AppResult<WeatherDto>>> {

    override fun execute(param: WorldLocationWeatherRequest): Observable<AppResult<WeatherDto>> {
        return weatherRepo.getLocationWeather(param.lat,param.lon)
            .flatMapAppResult(weatherUnitsMapper::mapWeather)
    }
}