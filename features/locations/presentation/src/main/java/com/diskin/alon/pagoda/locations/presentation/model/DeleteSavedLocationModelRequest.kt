package com.diskin.alon.pagoda.locations.presentation.model

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.locations.appservices.model.DeleteSavedLocationRequest
import io.reactivex.Single

data class DeleteSavedLocationModelRequest(
    val lat: Double,
    val lon: Double
) : ModelRequest<DeleteSavedLocationRequest,Single<AppResult<Unit>>>(DeleteSavedLocationRequest(lat, lon))