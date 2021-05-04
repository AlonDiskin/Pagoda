package com.diskin.alon.pagoda.settings.data.local

import com.diskin.alon.pagoda.settings.data.model.UserLocation
import io.reactivex.Single

interface CurrentLocationProvider {

    fun get(): Single<UserLocation>
}