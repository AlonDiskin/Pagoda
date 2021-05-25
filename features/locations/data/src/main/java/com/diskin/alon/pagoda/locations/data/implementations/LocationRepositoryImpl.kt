package com.diskin.alon.pagoda.locations.data.implementations

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.observable
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.ErrorType
import com.diskin.alon.pagoda.common.appservices.toSingleResult
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.interfaces.LocationRepository
import com.diskin.alon.pagoda.locations.data.local.LocationDao
import com.diskin.alon.pagoda.locations.data.local.LocationEntity
import com.diskin.alon.pagoda.locations.domain.Coordinates
import com.diskin.alon.pagoda.locations.domain.Location
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Handles data sources operations to provide [Location].
 */
class LocationRepositoryImpl @Inject constructor(
    private val locationDao: LocationDao,
    private val locationMapper: Mapper<PagingData<LocationEntity>, PagingData<Location>>
) : LocationRepository {

    companion object { const val PAGE_SIZE = 20 }

    override fun search(query: String): Observable<PagingData<Location>> {
        return Pager(PagingConfig(PAGE_SIZE)) { locationDao.getStartsWith(query) }
            .observable
            .subscribeOn(Schedulers.io())
            .map(locationMapper::map)
    }

    override fun getBookmarked(): Observable<PagingData<Location>> {
        return Pager(PagingConfig(PAGE_SIZE)) { locationDao.getBookmarked() }
            .observable
            .subscribeOn(Schedulers.io())
            .map(locationMapper::map)
    }

    override fun unBookmark(id: Coordinates): Single<AppResult<Unit>> {
        return locationDao.unBookmark(id.lat,id.lon)
            .toSingleDefault(Unit)
            .subscribeOn(Schedulers.io())
            .toSingleResult(::handleBookmarkRemoveError)
    }

    override fun bookmark(id: Coordinates): Single<AppResult<Unit>> {
        return locationDao.bookmark(id.lat,id.lon)
            .toSingleDefault(Unit)
            .subscribeOn(Schedulers.io())
            .toSingleResult(::handleBookmarkRemoveError)
    }

    private fun handleBookmarkRemoveError(throwable: Throwable): AppError {
        return AppError(ErrorType.DB_ERROR)
    }
}