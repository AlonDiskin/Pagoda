package com.diskin.alon.pagoda.weatherinfo.appservices.util

import androidx.paging.PagingData
import androidx.paging.map
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.model.CoordinatesDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationDto
import com.diskin.alon.pagoda.weatherinfo.domain.Location
import javax.inject.Inject

class LocationDtoMapper @Inject constructor() : Mapper<PagingData<Location>, PagingData<LocationDto>> {
    override fun map(source: PagingData<Location>): PagingData<LocationDto> {
        return source.map {
            LocationDto(
                CoordinatesDto(
                    it.id.lat,
                    it.id.lon
                ),
                it.name,
                it.country,
                it.state,
                it.isFavorite
            )
        }
    }
}