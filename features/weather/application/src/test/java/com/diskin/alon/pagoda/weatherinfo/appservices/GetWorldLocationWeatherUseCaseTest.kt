package com.diskin.alon.pagoda.weatherinfo.appservices

import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.WeatherRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.WorldLocationWeatherRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.GetWorldLocationWeatherUseCase
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.WeatherUnitsSettingMapper
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

/**
 * [GetWorldLocationWeatherUseCase] unit test case.
 */
class GetWorldLocationWeatherUseCaseTest {

    // Test subject
    private lateinit var useCase: GetWorldLocationWeatherUseCase

    // Collaborators
    private val weatherRepo: WeatherRepository = mockk()
    private val weatherUnitsMapper: WeatherUnitsSettingMapper = mockk()

    @Before
    fun setUp() {
        useCase = GetWorldLocationWeatherUseCase(weatherRepo, weatherUnitsMapper)
    }

    @Test
    fun getWorldLocationWeatherWhenExecuted() {
        // Given
        val repoWeather = mockk<Weather>()
        val repoRes = AppResult.Success(repoWeather)

        every { weatherRepo.get(any(),any()) } returns Observable.just(repoRes)
        every { weatherUnitsMapper.mapWeather(any()) } returns Observable.just(mockk())

        // When
        val request = WorldLocationWeatherRequest(23.5,67.9)
        useCase.execute(request).test()

        // Then
        verify { weatherRepo.get(request.lat,request.lon) }
        verify { weatherUnitsMapper.mapWeather(repoWeather) }
    }
}