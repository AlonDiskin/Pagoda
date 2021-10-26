package com.diskin.alon.pagoda.weatherinfo.data

import com.diskin.alon.pagoda.common.shared.AppDataProvider
import com.diskin.alon.pagoda.common.util.Mapper
import com.diskin.alon.pagoda.settings.shared.TempUnit
import com.diskin.alon.pagoda.settings.shared.TimeFormat
import com.diskin.alon.pagoda.settings.shared.WindSpeedUnit
import com.diskin.alon.pagoda.weatherinfo.appservices.model.TimeFormatDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UnitSystemDto
import com.diskin.alon.pagoda.weatherinfo.data.implementations.SettingsRepositoryImpl
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

/**
 * [SettingsRepositoryImpl] unit test class.
 */
class SettingsRepositoryImplTest {

    // Test subject
    private lateinit var repo: SettingsRepositoryImpl

    // Collaborators
    private val tempUnitPrefProvider: AppDataProvider<Observable<TempUnit>> = mockk()
    private val windSpeedUnitPrefProvider: AppDataProvider<Observable<WindSpeedUnit>> = mockk()
    private val timeFormatPrefProvider: AppDataProvider<Observable<TimeFormat>> = mockk()
    private val tempUnitMapper: Mapper<TempUnit, UnitSystemDto> = mockk()
    private val windSpeedUnitMapper: Mapper<WindSpeedUnit, UnitSystemDto> = mockk()
    private val timeFormatMapper: Mapper<TimeFormat, TimeFormatDto> = mockk()

    @Before
    fun setUp() {
        repo = SettingsRepositoryImpl(
            tempUnitPrefProvider,
            windSpeedUnitPrefProvider,
            timeFormatPrefProvider,
            tempUnitMapper,
            windSpeedUnitMapper,
            timeFormatMapper
        )
    }

    @Test
    fun getTempUnit_WhenQueried() {
        // Given
        val tempUnit: TempUnit = mockk()
        val mapperRes: UnitSystemDto = mockk()

        every { tempUnitPrefProvider.get() } returns Observable.just(tempUnit)
        every { tempUnitMapper.map(tempUnit) } returns mapperRes

        // When
        val observer = repo.getTempUnit().test()

        // Then
        observer.assertValue(mapperRes)
    }

    @Test
    fun getHorFormat_WhenQueried() {
        // Given
        val timeFormat: TimeFormat = mockk()
        val mapperRes: TimeFormatDto = mockk()

        every { timeFormatPrefProvider.get() } returns Observable.just(timeFormat)
        every { timeFormatMapper.map(timeFormat) } returns mapperRes

        // When
        val observer = repo.getTimeFormat().test()

        // Then
        observer.assertValue(mapperRes)
    }

    @Test
    fun getWindUnit_WhenQueried() {
        // Given
        val windUnit: WindSpeedUnit = mockk()
        val mapperRes: UnitSystemDto = mockk()

        every { windSpeedUnitPrefProvider.get() } returns Observable.just(windUnit)
        every { windSpeedUnitMapper.map(windUnit) } returns mapperRes

        // When
        val observer = repo.getWindSpeedUnit().test()

        // Then
        observer.assertValue(mapperRes)
    }
}