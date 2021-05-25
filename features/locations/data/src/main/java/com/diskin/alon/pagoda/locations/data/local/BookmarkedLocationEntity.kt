package com.diskin.alon.pagoda.locations.data.local

import androidx.room.Entity

@Entity(tableName = "bookmarked_locations", primaryKeys = ["lat", "lon"])
data class BookmarkedLocationEntity(val lat: Double,
                                    val lon: Double)