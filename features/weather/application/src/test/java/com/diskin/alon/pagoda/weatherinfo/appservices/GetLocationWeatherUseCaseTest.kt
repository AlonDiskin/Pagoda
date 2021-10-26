package com.diskin.alon.pagoda.weatherinfo.appservices

import com.diskin.alon.pagoda.common.appservices.results.AppResult
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.SettingsRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.WeatherRepository
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherRequest
import com.diskin.alon.pagoda.weatherinfo.appservices.model.TimeFormatDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UnitSystemDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.WeatherDto
import com.diskin.alon.pagoda.weatherinfo.appservices.usecase.GetLocationWeatherUseCase
import com.diskin.alon.pagoda.weatherinfo.appservices.util.WeatherDtoMapper
import com.diskin.alon.pagoda.weatherinfo.domain.Weather
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

/**
 * [GetLocationWeatherUseCase] unit test class.
 */
class GetLocationWeatherUseCaseTest {

    // Test subject
    private lateinit var useCase: GetLocationWeatherUseCase

    // Collaborators
    private val weatherRepo: WeatherRepository = mockk()
    private val settingsRepo: SettingsRepository = mockk()
    private val weatherMapper: WeatherDtoMapper = mockk()

    @Before
    fun setUp() {
        useCase = GetLocationWeatherUseCase(weatherRepo, settingsRepo, weatherMapper)
    }

    @Test
    fun getUserLocationWeather_WhenExecutedForUserLocationWeatherRequest() {
        // Given
        val useCaseRequest = LocationWeatherRequest.UserLocationWeatherRequest
        val repoWeather = mockk<Weather>()
        val weatherRepoRes = AppResult.Success(repoWeather)
        val settingsRepoTempUnit = mockk<UnitSystemDto>()
        val settingsRepoWindUnit = mockk<UnitSystemDto>()
        val settingsRepoTimeFormat = mockk<TimeFormatDto>()
        val mapperRes: WeatherDto = mockk()

        every { weatherRepo.getCurrentLocationWeather() } returns Observable.just(weatherRepoRes)
        every { settingsRepo.getTempUnit() } returns Observable.just(settingsRepoTempUnit)
        every { settingsRepo.getWindSpeedUnit() } returns Observable.just(settingsRepoWindUnit)
        every { settingsRepo.getTimeFormat() } returns Observable.just(settingsRepoTimeFormat)
        every { weatherMapper.map(repoWeather,settingsRepoTempUnit,settingsRepoWindUnit,settingsRepoTimeFormat) } returns mapperRes

        // When
        val observer = useCase.execute(useCaseRequest).test()

        // Then
        observer.assertValue(AppResult.Success(mapperRes))
    }

    @Test
    fun getWorldLocationWeather_WhenExecutedForUserLocationWeatherRequest() {
        // Given
        val useCaseRequest = LocationWeatherRequest.WorldLocationWeatherRequest(23.5,67.9)
        val repoWeather = mockk<Weather>()
        val weatherRepoRes = AppResult.Success(repoWeather)
        val settingsRepoTempUnit = mockk<UnitSystemDto>()
        val settingsRepoWindUnit = mockk<UnitSystemDto>()
        val settingsRepoTimeFormat = mockk<TimeFormatDto>()
        val mapperRes: WeatherDto = mockk()

        every { weatherRepo.getLocationWeather(useCaseRequest.lat,useCaseRequest.lon) } returns Observable.just(weatherRepoRes)
        every { settingsRepo.getTempUnit() } returns Observable.just(settingsRepoTempUnit)
        every { settingsRepo.getWindSpeedUnit() } returns Observable.just(settingsRepoWindUnit)
        every { settingsRepo.getTimeFormat() } returns Observable.just(settingsRepoTimeFormat)
        every { weatherMapper.map(repoWeather,settingsRepoTempUnit,settingsRepoWindUnit,settingsRepoTimeFormat) } returns mapperRes

        // When
        val observer = useCase.execute(useCaseRequest).test()

        // Then
        observer.assertValue(AppResult.Success(mapperRes))
    }
}