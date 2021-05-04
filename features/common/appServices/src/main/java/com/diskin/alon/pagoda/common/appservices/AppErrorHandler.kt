package com.diskin.alon.pagoda.common.appservices

/**
 * Application error handling contract.
 */
interface AppErrorHandler {

    fun handle(throwable: Throwable): AppError
}
