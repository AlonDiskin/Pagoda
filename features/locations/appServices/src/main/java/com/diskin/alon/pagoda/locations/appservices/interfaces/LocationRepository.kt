package com.diskin.alon.pagoda.locations.appservices.interfaces

import androidx.paging.PagingData
import com.diskin.alon.pagoda.locations.domain.Location
import io.reactivex.Observable

/**
 * [Location] repository contract.
 */
interface LocationRepository {

    fun search(query: String): Observable<PagingData<Location>>

    fun getSaved(): Observable<PagingData<Location>>
}