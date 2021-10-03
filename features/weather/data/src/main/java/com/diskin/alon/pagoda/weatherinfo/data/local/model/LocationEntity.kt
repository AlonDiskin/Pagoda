package com.diskin.alon.pagoda.weatherinfo.data.local.model

import androidx.room.Entity

@Entity(tableName = "locations", primaryKeys = ["lat", "lon"])
data class LocationEntity(val lat: Double,
                          val lon: Double,
                          val name: String,
                          val country: String,
                          val state: String,
                          val bookmarked: Boolean)