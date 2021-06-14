package com.diskin.alon.pagoda.locations.presentation.model

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.locations.appservices.model.SearchLocationsRequest
import io.reactivex.Observable

data class SearchLocationsModelRequest(
    val query: String
) : ModelRequest<SearchLocationsRequest,Observable<PagingData<UiLocationSearchResult>>>(
    SearchLocationsRequest(query)
)