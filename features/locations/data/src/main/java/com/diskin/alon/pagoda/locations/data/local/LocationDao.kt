package com.diskin.alon.pagoda.locations.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query

@Dao
interface LocationDao {

    @Query("SELECT * FROM locations WHERE name LIKE :query || '%'")
    fun getAllStartWith(query: String): PagingSource<Int, LocationEntity>
}