package com.diskin.alon.pagoda.settings.appservices.util

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.settings.appservices.model.AlertInfo
import com.diskin.alon.pagoda.settings.appservices.model.ScheduleAlertRequest
import javax.inject.Inject

class SchedulingRequestMapper @Inject constructor() : Mapper<ScheduleAlertRequest, AlertInfo> {

    override fun map(source: ScheduleAlertRequest): AlertInfo {
        return AlertInfo(source.enable)
    }
}