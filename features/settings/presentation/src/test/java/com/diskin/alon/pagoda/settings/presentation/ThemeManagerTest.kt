package com.diskin.alon.pagoda.settings.presentation

import androidx.appcompat.app.AppCompatDelegate
import com.diskin.alon.pagoda.settings.presentation.controller.ThemeManager
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [ThemeManager] unit test class.
 */
@RunWith(JUnitParamsRunner::class)
class ThemeManagerTest {

    // Test subject
    private lateinit var manager: ThemeManager

    @Before
    fun setUp() {
        manager = ThemeManager()
    }

    @Test
    @Parameters(method = "enableDarkModeParams")
    fun enableAppDarkMode_AccordingToInvocation(enabled: Boolean, darkMode: Int) {
        // Given
        mockkStatic(AppCompatDelegate::class)
        every { AppCompatDelegate.setDefaultNightMode(any()) } returns Unit


        // When
        manager.enableDarkMode(enabled)

        // Then
        verify { AppCompatDelegate.setDefaultNightMode(darkMode) }
    }

    private fun enableDarkModeParams() = arrayOf(
        arrayOf(true,AppCompatDelegate.MODE_NIGHT_YES),
        arrayOf(false,AppCompatDelegate.MODE_NIGHT_NO)
    )
}