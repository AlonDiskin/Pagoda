package com.diskin.alon.pagoda.locations.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import io.reactivex.Completable

@Dao
interface BookmarkedLocationDao {

    @Query("SELECT * FROM bookmarked_locations")
    fun getAll(): PagingSource<Int, BookmarkedLocationEntity>

    @Delete
    fun delete(bookmark: BookmarkedLocationEntity): Completable
}