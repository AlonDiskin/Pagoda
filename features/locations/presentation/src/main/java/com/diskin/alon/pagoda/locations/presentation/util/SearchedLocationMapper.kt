package com.diskin.alon.pagoda.locations.presentation.util

import androidx.paging.PagingData
import androidx.paging.map
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.model.LocationDto
import com.diskin.alon.pagoda.locations.presentation.model.UiLocationSearchResult
import io.reactivex.Observable
import javax.inject.Inject

class SearchedLocationMapper @Inject constructor() : Mapper<Observable<PagingData<LocationDto>>,Observable<PagingData<UiLocationSearchResult>>> {

    override fun map(source: Observable<PagingData<LocationDto>>): Observable<PagingData<UiLocationSearchResult>> {
        return source.map {
            it.map { dto ->
                UiLocationSearchResult(
                    dto.id.lat,
                    dto.id.lon,
                    dto.name,
                    mapLocationCountry(dto),
                    dto.bookmarked
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