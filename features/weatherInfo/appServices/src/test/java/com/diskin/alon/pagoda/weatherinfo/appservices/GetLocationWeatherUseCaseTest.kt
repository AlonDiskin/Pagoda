package com.diskin.alon.pagoda.weatherinfo.appservices

import com.diskin.alon.pagoda.common.appservices.Result
import com.diskin.alon.pagoda.common.appservices.toResult
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.AppPrefsStore
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.WeatherRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherRequest.*
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.GetLocationWeatherUseCase
import com.diskin.alon.pagoda.weatherinfo.domain.LocationWeather
import com.diskin.alon.pagoda.weatherinfo.domain.UnitSystem
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [GetLocationWeatherUseCase] unit test class.
 */
@RunWith(JUnitParamsRunner::class)
class GetLocationWeatherUseCaseTest {

    // Test subject
    private lateinit var useCase: GetLocationWeatherUseCase

    // Collaborators
    private val weatherRepo: WeatherRepository = mockk()
    private val locationProvider: UserLocationProvider = mockk()
    private val prefsStore: AppPrefsStore = mockk()
    private val mapper: Mapper<LocationWeather, LocationWeatherDto> = mockk()

    @Before
    fun setUp() {
        useCase = GetLocationWeatherUseCase(weatherRepo, locationProvider, prefsStore, mapper)
    }

    @Test
    @Parameters(method = "useCaseParams")
    fun provideCurrentLocationWeatherWhenExecutedForCurrentLocation(unitSystem: UnitSystem) {
        val location: UserLocation = createUserLocation()
        val weather: LocationWeather = mockk()
        val mappedWeather: LocationWeatherDto = mockk()

        every { locationProvider.getCurrentLocation() } returns Observable.just(location).toResult()
        every { prefsStore.getUnitSystem() } returns Observable.just(unitSystem).toResult()
        every { weatherRepo.get(any(),any()) } returns Observable.just(weather).toResult()
        every { weather.toImperial() } returns Unit
        every { weather.unitSystem } returns UnitSystem.METRIC
        every { mapper.map(any()) } returns mappedWeather

        // Given an initialized useCase

        // When use case is executed
        val observer = useCase.execute(CurrentLocationRequest).test()

        // Then useCase should get an observable value of user location
        verify { locationProvider.getCurrentLocation() }

        // And use location value to get weather from repository
        verify { weatherRepo.get(location.lat,location.lon) }

        // And retrieve user unit system pref
        verify { prefsStore.getUnitSystem() }

        // And propagate result model according to user unit system pref,when repo result
        // unit system differ from user unit system
        if (unitSystem == UnitSystem.IMPERIAL) {
            verify { weather.toImperial() }
        }

        //verify { mapper.map(weather) }
        observer.assertValue(Result.Success(mappedWeather))
    }

    @Test
    @Parameters(method = "useCaseParams")
    fun provideLocationWeatherWhenExecutedForLocation(unitSystem: UnitSystem) {
        val weather: LocationWeather = mockk()
        val mappedWeather: LocationWeatherDto = mockk()

        every { prefsStore.getUnitSystem() } returns Observable.just(unitSystem).toResult()
        every { weatherRepo.get(any(),any()) } returns Observable.just(weather).toResult()
        every { weather.toImperial() } returns Unit
        every { weather.unitSystem } returns UnitSystem.METRIC
        every { mapper.map(any()) } returns mappedWeather

        // Given an initialized useCase

        // When use case is executed
        val request = LocationRequest(12.34,56.78)
        val observer = useCase.execute(request).test()

        // Then useCase should not get an observable value of user location
        verify(exactly = 0) { locationProvider.getCurrentLocation() }

        // And use request coordinates values to get weather from repository
        verify { weatherRepo.get(request.lat,request.lon) }

        // And retrieve user unit system pref
        verify { prefsStore.getUnitSystem() }

        // And propagate result model according to user unit system pref,when repo result
        // unit system differ from user unit system
        if (unitSystem == UnitSystem.IMPERIAL) {
            verify { weather.toImperial() }
        }

        //verify { mapper.map(weather) }
        observer.assertValue(Result.Success(mappedWeather))
    }

    private fun useCaseParams() = arrayOf(UnitSystem.METRIC,UnitSystem.IMPERIAL)
}