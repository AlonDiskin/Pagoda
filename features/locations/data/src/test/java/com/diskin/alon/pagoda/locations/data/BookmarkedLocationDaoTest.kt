package com.diskin.alon.pagoda.locations.data

import android.content.Context
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.pagoda.locations.data.local.BookmarkedLocationDao
import com.diskin.alon.pagoda.locations.data.local.BookmarkedLocationEntity
import com.diskin.alon.pagoda.locations.data.local.LocationDao
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * [LocationDao] integration test class.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class BookmarkedLocationDaoTest {

    // System under test
    private lateinit var dao: BookmarkedLocationDao
    private lateinit var db: TestDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.bookmarkedLocationDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getAllStartWithWhenQueried() = runBlocking {
        // Given
        val insert1 = "INSERT INTO bookmarked_locations (lat, lon)" +
                "VALUES (10.3, 23.4)"
        val insert2 = "INSERT INTO bookmarked_locations (lat, lon)" +
                "VALUES (40.3, 23.4)"
        val insert3 = "INSERT INTO bookmarked_locations (lat, lon)" +
                "VALUES (10.3, 53.4)"

        db.compileStatement(insert1).executeInsert()
        db.compileStatement(insert2).executeInsert()
        db.compileStatement(insert3).executeInsert()

        // When
        val result = dao.getAll().load(
            LoadParams.Refresh(null,20,false)
        ) as LoadResult.Page<Int, BookmarkedLocationEntity>

        // Then
        assertThat(result.data.size).isEqualTo(3)
    }
}