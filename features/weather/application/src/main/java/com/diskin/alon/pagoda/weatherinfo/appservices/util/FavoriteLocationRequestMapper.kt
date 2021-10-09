package com.diskin.alon.pagoda.weatherinfo.appservices.util

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.model.FavoriteLocationRequest
import com.diskin.alon.pagoda.weatherinfo.domain.Coordinates
import javax.inject.Inject

class FavoriteLocationRequestMapper @Inject constructor() : Mapper<FavoriteLocationRequest, Coordinates> {

    override fun map(source: FavoriteLocationRequest): Coordinates {
        return Coordinates(source.lat,source.lon)
    }
}