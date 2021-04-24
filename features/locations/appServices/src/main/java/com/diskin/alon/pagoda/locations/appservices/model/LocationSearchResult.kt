package com.diskin.alon.pagoda.locations.appservices.model

/**
 * Location Search result data.
 *
 * @param lat geo coordinates latitude value.
 * @param lon geo coordinates longitude value.
 * @param name location name
 * @param country location country, optionally empty,  if location has non
 * @param state location country, optionally empty,  if location has non
 */
data class LocationSearchResult(val lat: Double,
                                val lon: Double,
                                val name: String,
                                val country: String,
                                val state: String)