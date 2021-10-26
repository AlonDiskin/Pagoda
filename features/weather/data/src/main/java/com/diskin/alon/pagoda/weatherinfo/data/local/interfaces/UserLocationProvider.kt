package com.diskin.alon.pagoda.weatherinfo.data.local.interfaces

import com.diskin.alon.pagoda.common.appservices.results.Result
import com.diskin.alon.pagoda.weatherinfo.data.local.model.UserLocation
import io.reactivex.Single

interface UserLocationProvider {

    fun getLocation(): Single<Result<UserLocation>>
}