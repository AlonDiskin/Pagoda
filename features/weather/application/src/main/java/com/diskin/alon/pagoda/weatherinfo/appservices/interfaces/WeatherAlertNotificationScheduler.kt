package com.diskin.alon.pagoda.weatherinfo.appservices.interfaces

import com.diskin.alon.pagoda.common.appservices.results.AppResult
import com.diskin.alon.pagoda.weatherinfo.appservices.model.AlertInfoDto
import io.reactivex.Single

/**
 * Schedules and configures device notifications, for user location weather alerts.
 */
interface WeatherAlertNotificationScheduler {

    /**
     * Schedule an alert notification on user device.
     *
     * @param info the data needed for alert scheduling.
     */
    fun schedule(info: AlertInfoDto): Single<AppResult<Unit>>
}