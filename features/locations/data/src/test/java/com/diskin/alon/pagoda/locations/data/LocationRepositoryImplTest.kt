package com.diskin.alon.pagoda.locations.data

import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.locations.data.implementations.LocationRepositoryImpl
import com.diskin.alon.pagoda.locations.data.local.BookmarkedLocationDao
import com.diskin.alon.pagoda.locations.data.local.BookmarkedLocationEntity
import com.diskin.alon.pagoda.locations.data.local.LocationDao
import com.diskin.alon.pagoda.locations.data.local.LocationEntity
import com.diskin.alon.pagoda.locations.domain.Coordinates
import com.diskin.alon.pagoda.locations.domain.Location
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

/**
 * [LocationRepositoryImpl] unit test class.
 */
class LocationRepositoryImplTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var repository: LocationRepositoryImpl

    // Collaborators
    private val locationDao: LocationDao = mockk()
    private val bookmarkedDao: BookmarkedLocationDao = mockk()
    private val locationMapper: Mapper<PagingData<LocationEntity>, PagingData<Location>> = mockk()

    @Before
    fun setUp() {
        repository = LocationRepositoryImpl(locationDao, bookmarkedDao ,locationMapper)
    }

    @Test
    fun searchLocalSourceWhenSearched() {
        // Test case fixture
        val daoRes: PagingSource<Int, LocationEntity> = object : PagingSource<Int, LocationEntity>() {
            override fun getRefreshKey(state: PagingState<Int, LocationEntity>): Int? {
                TODO("Not yet implemented")
            }

            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LocationEntity> {
                TODO("Not yet implemented")
            }

        }
        val mappedRes: PagingData<Location> = mockk()

        every { locationDao.getStartsWith(any()) } returns daoRes
        every { locationMapper.map(any()) } returns mappedRes

        // Given

        // When
        val query = "query"
        val observer = repository.search(query).test()

        // Then
        verify { locationDao.getStartsWith(query) }
        verify { locationMapper.map(any()) }
        observer.assertValue(mappedRes)
    }

    @Test
    fun getAllBookmarkedLocationsWhenQueriedForSavedLocations() {
        // Test case fixture
        val daoRes: PagingSource<Int, BookmarkedLocationEntity> = object : PagingSource<Int, BookmarkedLocationEntity>() {
            override fun getRefreshKey(state: PagingState<Int, BookmarkedLocationEntity>): Int? {
                TODO("Not yet implemented")
            }

            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BookmarkedLocationEntity> {
                TODO("Not yet implemented")
            }

        }
        val mappedRes: PagingData<Location> = mockk()

        every { bookmarkedDao.getAll() } returns daoRes
        every { locationMapper.map(any()) } returns mappedRes

        // Given

        // When
        val observer = repository.getSaved().test()

        // Then
        verify { bookmarkedDao.getAll() }
    }

    @Test
    fun removeLocationFromBookmarkedWhenUnsaved() {
        // Test fixture

        every { bookmarkedDao.delete(any()) } returns Completable.complete()

        // Given

        // When
        val id = Coordinates(80.6,56.9)
        val observer = repository.unSave(id).test()

        // Then
        verify { bookmarkedDao.delete(BookmarkedLocationEntity(id.lat,id.lon)) }
        observer.assertValue(AppResult.Success(Unit))
    }
}