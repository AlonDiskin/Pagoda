package com.diskin.alon.pagoda.weatherinfo.data

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.CurrentWeatherDao
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * [CurrentWeatherDao] integration test class.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class CurrentWeatherDaoTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // System under test
    private lateinit var dao: CurrentWeatherDao
    private lateinit var db: TestDatabase

    @Before
    fun setUp() {
        // Init system under test
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.currentWeatherDao()
    }

    @Test
    fun writeAndCheckWeatherExist() {
        // Given

        // When
        dao.insert(createCurrentWeatherEntity()).test()

        // And
        val observer = dao.isWeatherExist(1).test()

        // Then
        observer.assertValue(1)
    }

    @Test
    fun writeAndReadWeather() {
        // Given
        val entity = createCurrentWeatherEntity()

        // When
        dao.insert(entity).test()

        // And
        val observer = dao.getWeather(1).test()

        // Then
        observer.assertValue(entity)
    }
}