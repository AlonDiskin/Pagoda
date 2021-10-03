package com.diskin.alon.pagoda.weatherinfo.data

import android.content.Context
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.LocationDao
import com.diskin.alon.pagoda.weatherinfo.data.local.model.LocationEntity
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
class LocationDaoTest {

    // System under test
    private lateinit var dao: LocationDao
    private lateinit var db: TestDatabase

    @Before
    fun setUp() {
        // Init system under test
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.locationDao()

        // Populate test db
        val insert1 = "INSERT INTO locations (lat, lon, name, country, state, bookmarked)" +
                "VALUES (10.3, 23.4, 'London', 'England', 'London', 1)"
        val insert2 = "INSERT INTO locations (lat, lon, name, country, state, bookmarked)" +
                "VALUES (40.3, 23.4, 'Moscow', 'Russia', 'Moscow', 0)"
        val insert3 = "INSERT INTO locations (lat, lon, name, country, state, bookmarked)" +
                "VALUES (10.3, 53.4, 'Long Beach', 'USA', 'California', 0)"
        val insert4 = "INSERT INTO locations (lat, lon, name, country, state, bookmarked)" +
                "VALUES (10.3, -23.4, 'Jerusalem', 'Israel', 'Israel', 0)"
        val insert5 = "INSERT INTO locations (lat, lon, name, country, state, bookmarked)" +
                "VALUES (100.3, 23.4, 'Rome', 'Italy', 'Rome', 0)"
        val insert6 = "INSERT INTO locations (lat, lon, name, country, state, bookmarked)" +
                "VALUES (-10.3, 23.4, 'Longwood', 'USA', 'Florida', 1)"

        db.compileStatement(insert1).executeInsert()
        db.compileStatement(insert2).executeInsert()
        db.compileStatement(insert3).executeInsert()
        db.compileStatement(insert4).executeInsert()
        db.compileStatement(insert5).executeInsert()
        db.compileStatement(insert6).executeInsert()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getAllStartWithWhenQueried() = runBlocking {
        // Given

        // When
        val result = dao.getStartsWith("lon").load(
            LoadParams.Refresh(null,20,false)
        ) as LoadResult.Page<Int, LocationEntity>

        // Then
        assertThat(result.data.size).isEqualTo(3)
    }

    @Test
    fun getBookmarkedWhenQueried() = runBlocking {
        // Given

        // When
        val result = dao.getBookmarked().load(
            LoadParams.Refresh(null,20,false)
        ) as LoadResult.Page<Int, LocationEntity>

        // Then
        assertThat(result.data.size).isEqualTo(2)
    }

    @Test
    fun updateUnBookmarkedLocationWhenBookmarked() = runBlocking {
        // Given

        // When
        dao.bookmark(100.3, 23.4).blockingAwait()

        // Then
        val bookmarked = dao.getBookmarked().load(
            LoadParams.Refresh(null,20,false)
        ) as LoadResult.Page<Int, LocationEntity>
        assertThat(bookmarked.data.size).isEqualTo(3)
    }

    @Test
    fun updateBookmarkLocationWhenUnBookmarked() = runBlocking {
        // Given

        // When
        dao.unBookmark(-10.3, 23.4).blockingAwait()

        // Then
        val bookmarked = dao.getBookmarked().load(
            LoadParams.Refresh(null,20,false)
        ) as LoadResult.Page<Int, LocationEntity>
        assertThat(bookmarked.data.size).isEqualTo(1)
    }
}