package com.diskin.alon.pagoda.weatherinfo.infrastructure

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UserLocation
import com.google.android.gms.location.LocationResult
import javax.inject.Inject

class LocationMapper @Inject constructor() : Mapper<LocationResult, UserLocation> {
    override fun map(source: LocationResult): UserLocation {
        return UserLocation(
            source.lastLocation.latitude,
            source.lastLocation.longitude
        )
    }
}