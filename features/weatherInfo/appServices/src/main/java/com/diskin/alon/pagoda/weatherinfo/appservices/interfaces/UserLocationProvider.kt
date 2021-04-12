package com.diskin.alon.pagoda.weatherinfo.appservices.interfaces

import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UserLocation
import io.reactivex.Observable

/**
 * User device location provider contract.
 */
interface UserLocationProvider {

    fun getCurrentLocation(): Observable<Result<UserLocation>>
}