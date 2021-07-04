package com.diskin.alon.pagoda.weatherinfo.data.local.util

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.data.local.model.UserLocation
import com.google.android.gms.location.LocationResult
import javax.inject.Inject

class UserLocationMapper @Inject constructor() : Mapper<LocationResult, UserLocation> {
    override fun map(source: LocationResult): UserLocation {
        return UserLocation(
            source.lastLocation.latitude,
            source.lastLocation.longitude
        )
    }
}