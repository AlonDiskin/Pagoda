package com.diskin.alon.pagoda.settings.presentation

import com.diskin.alon.pagoda.common.presentation.Model
import com.diskin.alon.pagoda.settings.appservices.WeatherUnit
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
    private val model: Model = mockk()

    @Before
    fun setUp() {
        viewModel = SettingsViewModel(model)
    }

    @Test
    fun publishEventToModelWhenUnitTypeUpdated() {
        // Test case fixture
        every { model.execute(any<UpdateWeatherUnitModelRequest>()) } returns Unit

        // Given

        // When
        val unit: WeatherUnit = mockk()
        viewModel.updateWeatherUnits(unit)

        // Then
        verify { model.execute(UpdateWeatherUnitModelRequest(unit)) }
    }
}