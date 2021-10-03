package com.diskin.alon.pagoda.weatherinfo.appservices.util

import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.model.BookmarkLocationRequest
import com.diskin.alon.pagoda.weatherinfo.domain.Coordinates
import javax.inject.Inject

class BookmarkLocationRequestMapper @Inject constructor() : Mapper<BookmarkLocationRequest, Coordinates> {

    override fun map(source: BookmarkLocationRequest): Coordinates {
        return Coordinates(source.lat,source.lon)
    }
}