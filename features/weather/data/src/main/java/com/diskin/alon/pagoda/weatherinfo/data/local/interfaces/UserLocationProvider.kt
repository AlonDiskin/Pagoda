package com.diskin.alon.pagoda.weatherinfo.data.local.interfaces

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.weatherinfo.data.local.model.UserLocation
import io.reactivex.Observable
import io.reactivex.Single

interface UserLocationProvider {

    fun getLocation(): Observable<AppResult<UserLocation>>
}