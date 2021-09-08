package com.diskin.alon.pagoda.weatherinfo.appservices.usecase

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.appservices.flatMapAppResult
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.WeatherRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.WeatherDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UserLocationWeatherRequest
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Coordinate app operations to provide weather info for current user location.
 */
class GetUserLocationWeatherUseCase @Inject constructor(
    private val weatherRepo: WeatherRepository,
    private val weatherUnitsMapper: WeatherUnitsSettingMapper
) : UseCase<UserLocationWeatherRequest,Observable<AppResult<WeatherDto>>> {

    override fun execute(param: UserLocationWeatherRequest): Observable<AppResult<WeatherDto>> {
        return weatherRepo
            .getCurrentLocationWeather()
            .flatMapAppResult(weatherUnitsMapper::mapWeather)
    }
}