package com.diskin.alon.pagoda.settings.data.implementation

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.settings.data.local.CurrentLocationProvider
import com.diskin.alon.pagoda.settings.data.model.ApiWeatherAlertResponse
import com.diskin.alon.pagoda.settings.data.remote.OpenWeatherMapApi
import com.diskin.alon.pagoda.settings.infrastructure.interfaces.WeatherAlertProvider
import com.diskin.alon.pagoda.settings.infrastructure.model.WeatherAlert
import io.reactivex.Single
import javax.inject.Inject

class WeatherAlertProviderImpl @Inject constructor(
    private val locationProvider: CurrentLocationProvider,
    private val api: OpenWeatherMapApi,
    private val mapper: Mapper<ApiWeatherAlertResponse, WeatherAlert>
) : WeatherAlertProvider {

    override fun get(): Single<WeatherAlert> {
        return locationProvider.get()
            .flatMap {
                api.getWeatherAlert(it.lat,it.lon)
            }
            .map(mapper::map)
    }
}