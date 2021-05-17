package com.diskin.alon.pagoda.locations.presentation.util

import androidx.paging.PagingData
import androidx.paging.map
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.model.LocationDto
import com.diskin.alon.pagoda.locations.presentation.model.UiLocation
import io.reactivex.Observable
import javax.inject.Inject

class LocationMapper @Inject constructor() : Mapper<Observable<PagingData<LocationDto>>,Observable<PagingData<UiLocation>>> {

    override fun map(source: Observable<PagingData<LocationDto>>): Observable<PagingData<UiLocation>> {
        return source.map {
            it.map { dto ->
                UiLocation(
                    dto.lat,
                    dto.lon,
                    dto.name,
                    mapLocationCountry(dto)
                )
            }
        }
    }

    private fun mapLocationCountry(dto: LocationDto): String {
        val country = if (dto.country.isEmpty()) "Unknown" else dto.country
        val state = if (dto.state.isEmpty())  "" else ", ${dto.state}"

        return country.plus(state)
    }
}