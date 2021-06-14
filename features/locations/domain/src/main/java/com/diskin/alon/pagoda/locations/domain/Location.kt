package com.diskin.alon.pagoda.locations.domain

/**
 * Entity class that represent a world location.
 *
 * @param name location name
 * @param country location country, optionally empty,  if location has non
 * @param state location country, optionally empty,  if location has non
 */
data class Location(val id: Coordinates,
                    var name: String,
                    var country: String,
                    var state: String,
                    var bookmarked: Boolean) {

    init {
        require(name.isNotEmpty())
    }
}