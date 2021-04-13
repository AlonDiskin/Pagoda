package com.diskin.alon.pagoda.locations.presentation

import com.diskin.alon.pagoda.locations.appservices.model.LocationSearchResult

fun createSearchResults(): List<LocationSearchResult> {
    return listOf(
        LocationSearchResult(
            10.0,
            20.7,
            "Location1",
            "Country1",
            "State1"
        ),
        LocationSearchResult(
            10.0,
            20.7,
            "Location2",
            "Country2",
            "State2"
        ),
        LocationSearchResult(
            10.0,
            20.7,
            "Location3",
            "Country3",
            "State3"
        ),
        LocationSearchResult(
            10.0,
            20.7,
            "Location4",
            "Country4",
            "State4"
        )
    )
}