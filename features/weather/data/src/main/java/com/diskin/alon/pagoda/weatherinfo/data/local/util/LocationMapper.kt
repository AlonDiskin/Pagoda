package com.diskin.alon.pagoda.weatherinfo.data.local.util

import androidx.paging.PagingData
import androidx.paging.map
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.data.local.model.LocationEntity
import com.diskin.alon.pagoda.weatherinfo.domain.Coordinates
import com.diskin.alon.pagoda.weatherinfo.domain.Location
import javax.inject.Inject

class LocationMapper @Inject constructor() : Mapper<PagingData<LocationEntity>,PagingData<Location>> {
    override fun map(source: PagingData<LocationEntity>): PagingData<Location> {
        return source.map {
            Location(
                Coordinates(it.lat,it.lon),
                it.name,
                it.country,
                it.state,
                it.bookmarked
            )
        }
    }
}