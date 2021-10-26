package com.diskin.alon.pagoda.weatherinfo.data

import com.diskin.alon.pagoda.common.appservices.results.AppError
import com.diskin.alon.pagoda.common.appservices.results.Result
import com.diskin.alon.pagoda.common.appservices.results.ErrorType
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
    fun saveWeatherToLocalStorage_WhenCacheCurrent() {
        // Given
        val entity: CurrentWeatherEntity = mockk()

        every { entityMapper.map(any()) } returns entity
        every { weatherDao.insert(any()) } returns Completable.complete()

        // When
        val weather: Weather = mockk()
        val observer = cache.cacheCurrentLocation(weather).test()

        // Then
        verify { entityMapper.map(weather) }
        verify { weatherDao.insert(entity) }
        observer.assertValue(Result.Success(Unit) )
    }

    @Test
    fun handleError_WhenSavingCurrentWeatherToStorageFail() {
        // Given
        val entity: CurrentWeatherEntity = mockk()

        every { weatherDao.insert(any()) } returns Completable.error(Throwable())
        every { entityMapper.map(any()) } returns entity

        // When
        val observer = cache.cacheCurrentLocation(mockk()).test()

        // Then
        observer.assertValue(Result.Error(AppError(ErrorType.DB_ERROR)) )
    }

    @Test
    fun getWeatherFromLocalStorage_WhenQueriedForCurrent() {
        // Given
        val daoEntity: CurrentWeatherEntity = mockk()
        val weather: Weather = mockk()

        every { weatherDao.getWeather(any()) } returns Observable.just(daoEntity)
        every { weatherMapper.map(any()) } returns weather

        // When
        val observer = cache.getCurrentLocationWeather().test()

        // Then
        verify { weatherDao.getWeather(WEATHER_ID) }
        verify { weatherMapper.map(daoEntity) }
        observer.assertValue(Result.Success(weather))
    }

    @Test
    fun handleError_WhenRetrievingCurrentWeatherFromStorageFail() {
        // Given
        every { weatherDao.getWeather(any()) } returns Observable.error(Throwable())

        // When
        val observer = cache.getCurrentLocationWeather().test()

        // Then
        observer.assertValue(Result.Error(AppError(ErrorType.DB_ERROR)))
    }

    @Test
    fun checkCurrentWeatherInStorage_WhenQueriedForCurrentExistence() {
        // Given
        every { weatherDao.isWeatherExist(any()) } returns Single.just(1)

        // When
        val observer = cache.hasCurrentLocation().test()

        // Then
        val expected = true

        verify { weatherDao.isWeatherExist(WEATHER_ID) }
        observer.assertValue(Result.Success(expected))
    }

    @Test
    fun handleError_WhenCheckCurrentWeatherExistenceInStorageFail() {
        // Given
        every { weatherDao.isWeatherExist(any()) } returns Single.error(Throwable())

        // When
        val observer = cache.hasCurrentLocation().test()

        // Then
        observer.assertValue(Result.Error(AppError(ErrorType.DB_ERROR)))
    }
}