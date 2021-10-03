package com.diskin.alon.pagoda.weatherinfo.appservices.usecase

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.SearchLocationsRequest
import com.diskin.alon.pagoda.weatherinfo.domain.Location
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Coordinate app operations to perform world locations search. Use case will perform search on queries
 * of size of 3 letters at least. otherwise will return an empty result set.
 */
class SearchLocationsUseCase @Inject constructor(
    private val repository: LocationRepository,
    private val locationMapper: Mapper<PagingData<Location>, PagingData<LocationDto>>
) : UseCase<SearchLocationsRequest,Observable<PagingData<LocationDto>>>{

    companion object {
        const val MIN_QUERY_SIZE = 2
    }

    override fun execute(param: SearchLocationsRequest): Observable<PagingData<LocationDto>> {
        return when {
            (param.query.length <= MIN_QUERY_SIZE) -> Observable.just(PagingData.empty())
            else -> repository.search(param.query).map(locationMapper::map)
        }
    }
}