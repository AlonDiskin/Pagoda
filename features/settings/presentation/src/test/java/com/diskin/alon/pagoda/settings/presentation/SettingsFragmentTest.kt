package com.diskin.alon.pagoda.settings.presentation

import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.ViewModelLazy
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
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
import com.diskin.alon.pagoda.common.events.UnitSystemEvent
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

    // Test nav controller
    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

    @Before
    fun setUp() {
        // Reset shared prefs settings
        clearSharedPrefs()

        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<*>>().value } returns viewModel

        // Setup test nav controller
        navController.setGraph(R.navigation.settings_nav_graph)

        // Launch fragment under test
        scenario = FragmentScenario.launchInContainer(SettingsFragment::class.java,
            null, R.style.Theme_AppCompat_Light_DarkActionBar,null)

        // Set the NavController property on the fragment with test controller
        scenario.onFragment {
            Navigation.setViewNavController(
                it.requireView(),
                navController)
        }
    }

    @Test
    fun showSettingsTitleWhenResumed() {
        // Given a resumed fragment

        // Then fragment should show settings title in containing activity appbar
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.settings_label)

        assertThat(navController.currentDestination?.label).isEqualTo(appName)
    }

    @Test
    fun setAndDisplayUnitTypePreference() {
        // Given a resumed fragment with default app preferences

        scenario.onFragment {
            val key = it.getString(R.string.pref_units_key)
            val unitPref = it.preferenceScreen.get<ListPreference>(key)!!
            val metricValue = it.getString(R.string.pref_units_metric_value)
            val metricValueUiEntry = it.getString(R.string.pref_units_metric_entry)

            // Then fragment should show 'unit type' preference with summary entry as 'metric' value
            assertThat(unitPref.isShown).isTrue()
            assertThat(unitPref.summary).isEqualTo(metricValueUiEntry)

            // And preference should be set as 'metric'
            assertThat(unitPref.value).isEqualTo(metricValue)
        }
    }

    @Test
    fun changeUnitTypeAccordingToUserSelection() {
        // Text case fixture
        every { viewModel.updateWeatherUnit(any()) } returns Unit

        // Given a resumed fragment with default unit preferences as 'metric'

        // When user select 'imperial' unit type
        onView(withText(R.string.pref_units_title))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withText(R.string.pref_units_imperial_entry))
            .inRoot(RootMatchers.isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then fragment should set preference as 'imperial'
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)!!
        val unitPrefKey = context.getString(R.string.pref_units_key)
        val imperialPrefValue = context.getString(R.string.pref_units_imperial_value)

        assertThat(prefs.getString(unitPrefKey,"")).isEqualTo(imperialPrefValue)

        // And display unit preference as set to 'imperial'
        scenario.onFragment {
            val key = it.getString(R.string.pref_units_key)
            val unitPref = it.preferenceScreen.get<ListPreference>(key)!!
            val imperialValueUiEntry = context.getString(R.string.pref_units_imperial_entry)

            assertThat(unitPref.isShown).isTrue()
            assertThat(unitPref.summary).isEqualTo(imperialValueUiEntry)
        }
    }

    @Test
    fun updateWeatherUnitWhenUnitPrefChanged() {
        // Text case fixture
        every { viewModel.updateWeatherUnit(any()) } returns Unit

        // Given

        // When
        onView(withText(R.string.pref_units_title))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withText(R.string.pref_units_imperial_entry))
            .inRoot(RootMatchers.isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.updateWeatherUnit(UnitSystemEvent.IMPERIAL) }

        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        onView(withText(R.string.pref_units_title))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withText(R.string.pref_units_metric_entry))
            .inRoot(RootMatchers.isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.updateWeatherUnit(UnitSystemEvent.METRIC) }
    }

    private fun clearSharedPrefs() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.clear()
        editor.commit()
    }
}