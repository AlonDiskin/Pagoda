package com.diskin.alon.pagoda.weatherinfo.appservices

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.WeatherRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UserLocationWeatherRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.GetUserLocationWeatherUseCase
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.WeatherUnitsSettingMapper
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

/**
 * [GetUserLocationWeatherUseCase] unit test case.
 */
class GetUserLocationWeatherUseCaseTest {

    // Test subject
    private lateinit var useCase: GetUserLocationWeatherUseCase

    // Collaborators
    private val weatherRepo: WeatherRepository = mockk()
    private val weatherUnitsMapper: WeatherUnitsSettingMapper = mockk()

    @Before
    fun setUp() {
        useCase = GetUserLocationWeatherUseCase(weatherRepo, weatherUnitsMapper)
    }

    @Test
    fun getUserLocationWeatherWhenExecuted() {
        // Given
        val repoWeather = mockk<Weather>()
        val repoRes = AppResult.Success(repoWeather)

        every { weatherRepo.getCurrentLocation() } returns Observable.just(repoRes)
        every { weatherUnitsMapper.mapWeather(any()) } returns Observable.just(mockk())

        // When
        val request = UserLocationWeatherRequest()
        useCase.execute(request).test()

        // Then
        verify { weatherRepo.getCurrentLocation() }
        verify { weatherUnitsMapper.mapWeather(repoWeather) }
    }
}