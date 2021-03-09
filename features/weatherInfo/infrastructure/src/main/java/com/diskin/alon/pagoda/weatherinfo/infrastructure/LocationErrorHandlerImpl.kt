package com.diskin.alon.pagoda.weatherinfo.infrastructure

import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.weatherinfo.errors.DEVICE_LOCATION
import com.diskin.alon.pagoda.weatherinfo.errors.LOCATION_PERMISSION
import com.diskin.alon.pagoda.weatherinfo.errors.UNKNOWN_ERR
import com.google.android.gms.common.api.ResolvableApiException
import javax.inject.Inject

/**
 * Device location errors handling class.
 */
class LocationErrorHandlerImpl @Inject constructor() : LocationErrorHandler {

    override fun handle(throwable: Throwable): AppError {
        return when(throwable) {
            //  Thrown by system if app has no location permission
            is SecurityException -> AppError(LOCATION_PERMISSION,true)
            //  Thrown by system if needed location settings are turned off in device
            is ResolvableApiException -> AppError(DEVICE_LOCATION,true,throwable)
            else -> AppError(UNKNOWN_ERR,false)
        }
    }
}