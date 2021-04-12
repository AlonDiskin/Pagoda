package com.diskin.alon.pagoda.weatherinfo.data.remote

import com.diskin.alon.pagoda.common.appservices.AppError

/**
 * Network error handling contract.
 */
interface NetworkErrorHandler {

    fun handle(throwable: Throwable): AppError
}
