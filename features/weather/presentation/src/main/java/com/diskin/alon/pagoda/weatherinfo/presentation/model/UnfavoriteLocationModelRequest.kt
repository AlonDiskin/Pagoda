package com.diskin.alon.pagoda.weatherinfo.presentation.model

import com.diskin.alon.pagoda.common.appservices.results.AppResult
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UnfavoriteLocationRequest
import io.reactivex.Single

data class UnfavoriteLocationModelRequest(
    val lat: Double,
    val lon: Double
) : ModelRequest<UnfavoriteLocationRequest,Single<AppResult<Unit>>>(UnfavoriteLocationRequest(lat, lon))