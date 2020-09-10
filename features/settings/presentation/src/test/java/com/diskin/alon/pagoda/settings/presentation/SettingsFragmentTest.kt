package com.diskin.alon.pagoda.settings.presentation

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.testing.FragmentScenario
import androidx.preference.ListPreference
import androidx.preference.PreferenceManager
import androidx.preference.get
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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

    @Before
    fun setUp() {
        // Reset shared prefs settings
        clearSharedPrefs()

        // Launch fragment under test
        scenario = FragmentScenario.launchInContainer(SettingsFragment::class.java)
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

    private fun clearSharedPrefs() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.clear()
        editor.commit()
    }
}