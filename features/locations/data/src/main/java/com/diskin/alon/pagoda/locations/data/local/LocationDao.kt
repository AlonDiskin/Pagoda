package com.diskin.alon.pagoda.locations.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import io.reactivex.Completable

@Dao
interface LocationDao {

    @Query("SELECT * FROM locations WHERE name LIKE :query || '%'")
    fun getStartsWith(query: String): PagingSource<Int, LocationEntity>

    @Query("SELECT * FROM locations WHERE bookmarked")
    fun getBookmarked(): PagingSource<Int, LocationEntity>

    @Query("UPDATE locations SET bookmarked = 1 WHERE lat = :lat AND lon = :lon")
    fun bookmark(lat: Double, lon: Double): Completable

    @Query("UPDATE locations SET bookmarked = 0 WHERE lat = :lat AND lon = :lon")
    fun unBookmark(lat: Double, lon: Double): Completable
}