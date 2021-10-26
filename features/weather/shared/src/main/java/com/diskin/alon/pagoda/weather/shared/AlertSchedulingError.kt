package com.diskin.alon.pagoda.weather.shared

import com.diskin.alon.pagoda.common.appservices.results.AppError

/**
 * Hold the data associated with alert scheduling fail.
 */
data class AlertSchedulingError(val error: AppError)