package com.diskin.alon.pagoda.weatherinfo.presentation.model

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import io.reactivex.Observable

object BookmarkedLocationsModelRequest : ModelRequest<Unit,Observable<PagingData<UiBookmarkedLocation>>>(Unit)