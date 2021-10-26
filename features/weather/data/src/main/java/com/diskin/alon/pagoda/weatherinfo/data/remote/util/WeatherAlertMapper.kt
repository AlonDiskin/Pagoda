package com.diskin.alon.pagoda.weatherinfo.data.remote.util

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiWeatherAlertResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.WeatherAlert
import javax.inject.Inject

class WeatherAlertMapper @Inject constructor() : Mapper<ApiWeatherAlertResponse, WeatherAlert> {
    override fun map(source: ApiWeatherAlertResponse): WeatherAlert {
        return WeatherAlert(source.alerts?.first()?.event ?: "")
    }
}