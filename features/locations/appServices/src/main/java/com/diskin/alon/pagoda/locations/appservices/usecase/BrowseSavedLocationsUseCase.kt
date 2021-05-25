package com.diskin.alon.pagoda.locations.appservices.usecase

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.locations.appservices.model.LocationDto
import com.diskin.alon.pagoda.locations.domain.Location
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Coordinate app operations to provide a listing of user saved [Location]s.
 */
class BrowseSavedLocationsUseCase @Inject constructor(
    private val repository: LocationRepository,
    private val mapper: Mapper<PagingData<Location>, PagingData<LocationDto>>
) : UseCase<Unit, Observable<PagingData<LocationDto>>>  {

    override fun execute(param: Unit): Observable<PagingData<LocationDto>> {
        return repository.getSaved()
            .map(mapper::map)
    }
}