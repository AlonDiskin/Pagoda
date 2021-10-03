package com.diskin.alon.pagoda.weatherinfo.presentation.model

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.model.SearchLocationsRequest
import io.reactivex.Observable

data class SearchLocationsModelRequest(
    val query: String
) : ModelRequest<SearchLocationsRequest,Observable<PagingData<UiLocationSearchResult>>>(
    SearchLocationsRequest(query)
)