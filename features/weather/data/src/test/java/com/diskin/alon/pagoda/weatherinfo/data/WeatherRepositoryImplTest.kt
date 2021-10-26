package com.diskin.alon.pagoda.weatherinfo.data

import com.diskin.alon.pagoda.common.appservices.results.AppResult
import com.diskin.alon.pagoda.common.appservices.results.Result
import com.diskin.alon.pagoda.weatherinfo.data.implementations.WeatherRepositoryImpl
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.WeatherCache
import com.diskin.alon.pagoda.weatherinfo.data.local.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.WeatherStore
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.Test

/**
 * [WeatherRepositoryImpl] unit test class.
 */
class WeatherRepositoryImplTest {

    // Test subject
    private lateinit var repository: WeatherRepositoryImpl

    // Collaborators
    private val weatherStore: WeatherStore = mockk()
    private val locationProvider: UserLocationProvider = mockk()
    private val cache: WeatherCache = mockk()

    @Before
    fun setUp() {
        repository = WeatherRepositoryImpl(weatherStore,locationProvider,cache)
    }

    @Test
    fun getWeatherFromRemote_WhenQueriedForWeather() {
        // Given
        val lat = 23.56
        val lon = 56.78
        val weather = mockk<Weather>()

        every { weatherStore.getWeather(lat, lon) } returns Single.just(Result.Success(weather))

        // When
        val observer = repository.getLocationWeather(lat, lon).test()

        // Then
        observer.assertValueAt(0) { it is AppResult.Loading }
        observer.assertValueAt(1, AppResult.Success(weather))
    }

    @Test
    fun updateCache_WhenQueriedForCurrentWeather_AndCacheEmpty() {
        // Given
        val cachedWeather = BehaviorSubject.create<Result<Weather>>()
        val currentLocation = UserLocation(34.4,67.8)
        val weatherUpdate = mockk<Weather>()

        every { cache.hasCurrentLocation() } returns Single.just(Result.Success(false))
        every { cache.getCurrentLocationWeather() } returns cachedWeather
        every { cache.cacheCurrentLocation(weatherUpdate) } answers {
            cachedWeather.onNext(Result.Success(this.arg(0)))
            Single.just(Result.Success(Unit))
        }
        every { locationProvider.getLocation() } returns Single.just(Result.Success(currentLocation))
        every { weatherStore.getWeather(currentLocation.lat,currentLocation.lon) } returns Single.just(
            Result.Success(weatherUpdate))

        // When
        val observer = repository.getCurrentLocationWeather().test()

        // Then
        observer.assertValueAt(0) { it is AppResult.Loading }
        observer.assertValueAt(1, AppResult.Success(weatherUpdate))
        assertThat(observer.valueCount()).isEqualTo(2)
        assertThat(observer.errorCount()).isEqualTo(0)
    }

    @Test
    fun updateCache_WhenQueriedForCurrentWeather_AndUserLocationDifferFromCached() {
        // Given
        val weather = createWeather()
        val cachedWeather = BehaviorSubject.createDefault<Result<Weather>>(Result.Success(weather))
        val currentLocation = UserLocation(weather.id.lat + 20.0,weather.id.lon + 15.5)
        val weatherUpdate = mockk<Weather>()

        every { cache.hasCurrentLocation() } returns Single.just(Result.Success(true))
        every { cache.getCurrentLocationWeather() } returns cachedWeather
        every { cache.cacheCurrentLocation(weatherUpdate) } answers {
            cachedWeather.onNext(Result.Success(this.arg(0)))
            Single.just(Result.Success(Unit))
        }
        every { locationProvider.getLocation() } returns Single.just(Result.Success(currentLocation))
        every { weatherStore.getWeather(currentLocation.lat,currentLocation.lon) } returns Single.just(
            Result.Success(weatherUpdate))

        // When
        val observer = repository.getCurrentLocationWeather().test()

        // Then
        observer.assertValueAt(0) { it is AppResult.Loading }
        observer.assertValueAt(1, AppResult.Success(weather))
        observer.assertValueAt(2) { it is AppResult.Loading }
        observer.assertValueAt(3, AppResult.Success(weatherUpdate))
        assertThat(observer.valueCount()).isEqualTo(4)
        assertThat(observer.errorCount()).isEqualTo(0)
    }

    @Test
    fun updateCache_WhenQueriedForCurrentWeather_AndUserLocationSameAsCached_AndUpdateAvailable() {
        // Given
        val weather = createWeather()
        val cachedWeather = BehaviorSubject.createDefault<Result<Weather>>(Result.Success(weather))
        val currentLocation = UserLocation(weather.id.lat,weather.id.lon)
        val weatherUpdate = mockk<Weather>()

        every { cache.hasCurrentLocation() } returns Single.just(Result.Success(true))
        every { cache.getCurrentLocationWeather() } returns cachedWeather
        every { cache.cacheCurrentLocation(weatherUpdate) } answers {
            cachedWeather.onNext(Result.Success(this.arg(0)))
            Single.just(Result.Success(Unit))
        }
        every { locationProvider.getLocation() } returns Single.just(Result.Success(currentLocation))
        every { weatherStore.isUpdateAvailable(weather.updated) } returns true
        every { weatherStore.getWeather(currentLocation.lat,currentLocation.lon) } returns Single.just(
            Result.Success(weatherUpdate))

        // When
        val observer = repository.getCurrentLocationWeather().test()

        // Then
        observer.assertValueAt(0) { it is AppResult.Loading }
        observer.assertValueAt(1, AppResult.Success(weather))
        observer.assertValueAt(2) { it is AppResult.Loading }
        observer.assertValueAt(3, AppResult.Success(weatherUpdate))
        assertThat(observer.valueCount()).isEqualTo(4)
        assertThat(observer.errorCount()).isEqualTo(0)
    }

    @Test
    fun doNotUpdateCache_WhenQueriedForCurrentWeather_AndUserLocationSameAsCached_AndUpdateNotAvailable() {
        // Given
        val weather = createWeather()
        val cachedWeather = BehaviorSubject.createDefault<Result<Weather>>(Result.Success(weather))
        val currentLocation = UserLocation(weather.id.lat,weather.id.lon)

        every { cache.hasCurrentLocation() } returns Single.just(Result.Success(true))
        every { cache.getCurrentLocationWeather() } returns cachedWeather
        every { locationProvider.getLocation() } returns Single.just(Result.Success(currentLocation))
        every { weatherStore.isUpdateAvailable(weather.updated) } returns false

        // When
        val observer = repository.getCurrentLocationWeather().test()

        // Then
        observer.assertValueAt(0) { it is AppResult.Loading }
        observer.assertValueAt(1, AppResult.Success(weather))
        assertThat(observer.valueCount()).isEqualTo(2)
        assertThat(observer.errorCount()).isEqualTo(0)
    }
}