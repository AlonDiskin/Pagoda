package com.diskin.alon.pagoda.settings.presentation

import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.testing.FragmentScenario
import androidx.preference.ListPreference
import androidx.preference.PreferenceManager
import androidx.preference.get
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowAlertDialog

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
        scenario = FragmentScenario.launchInContainer(SettingsFragment::class.java,
            null, R.style.Theme_AppCompat_Light_DarkActionBar,null)
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
        // Given a resumed fragment with default unit preferences as 'metric'

        // When user select 'imperial' unit type
        val context = ApplicationProvider.getApplicationContext<Context>()
        val imperialValueUiEntry = context.getString(R.string.pref_units_imperial_entry)
        val imperialUiIndex = context.resources
            .getStringArray(R.array.pref_units_list_entries).toList().indexOf(imperialValueUiEntry)

        onView(withClassName(CoreMatchers.equalTo(RecyclerView::class.java.name)))
            .perform(actionOnItem<ViewHolder>(
                hasDescendant(withText(R.string.pref_units_title)), click())
            )

        val dialog =
            ShadowAlertDialog.getLatestDialog() as AlertDialog

        dialog.listView.performItemClick(
            dialog.listView.adapter.getView(imperialUiIndex, null, null),
            imperialUiIndex,
            dialog.listView.adapter.getItemId(imperialUiIndex))

        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then activity should set preference as 'imperial'
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)!!
        val unitPrefKey = context.getString(R.string.pref_units_key)
        val imperialPrefValue = context.getString(R.string.pref_units_imperial_value)

        assertThat(prefs.getString(unitPrefKey,"")).isEqualTo(imperialPrefValue)

        // And display unit preference as set to 'imperial'
        scenario.onFragment {
            val key = it.getString(R.string.pref_units_key)
            val unitPref = it.preferenceScreen.get<ListPreference>(key)!!

            assertThat(unitPref.isShown).isTrue()
            assertThat(unitPref.summary).isEqualTo(imperialValueUiEntry)
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