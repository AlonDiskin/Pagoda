package com.diskin.alon.pagoda.weatherinfo.appservices.interfaces

import androidx.paging.PagingData
import com.diskin.alon.pagoda.common.appservices.results.AppResult
import com.diskin.alon.pagoda.weatherinfo.domain.Coordinates
import com.diskin.alon.pagoda.weatherinfo.domain.Location
import io.reactivex.Observable
import io.reactivex.Single

/**
 * [Location] repository contract.
 */
interface LocationRepository {

    fun search(query: String): Observable<PagingData<Location>>

    fun getFavorite(): Observable<PagingData<Location>>

    fun unfavorite(id: Coordinates): Single<AppResult<Unit>>

    fun favorite(id: Coordinates): Single<AppResult<Unit>>
}