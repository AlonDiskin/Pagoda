package com.diskin.alon.pagoda.weatherinfo.appservices.usecase

import com.diskin.alon.pagoda.common.appservices.*
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.AppPrefsStore
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.WeatherRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherDto
import com.diskin.alon.pagoda.weatherinfo.domain.LocationWeather
import com.diskin.alon.pagoda.weatherinfo.domain.UnitSystem
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Coordinate app operations to provide weather info for current user location.
 */
class GetCurrentWeatherUseCase @Inject constructor(
    private val weatherRepo: WeatherRepository,
    private val locationProvider: UserLocationProvider,
    private val prefsStore: AppPrefsStore,
    private val mapper: Mapper<LocationWeather,LocationWeatherDto>
) : UseCase<Unit, Observable<Result<LocationWeatherDto>>> {

    override fun execute(param: Unit): Observable<Result<LocationWeatherDto>> {
        return locationProvider
            .getCurrentLocation().toData()
            .switchMap { getWeather(it.lat,it.lon) }
            .toResult()
            .mapResult(mapper::map)
    }

    private fun getWeather(lat: Double, lon: Double): Observable<LocationWeather> {
        return Observable.combineLatest(
            weatherRepo.get(lat,lon).toData(),
            prefsStore.getUnitSystem().toData(),
            { weather, pref ->
                when(pref) {
                    UnitSystem.METRIC -> {
                        if (weather.unitSystem != UnitSystem.METRIC) weather.toMetric()
                    }
                    UnitSystem.IMPERIAL -> {
                        if (weather.unitSystem != UnitSystem.IMPERIAL) weather.toImperial()
                    }
                }

                weather
            })
    }
}