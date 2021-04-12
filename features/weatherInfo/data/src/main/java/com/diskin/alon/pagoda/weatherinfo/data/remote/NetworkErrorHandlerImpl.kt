package com.diskin.alon.pagoda.weatherinfo.data.remote

import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.weatherinfo.errors.DEVICE_NETWORK
import com.diskin.alon.pagoda.weatherinfo.errors.REMOTE_SERVER
import com.diskin.alon.pagoda.weatherinfo.errors.UNKNOWN_ERR
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Network error handling class.
 */
class NetworkErrorHandlerImpl @Inject constructor() : NetworkErrorHandler {

    override fun handle(throwable: Throwable): AppError {
        return when (throwable) {
            // Retrofit calls that return the body type throw either IOException for
            // network failures, or HttpException for any non-2xx HTTP status codes.
            // This code reports all errors to the UI
            is IOException -> AppError(DEVICE_NETWORK,true)
            is HttpException -> AppError(REMOTE_SERVER,true)
            else -> AppError(UNKNOWN_ERR,false)
        }
    }
}