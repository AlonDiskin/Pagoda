package com.diskin.alon.pagoda.locations.appservices.usecase

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.appservices.UseCase
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.locations.appservices.model.LocationSearchResult
import com.diskin.alon.pagoda.locations.appservices.model.SearchLocationsRequest
import com.diskin.alon.pagoda.locations.domain.Location
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Coordinate app operations to perform world locations search. Use case will perform search on queries
 * of size of 3 letters at least. otherwise will return an empty result set.
 */
class SearchLocationsUseCase @Inject constructor(
    private val repository: LocationRepository,
    private val locationMapper: Mapper<PagingData<Location>, PagingData<LocationSearchResult>>
) : UseCase<SearchLocationsRequest,Observable<PagingData<LocationSearchResult>>>{

    companion object {
        const val MIN_QUERY_SIZE = 2
    }

    override fun execute(param: SearchLocationsRequest): Observable<PagingData<LocationSearchResult>> {
        return when {
            (param.query.length <= MIN_QUERY_SIZE) -> Observable.just(PagingData.empty())
            else -> repository.search(param.query).map(locationMapper::map)
        }
    }
}