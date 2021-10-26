package com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces

import com.diskin.alon.pagoda.weatherinfo.data.remote.model.WeatherAlert
import io.reactivex.Single

interface WeatherAlertProvider {

    fun get(): Single<WeatherAlert>
}