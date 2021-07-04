package com.diskin.alon.pagoda.settings.appservices.usecase

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.settings.appservices.interfaces.WeatherAlertNotificationScheduler
import com.diskin.alon.pagoda.settings.appservices.model.AlertInfo
import com.diskin.alon.pagoda.settings.appservices.model.ScheduleAlertRequest
import io.reactivex.Single
import javax.inject.Inject

/**
 * Coordinate app operations to schedule weather alerts.
 */
class ScheduleWeatherAlertNotificationUseCase @Inject constructor(
    private val scheduler: WeatherAlertNotificationScheduler,
    private val mapper: Mapper<ScheduleAlertRequest,AlertInfo>
) : UseCase<ScheduleAlertRequest,Single<AppResult<Unit>>> {

    override fun execute(param: ScheduleAlertRequest): Single<AppResult<Unit>> {
        return scheduler.schedule(mapper.map(param))
    }
}