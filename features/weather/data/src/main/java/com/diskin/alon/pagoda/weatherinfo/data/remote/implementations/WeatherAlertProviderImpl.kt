package com.diskin.alon.pagoda.weatherinfo.data.remote.implementations

import com.diskin.alon.pagoda.common.appservices.results.singleData
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.OpenWeatherMapApi
import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.WeatherAlertProvider
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiWeatherAlertResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.WeatherAlert
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class WeatherAlertProviderImpl @Inject constructor(
    private val locationProvider: UserLocationProvider,
    private val api: OpenWeatherMapApi,
    private val mapper: Mapper<ApiWeatherAlertResponse, WeatherAlert>
) : WeatherAlertProvider {
    override fun get(): Single<WeatherAlert> {
        return locationProvider.getLocation()
            .singleData()
            .flatMap {
                api.getWeatherAlert(it.lat,it.lon)
                    .subscribeOn(Schedulers.io())
            }
            .map(mapper::map)
    }
}