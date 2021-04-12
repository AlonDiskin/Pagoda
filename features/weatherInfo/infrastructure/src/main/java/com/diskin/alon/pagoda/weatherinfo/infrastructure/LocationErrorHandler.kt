package com.diskin.alon.pagoda.weatherinfo.infrastructure

import com.diskin.alon.pagoda.common.appservices.AppError

/**
 * Device location error handling contract.
 */
interface LocationErrorHandler {

    fun handle(throwable: Throwable): AppError
}
