package com.diskin.alon.pagoda.locations.presentation.model

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.presentation.ModelRequest
import com.diskin.alon.pagoda.locations.appservices.model.LocationSearchResult
import com.diskin.alon.pagoda.locations.appservices.model.SearchLocationsRequest
import io.reactivex.Observable

data class SearchModelRequest(
    val query: String
) : ModelRequest<SearchLocationsRequest,Observable<PagingData<LocationSearchResult>>>(
    SearchLocationsRequest(query)
)