package com.diskin.alon.pagoda.locations.data.implementations

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.observable
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.locations.data.local.LocationDao
import com.diskin.alon.pagoda.locations.data.local.LocationEntity
import com.diskin.alon.pagoda.locations.domain.Location
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Handles data sources operations to provide [Location].
 */
class LocationRepositoryImpl @Inject constructor(
    private val locationDao: LocationDao,
    private val locationMapper: Mapper<PagingData<LocationEntity>, PagingData<Location>>
) : LocationRepository {

    companion object {
        const val PAGE_SIZE = 20
    }

    override fun search(query: String): Observable<PagingData<Location>> {
        return Pager(PagingConfig(PAGE_SIZE)) { locationDao.getAllStartWith(query) }
            .observable
            .subscribeOn(Schedulers.io())
            .map(locationMapper::map)
    }
}