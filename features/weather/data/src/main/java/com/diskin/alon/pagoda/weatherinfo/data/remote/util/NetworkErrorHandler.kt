package com.diskin.alon.pagoda.weatherinfo.data.remote.util

import com.diskin.alon.pagoda.common.appservices.results.AppError
import com.diskin.alon.pagoda.common.appservices.results.AppErrorHandler
import com.diskin.alon.pagoda.common.appservices.results.ErrorType
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Network error handling class.
 */
@Singleton
class NetworkErrorHandler @Inject constructor() : AppErrorHandler {

    override fun handle(throwable: Throwable): AppError {
        return when (throwable) {
            // Retrofit calls that return the body type throw either IOException for
            // network failures, or HttpException for any non-2xx HTTP status codes.
            // This code reports all errors to the UI
            is IOException -> AppError(ErrorType.DEVICE_NETWORK)
            is HttpException -> AppError(ErrorType.REMOTE_SERVER)
            else -> AppError(ErrorType.UNKNOWN_ERR)
        }
    }
}