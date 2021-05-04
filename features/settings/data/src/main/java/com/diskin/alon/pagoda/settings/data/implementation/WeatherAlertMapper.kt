package com.diskin.alon.pagoda.settings.data.implementation

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.settings.data.model.ApiWeatherAlertResponse
import com.diskin.alon.pagoda.settings.infrastructure.model.WeatherAlert
import javax.inject.Inject

class WeatherAlertMapper @Inject constructor() : Mapper<ApiWeatherAlertResponse, WeatherAlert> {
    override fun map(source: ApiWeatherAlertResponse): WeatherAlert {
        return WeatherAlert(source.alerts?.first()?.event ?: "")
    }
}