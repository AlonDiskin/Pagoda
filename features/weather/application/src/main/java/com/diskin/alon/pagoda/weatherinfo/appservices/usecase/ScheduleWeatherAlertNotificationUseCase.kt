package com.diskin.alon.pagoda.weatherinfo.appservices.usecase

import com.diskin.alon.pagoda.common.appservices.results.AppResult
import com.diskin.alon.pagoda.common.appservices.usecase.UseCase
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.WeatherAlertNotificationScheduler
import com.diskin.alon.pagoda.weatherinfo.appservices.model.ScheduleWeatherAlertNotificationRequest
import io.reactivex.Single
import javax.inject.Inject

class ScheduleWeatherAlertNotificationUseCase @Inject constructor(
    private val scheduler: WeatherAlertNotificationScheduler
) : UseCase<ScheduleWeatherAlertNotificationRequest, Single<AppResult<Unit>>> {

    override fun execute(param: ScheduleWeatherAlertNotificationRequest): Single<AppResult<Unit>> {
        return scheduler.schedule(param.alertInfoDto)
    }
}