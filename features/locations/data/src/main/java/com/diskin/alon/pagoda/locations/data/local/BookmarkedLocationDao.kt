package com.diskin.alon.pagoda.locations.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query

@Dao
interface BookmarkedLocationDao {

    @Query("SELECT * FROM bookmarked_locations")
    fun getAll(): PagingSource<Int, BookmarkedLocationEntity>
}