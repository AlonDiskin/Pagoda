package com.diskin.alon.pagoda.weatherinfo.appservices

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.toResult
import com.diskin.alon.pagoda.common.eventcontracts.AppEventProvider
import com.diskin.alon.pagoda.common.eventcontracts.settings.TemperatureUnitPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.WindSpeedUnitPref
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.WeatherRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherRequest.CurrentLocationRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.ProvideLocationWeatherUseCase
import com.diskin.alon.pagoda.weatherinfo.domain.LocationWeather
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
 * [ProvideLocationWeatherUseCase] unit test class.
 */
@RunWith(JUnitParamsRunner::class)
class ProvideLocationWeatherUseCaseTest {

    // Test subject
    private lateinit var useCase: ProvideLocationWeatherUseCase

    // Collaborators
    private val weatherRepo: WeatherRepository = mockk()
    private val locationProvider: UserLocationProvider = mockk()
    private val tempUnitPrefProvider: AppEventProvider<TemperatureUnitPref> = mockk()
    private val windSpeedUnitPrefProvider: AppEventProvider<WindSpeedUnitPref> = mockk()
    private val timeFormatPrefProvider: AppEventProvider<TimeFormatPref> = mockk()

    @Before
    fun setUp() {
        useCase = ProvideLocationWeatherUseCase(
            weatherRepo,
            locationProvider,
            tempUnitPrefProvider,
            windSpeedUnitPrefProvider,
            timeFormatPrefProvider
        )
    }

    @Test
    @Parameters(method = "useCaseParams")
    fun provideCurrentLocationWeatherWhenExecutedForCurrentLocation(
        weather: LocationWeather,
        tempUnitPref: TemperatureUnitPref,
        windSpeedUnitPref: WindSpeedUnitPref,
        timeFormatPref: TimeFormatPref,
        providedWeather: LocationWeatherDto
    ) {
        val location: UserLocation = createUserLocation()

        every { locationProvider.getCurrentLocation() } returns Observable.just(location).toResult()
        every { weatherRepo.get(any(),any()) } returns Observable.just(weather).toResult()
        every { tempUnitPrefProvider.get() } returns Observable.just(tempUnitPref)
        every { windSpeedUnitPrefProvider.get() } returns Observable.just(windSpeedUnitPref)
        every { timeFormatPrefProvider.get() } returns Observable.just(timeFormatPref)

        // Given

        // When
        val observer = useCase.execute(CurrentLocationRequest).test()

        // Then
        verify { locationProvider.getCurrentLocation() }
        verify { weatherRepo.get(location.lat,location.lon) }
        verify { tempUnitPrefProvider.get() }
        verify { windSpeedUnitPrefProvider.get() }
        verify { timeFormatPrefProvider.get() }
        observer.assertValue(AppResult.Success(providedWeather))
    }

    @Test
    @Parameters(method = "useCaseParams")
    fun provideLocationWeatherWhenExecutedForLocation(
        weather: LocationWeather,
        tempUnitPref: TemperatureUnitPref,
        windSpeedUnitPref: WindSpeedUnitPref,
        timeFormatPref: TimeFormatPref,
        providedWeather: LocationWeatherDto
    ) {
        every { weatherRepo.get(any(),any()) } returns Observable.just(weather).toResult()
        every { tempUnitPrefProvider.get() } returns Observable.just(tempUnitPref)
        every { windSpeedUnitPrefProvider.get() } returns Observable.just(windSpeedUnitPref)
        every { timeFormatPrefProvider.get() } returns Observable.just(timeFormatPref)

        // Given

        // When
        val request = createLocationRequest()
        val observer = useCase.execute(request).test()

        // Then
        verify(exactly = 0) { locationProvider.getCurrentLocation() }
        verify { weatherRepo.get(request.lat,request.lon) }
        verify { tempUnitPrefProvider.get() }
        verify { windSpeedUnitPrefProvider.get() }
        verify { timeFormatPrefProvider.get() }
        observer.assertValue(AppResult.Success(providedWeather))
    }

    @Test
    fun updateProvidedWeatherAccordingToAppPrefsUpdates() {
        // TODO("Not yet implemented")
    }

    private fun useCaseParams() = createUseCaseTestParams()
}