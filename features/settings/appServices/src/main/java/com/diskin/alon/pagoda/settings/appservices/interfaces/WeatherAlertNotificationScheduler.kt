package com.diskin.alon.pagoda.settings.appservices.interfaces

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.settings.appservices.model.AlertInfo
import io.reactivex.Completable
import io.reactivex.Single

interface WeatherAlertNotificationScheduler {

    fun schedule(info: AlertInfo): Single<AppResult<Unit>>
}