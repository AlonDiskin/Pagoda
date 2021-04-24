package com.diskin.alon.pagoda.settings.presentation

import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.ViewModelLazy
import androidx.preference.ListPreference
import androidx.preference.PreferenceManager
import androidx.preference.get
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.pagoda.settings.appservices.WeatherUnit.*
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/**
 * [SettingsFragment] unit test.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(sdk = [28])
class SettingsFragmentTest {

    // Test subject
    private lateinit var scenario: FragmentScenario<SettingsFragment>

    // Collaborators
    private val viewModel = mockk<SettingsViewModel>()

    @Before
    fun setUp() {
        // Reset shared prefs settings
        clearSharedPrefs()

        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<*>>().value } returns viewModel

        // Launch fragment under test
        scenario = FragmentScenario.launchInContainer(SettingsFragment::class.java,
            null, R.style.Theme_AppCompat_Light_DarkActionBar,null)
    }

    @Test
    fun allowTemperatureUnitPreferenceSelection() {
        // Given

        scenario.onFragment {
            val key = it.getString(R.string.pref_temperature_unit_key)
            val tempUnitPref = it.preferenceScreen.get<ListPreference>(key)!!
            val metricValue = it.getString(R.string.pref_units_metric_value)
            val metricValueUiEntry = it.getString(R.string.pref_temperature_metric_entry)

            // Then fragment should show temperature unit selection preference with
            // summary entry as metric
            assertThat(tempUnitPref.isShown).isTrue()
            assertThat(tempUnitPref.summary).isEqualTo(metricValueUiEntry)

            // And default preference should be set as metric
            assertThat(tempUnitPref.value).isEqualTo(metricValue)
        }
    }

    @Test
    fun updateTemperatureUnitWhenPrefChanges() {
        // Test cas fixture
        every { viewModel.updateWeatherUnits(any()) } returns Unit

        // Given

        // When
        onView(withText(R.string.pref_temperature_title))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withText(R.string.pref_temperature_imperial_entry))
            .inRoot(RootMatchers.isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.updateWeatherUnits(Temperature(UnitSystem.IMPERIAL)) }
    }

    @Test
    fun allowTimeFormatUnitPreferenceSelection() {
        // Given

        scenario.onFragment {
            val key = it.getString(R.string.pref_time_format_key)
            val tempUnitPref = it.preferenceScreen.get<ListPreference>(key)!!
            val unitValue = it.getString(R.string.pref_time_format_24_value)

            // Then
            assertThat(tempUnitPref.isShown).isTrue()
            assertThat(tempUnitPref.summary).isEqualTo(unitValue)

            // And d
            assertThat(tempUnitPref.value).isEqualTo(unitValue)
        }
    }

    @Test
    fun updateTimeFormatUnitWhenPrefChanges() {
        // Test cas fixture
        every { viewModel.updateWeatherUnits(any()) } returns Unit

        // Given

        // When
        onView(withText(R.string.pref_time_format_title))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withText(R.string.pref_time_format_12_value))
            .inRoot(RootMatchers.isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.updateWeatherUnits(TimeFormat(HourFormat.HOUR_12)) }
    }

    @Test
    fun allowWindSpeedUnitPreferenceSelection() {
        // Given

        scenario.onFragment {
            val key = it.getString(R.string.pref_wind_speed_unit_key)
            val tempUnitPref = it.preferenceScreen.get<ListPreference>(key)!!
            val unitValue = it.getString(R.string.pref_units_metric_value)
            val unitUiEntry = it.getString(R.string.pref_wind_speed_metric_entry)

            // Then
            assertThat(tempUnitPref.isShown).isTrue()
            assertThat(tempUnitPref.summary).isEqualTo(unitUiEntry)

            // And d
            assertThat(tempUnitPref.value).isEqualTo(unitValue)
        }
    }

    @Test
    fun updateWindUnitWhenPrefChanges() {
        // Test cas fixture
        every { viewModel.updateWeatherUnits(any()) } returns Unit

        // Given

        // When
        onView(withText(R.string.pref_wind_speed_title))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withText(R.string.pref_wind_speed_imperial_entry))
            .inRoot(RootMatchers.isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.updateWeatherUnits(WindSpeed(UnitSystem.IMPERIAL)) }
    }

    private fun clearSharedPrefs() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.clear()
        editor.commit()
    }
}