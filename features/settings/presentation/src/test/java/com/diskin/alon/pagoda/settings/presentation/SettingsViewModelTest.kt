package com.diskin.alon.pagoda.settings.presentation

import com.diskin.alon.pagoda.common.events.UnitSystemEvent
import com.diskin.alon.pagoda.common.events.WeatherUnitsEventPublisher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * [SettingsViewModel] unit test class.
 */
class SettingsViewModelTest {

    // Test subject
    private lateinit var viewModel: SettingsViewModel

    // Collaborators
    private val eventPublisher: WeatherUnitsEventPublisher = mockk()

    @Before
    fun setUp() {
        viewModel = SettingsViewModel(eventPublisher)
    }

    @Test
    fun publishEventToModelWhenUnitTypeUpdated() {
        // Test case fixture
        every { eventPublisher.post(any()) } returns Unit

        // Given

        // When
        val unit: UnitSystemEvent = mockk()
        viewModel.updateWeatherUnit(unit)

        // Then
        verify { eventPublisher.post(unit) }
    }
}