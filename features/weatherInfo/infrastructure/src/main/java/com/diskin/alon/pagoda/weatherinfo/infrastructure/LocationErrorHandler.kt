package com.diskin.alon.pagoda.weatherinfo.infrastructure

import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.AppErrorHandler
import com.diskin.alon.pagoda.common.appservices.ErrorType
import com.google.android.gms.common.api.ResolvableApiException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Device location errors handling class.
 */
@Singleton
class LocationErrorHandler @Inject constructor() : AppErrorHandler {

    override fun handle(throwable: Throwable): AppError {
        return when(throwable) {
            //  Thrown by system if app has no location permission
            is SecurityException -> AppError(ErrorType.LOCATION_PERMISSION)
            //  Thrown by system if needed location settings are turned off in device
            is ResolvableApiException -> AppError(ErrorType.DEVICE_LOCATION,throwable)
            else -> AppError(ErrorType.UNKNOWN_ERR)
        }
    }
}