package com.diskin.alon.pagoda.weatherinfo.presentation.util

import androidx.paging.PagingData
import androidx.paging.map
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationDto
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiBookmarkedLocation
import io.reactivex.Observable
import javax.inject.Inject

class BookmarkedLocationMapper @Inject constructor() : Mapper<Observable<PagingData<LocationDto>>,Observable<PagingData<UiBookmarkedLocation>>> {

    override fun map(source: Observable<PagingData<LocationDto>>): Observable<PagingData<UiBookmarkedLocation>> {
        return source.map {
            it.map { dto ->
                UiBookmarkedLocation(
                    dto.id.lat,
                    dto.id.lon,
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