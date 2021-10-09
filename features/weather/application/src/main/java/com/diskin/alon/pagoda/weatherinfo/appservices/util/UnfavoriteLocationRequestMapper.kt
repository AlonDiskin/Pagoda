package com.diskin.alon.pagoda.weatherinfo.appservices.util

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UnfavoriteLocationRequest
import com.diskin.alon.pagoda.weatherinfo.domain.Coordinates
import javax.inject.Inject

class UnfavoriteLocationRequestMapper @Inject constructor() : Mapper<UnfavoriteLocationRequest, Coordinates> {

    override fun map(source: UnfavoriteLocationRequest): Coordinates {
        return Coordinates(source.lat,source.lon)
    }
}