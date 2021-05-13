package com.diskin.alon.pagoda.settings.presentation

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.settings.appservices.model.ScheduleAlertRequest
import io.reactivex.Single

data class ScheduleAlertModelRequest(
    val enable: Boolean
) : ModelRequest<ScheduleAlertRequest,Single<AppResult<Unit>>>(ScheduleAlertRequest(enable))