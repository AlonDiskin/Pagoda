package com.diskin.alon.pagoda.common.presentation

import com.diskin.alon.pagoda.common.appservices.AppError

sealed class ErrorViewData {

    object NoError : ErrorViewData()

    data class Error(val appError: AppError): ErrorViewData()
}