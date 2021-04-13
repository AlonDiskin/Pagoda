package com.diskin.alon.pagoda.locations.data.local

import androidx.room.Entity

@Entity(tableName = "locations", primaryKeys = ["lat", "lon"])
data class LocationEntity(val lat: Double,
                          val lon: Double,
                          val name: String,
                          val country: String,
                          val state: String)