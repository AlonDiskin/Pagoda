package com.diskin.alon.pagoda.weatherinfo.data

import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.ErrorType
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.data.local.implementations.WeatherCacheImpl
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.CurrentWeatherDao
import com.diskin.alon.pagoda.weatherinfo.data.local.model.CurrentWeatherEntity
import com.diskin.alon.pagoda.weatherinfo.data.local.model.CurrentWeatherEntity.Companion.WEATHER_ID
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

/**
 * [WeatherCacheImpl] unit test class.
 */
class WeatherCacheImplTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Set Rx framework for testing
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        }
    }

    // Test subject
    private lateinit var cache: WeatherCacheImpl

    // Collaborators
    private val weatherDao: CurrentWeatherDao = mockk()
    private val weatherMapper: Mapper<CurrentWeatherEntity, Weather> = mockk()
    private val entityMapper: Mapper<Weather, CurrentWeatherEntity> = mockk()

    @Before
    fun setUp() {
        cache = WeatherCacheImpl(weatherDao, weatherMapper, entityMapper)
    }

    @Test
    fun saveWeatherToLocalStorageWhenCacheCurrent() {
        // Test case fixture
        val entity: CurrentWeatherEntity = mockk()

        every { entityMapper.map(any()) } returns entity
        every { weatherDao.insert(any()) } returns Completable.complete()

        // Given

        // When
        val weather: Weather = mockk()
        val observer = cache.cacheCurrentLocation(weather).test()

        // Then
        verify { entityMapper.map(weather) }
        verify { weatherDao.insert(entity) }
        observer.assertValueAt(0) { it is AppResult.Loading }
        observer.assertValueAt(1) { it == AppResult.Success(Unit) }
    }

    @Test
    fun handleErrorWhenSavingCurrentWeatherToStorageFail() {
        // Test case fixture
        val entity: CurrentWeatherEntity = mockk()

        every { weatherDao.insert(any()) } returns Completable.error(Throwable())
        every { entityMapper.map(any()) } returns entity

        // Given

        // When
        val observer = cache.cacheCurrentLocation(mockk()).test()

        // Then
        observer.assertValueAt(0) { it is AppResult.Loading }
        observer.assertValueAt(1) { it == AppResult.Error<Unit>(AppError(ErrorType.DB_ERROR)) }
    }

    @Test
    fun getWeatherFromLocalStorageWhenQueriedForCurrent() {
        // Test case fixture
        val daoEntity: CurrentWeatherEntity = mockk()
        val weather: Weather = mockk()

        every { weatherDao.getWeather(any()) } returns Observable.just(daoEntity)
        every { weatherMapper.map(any()) } returns weather

        // Given

        // When
        val observer = cache.getCurrentLocation().test()

        // Then
        verify { weatherDao.getWeather(WEATHER_ID) }
        verify { weatherMapper.map(daoEntity) }
        observer.assertValueAt(0) { it is AppResult.Loading }
        observer.assertValueAt(1) { it == AppResult.Success(weather) }
    }

    @Test
    fun handleErrorWhenRetrievingCurrentWeatherFromStorageFail() {
        // Test case fixture
        every { weatherDao.getWeather(any()) } returns Observable.error(Throwable())

        // Given

        // When
        val observer = cache.getCurrentLocation().test()

        // Then
        observer.assertValueAt(0) { it is AppResult.Loading }
        observer.assertValueAt(1) { it == AppResult.Error<Weather>(AppError(ErrorType.DB_ERROR)) }
    }

    @Test
    fun checkCurrentWeatherInStorageWhenQueriedForCurrentExistence() {
        // Test case fixture
        every { weatherDao.isWeatherExist(any()) } returns Single.just(1)

        // Given

        // When
        val observer = cache.hasCurrentLocation().test()

        // Then
        val expected = true

        verify { weatherDao.isWeatherExist(WEATHER_ID) }
        observer.assertValueAt(0) { it is AppResult.Loading }
        observer.assertValueAt(1) { it == AppResult.Success(expected) }
    }

    @Test
    fun handleErrorWhenCheckCurrentWeatherExistenceInStorageFail() {
        // Test case fixture
        every { weatherDao.isWeatherExist(any()) } returns Single.error(Throwable())

        // Given

        // When
        val observer = cache.hasCurrentLocation().test()

        // Then
        observer.assertValueAt(0) { it is AppResult.Loading }
        observer.assertValueAt(1) { it == AppResult.Error<Boolean>(AppError(ErrorType.DB_ERROR)) }
    }
}