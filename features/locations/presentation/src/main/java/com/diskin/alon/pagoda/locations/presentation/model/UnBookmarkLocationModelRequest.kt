package com.diskin.alon.pagoda.locations.presentation.model

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.locations.appservices.model.UnBookmarkLocationRequest
import io.reactivex.Single

data class UnBookmarkLocationModelRequest(
    val lat: Double,
    val lon: Double
) : ModelRequest<UnBookmarkLocationRequest,Single<AppResult<Unit>>>(UnBookmarkLocationRequest(lat, lon))