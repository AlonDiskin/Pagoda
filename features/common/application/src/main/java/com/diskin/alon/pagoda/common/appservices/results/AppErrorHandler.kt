package com.diskin.alon.pagoda.common.appservices.results

/**
 * Application error handling contract.
 */
interface AppErrorHandler {

    fun handle(throwable: Throwable): AppError
}
