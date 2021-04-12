package com.diskin.alon.pagoda.settings.featuretesting

import android.content.Context
import android.os.Looper
import androidx.preference.ListPreference
import androidx.preference.PreferenceManager
import androidx.preference.get
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.pagoda.common.events.UnitSystemEvent
import com.diskin.alon.pagoda.common.events.WeatherUnitsEventPublisher
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.settings.presentation.R
import com.diskin.alon.pagoda.settings.presentation.SettingsFragment
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.verify
import org.robolectric.Shadows

/**
 * Step definitions for 'User change unit type preference' scenario.
 */
class UnitTypePrefChangedSteps(private val eventPublisher: WeatherUnitsEventPublisher) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val sp = PreferenceManager.getDefaultSharedPreferences(context)

    init {
        // Prepare event publisher mock for test scenario
        every { eventPublisher.post(any()) } returns Unit
    }

    @Given("^Weather data unit system preference has not been changed$")
    fun weather_data_unit_system_preference_has_not_been_changed() {
        // Reset shared preferences
        clearSharedPrefs()
    }

    @When("^User open app settings screen$")
    fun user_open_app_settings_screen() {
        // Launch settings fragment
        scenario = launchFragmentInHiltContainer<SettingsFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^Units should be set as metric by default$")
    fun units_should_be_set_as_metric_by_default() {
        // Verify current pref value is set as 'metric'
        val key = context.getString(R.string.pref_units_key)
        val defaultValue = context.getString(R.string.pref_units_default_value)
        val actualUnitPref = sp.getString(key,defaultValue)
        val metricPrefValue = context.getString(R.string.pref_units_metric_value)

        assertThat(actualUnitPref).isEqualTo(metricPrefValue)

        // Verify ui show unit as 'metric'
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]!! as SettingsFragment
            val unitPref = fragment.preferenceScreen.get<ListPreference>(key)!!
            val metricValueUiEntry = it.getString(R.string.pref_units_metric_entry)

            assertThat(unitPref.isShown).isTrue()
            assertThat(unitPref.summary).isEqualTo(metricValueUiEntry)
        }
    }

    @When("^User select unit system as imperial$")
    fun user_select_unit_system_as_imperial() {
        // Select unit pref as 'imperial'
        onView(withText(R.string.pref_units_title))
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(withText(R.string.pref_units_imperial_entry))
            .inRoot(RootMatchers.isDialog())
            .perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^App should set unit preference as imperial$")
    fun app_should_set_unit_preference_as_imperial() {
        // Verify current pref value is set as 'metric'
        val key = context.getString(R.string.pref_units_key)
        val defaultValue = context.getString(R.string.pref_units_default_value)
        val actualUnitPref = sp.getString(key,defaultValue)
        val imperialPrefValue = context.getString(R.string.pref_units_imperial_value)

        assertThat(actualUnitPref).isEqualTo(imperialPrefValue)

        // Verify ui show unit as 'imperial'
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0]!! as SettingsFragment
            val unitPref = fragment.preferenceScreen.get<ListPreference>(key)!!
            val imperialValueUiEntry = it.getString(R.string.pref_units_imperial_entry)

            assertThat(unitPref.isShown).isTrue()
            assertThat(unitPref.summary).isEqualTo(imperialValueUiEntry)
        }
    }

    @And("^Update weather data to selected unit system$")
    fun update_weather_data_to_selected_unit_system() {
        // Verify unit pref change event sent
        verify { eventPublisher.post(UnitSystemEvent.IMPERIAL) }
    }
}