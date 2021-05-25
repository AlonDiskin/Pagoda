package com.diskin.alon.pagoda.locations.presentation.model

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.locations.appservices.model.BookmarkLocationRequest
import io.reactivex.Single

data class BookmarkLocationModelRequest(
    val lat: Double,
    val lon: Double
) : ModelRequest<BookmarkLocationRequest,Single<AppResult<Unit>>>(BookmarkLocationRequest(lat, lon))