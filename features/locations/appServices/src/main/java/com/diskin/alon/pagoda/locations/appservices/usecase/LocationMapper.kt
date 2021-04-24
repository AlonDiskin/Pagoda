package com.diskin.alon.pagoda.locations.appservices.usecase

import androidx.paging.PagingData
import androidx.paging.map
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.model.LocationSearchResult
import com.diskin.alon.pagoda.locations.domain.Location
import javax.inject.Inject

class LocationMapper @Inject constructor() : Mapper<PagingData<Location>, PagingData<LocationSearchResult>> {
    override fun map(source: PagingData<Location>): PagingData<LocationSearchResult> {
        return source.map {
            LocationSearchResult(
                it.id.lat,
                it.id.lon,
                it.name,
                it.country,
                it.state
            )
        }
    }
}