package com.diskin.alon.pagoda.locations.appservices.util

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.model.UnBookmarkLocationRequest
import com.diskin.alon.pagoda.locations.domain.Coordinates
import javax.inject.Inject

class UnBookmarkLocationRequestMapper @Inject constructor() : Mapper<UnBookmarkLocationRequest, Coordinates> {

    override fun map(source: UnBookmarkLocationRequest): Coordinates {
        return Coordinates(source.lat,source.lon)
    }
}