package com.diskin.alon.pagoda.locations.appservices.model

/**
 * Location data.
 *
 * @param lat geo coordinates latitude value.
 * @param lon geo coordinates longitude value.
 * @param name location name
 * @param country location country, optionally empty,  if location has non
 * @param state location country, optionally empty,  if location has non
 */
data class LocationDto(val id: CoordinatesDto,
                       val name: String,
                       val country: String,
                       val state: String)