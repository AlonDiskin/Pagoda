package com.diskin.alon.pagoda.weatherinfo.presentation.model

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.model.FavoriteLocationRequest
import io.reactivex.Single

data class FavoriteLocationModelRequest(
    val lat: Double,
    val lon: Double
) : ModelRequest<FavoriteLocationRequest,Single<AppResult<Unit>>>(FavoriteLocationRequest(lat, lon))