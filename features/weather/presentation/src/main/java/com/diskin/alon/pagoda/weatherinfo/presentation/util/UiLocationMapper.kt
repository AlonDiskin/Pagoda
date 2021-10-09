package com.diskin.alon.pagoda.weatherinfo.presentation.util

import androidx.paging.PagingData
import androidx.paging.map
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationDto
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiLocation
import io.reactivex.Observable
import javax.inject.Inject

class UiLocationMapper @Inject constructor() : Mapper<Observable<PagingData<LocationDto>>,Observable<PagingData<UiLocation>>> {

    override fun map(source: Observable<PagingData<LocationDto>>): Observable<PagingData<UiLocation>> {
        return source.map {
            it.map { dto ->
                UiLocation(
                    dto.id.lat,
                    dto.id.lon,
                    dto.name,
                    mapLocationCountry(dto),
                    dto.isFavorite
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