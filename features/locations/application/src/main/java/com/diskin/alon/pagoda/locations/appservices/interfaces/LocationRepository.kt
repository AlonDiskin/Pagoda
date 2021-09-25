package com.diskin.alon.pagoda.locations.appservices.interfaces

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.locations.domain.Coordinates
import com.diskin.alon.pagoda.locations.domain.Location
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * [Location] repository contract.
 */
interface LocationRepository {

    fun search(query: String): Observable<PagingData<Location>>

    fun getBookmarked(): Observable<PagingData<Location>>

    fun unBookmark(id: Coordinates): Single<AppResult<Unit>>

    fun bookmark(id: Coordinates): Single<AppResult<Unit>>
}