package com.diskin.alon.pagoda.settings.infrastructure.interfaces

import com.diskin.alon.pagoda.settings.infrastructure.model.WeatherAlert
import io.reactivex.Single

interface WeatherAlertProvider {

    fun get(): Single<WeatherAlert>
}