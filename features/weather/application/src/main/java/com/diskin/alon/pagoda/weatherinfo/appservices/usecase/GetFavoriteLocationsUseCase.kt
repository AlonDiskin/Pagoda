package com.diskin.alon.pagoda.weatherinfo.appservices.usecase

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationDto
import com.diskin.alon.pagoda.weatherinfo.domain.Location
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Coordinate app operations to provide a listing of user saved [Location]s.
 */
class GetFavoriteLocationsUseCase @Inject constructor(
    private val repository: LocationRepository,
    private val mapper: Mapper<PagingData<Location>, PagingData<LocationDto>>
) : UseCase<Unit, Observable<PagingData<LocationDto>>>  {

    override fun execute(param: Unit): Observable<PagingData<LocationDto>> {
        return repository.getFavorite()
            .map(mapper::map)
    }
}