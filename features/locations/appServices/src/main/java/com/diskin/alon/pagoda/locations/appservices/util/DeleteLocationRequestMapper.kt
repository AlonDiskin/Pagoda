package com.diskin.alon.pagoda.locations.appservices.util

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.appservices.model.DeleteSavedLocationRequest
import com.diskin.alon.pagoda.locations.domain.Coordinates
import javax.inject.Inject

class DeleteLocationRequestMapper @Inject constructor() : Mapper<DeleteSavedLocationRequest, Coordinates> {

    override fun map(source: DeleteSavedLocationRequest): Coordinates {
        return Coordinates(source.lat,source.lon)
    }
}