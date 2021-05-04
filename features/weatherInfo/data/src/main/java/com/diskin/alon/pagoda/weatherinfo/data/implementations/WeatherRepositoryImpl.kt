package com.diskin.alon.pagoda.weatherinfo.data.implementations

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.WeatherRepository
import com.diskin.alon.pagoda.weatherinfo.data.remote.RemoteWeatherStore
import com.diskin.alon.pagoda.weatherinfo.domain.LocationWeather
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Handles data sources operations to provide location weather data.
 */
class WeatherRepositoryImpl @Inject constructor(
    private val remoteStore: RemoteWeatherStore
) : WeatherRepository {

    override fun get(
        lat: Double,
        lon: Double
    ): Observable<AppResult<LocationWeather>> {
        return remoteStore.get(lat, lon)
    }
}