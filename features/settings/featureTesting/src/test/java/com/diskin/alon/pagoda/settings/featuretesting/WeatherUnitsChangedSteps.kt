package com.diskin.alon.pagoda.settings.featuretesting

import android.content.Context
import android.os.Looper
import android.preference.PreferenceManager
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.pagoda.settings.presentation.SettingsActivity
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import org.hamcrest.CoreMatchers.equalTo
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlertDialog

/**
 * Step definitions for 'User change unit type' scenario.
 */
class WeatherUnitsChangedSteps : GreenCoffeeSteps(){

    private lateinit var scenario: ActivityScenario<SettingsActivity>
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    init {
        // Reset shared preferences
        clearSharedPrefs()
    }

    @Given("^Weather data unit system preference is set as \"([^\"]*)\"$")
    fun weatherDataUnitSystemPreferenceIsSetAs(pref: String) {
        val key = context.getString(R.string.pref_units_key)

        val selectedPrefValue = when(pref) {
            "metric" -> context.getString(R.string.pref_units_metric_value)

            "imperial" -> context.getString(R.string.pref_units_imperial_value)

            else -> throw IllegalArgumentException("unknown pref scenario param!")
        }

        prefs.edit()
            .putString(key,selectedPrefValue)
            .apply()
    }

    @When("^User open app settings screen$")
    fun userOpenAppSettingsScreen() {
        // Launch settings activity
        scenario = ActivityScenario.launch(SettingsActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @And("^Select unit system as \"([^\"]*)\"$")
    fun selectUnitSystemAs(pref: String) {
        onView(withClassName(equalTo(RecyclerView::class.java.name)))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(R.string.pref_units_title)), click()
                )
            )
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        val prefUiIndex = when(pref) {
            "metric" -> {
                val metricValueUiEntry = context.getString(R.string.pref_units_metric_entry)
                context.resources
                    .getStringArray(R.array.pref_units_list_entries)
                    .toList().indexOf(metricValueUiEntry)
            }

            "imperial" -> {
                val imperialValueUiEntry = context.getString(R.string.pref_units_imperial_entry)
                context.resources
                    .getStringArray(R.array.pref_units_list_entries)
                    .toList().indexOf(imperialValueUiEntry)
            }

            else -> throw IllegalArgumentException("unknown pref scenario param!")
        }

        val dialog =
            ShadowAlertDialog.getLatestDialog() as AlertDialog

        dialog.listView.performItemClick(
            dialog.listView.adapter.getView(prefUiIndex, null, null),
            prefUiIndex,
            dialog.listView.adapter.getItemId(prefUiIndex))

        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^App should set unit preference as \"([^\"]*)\"$")
    fun appShouldSetUnitPreferenceAs(pref: String) {
        val key = context.getString(R.string.pref_units_key)

        val expectedPrefValue = when(pref) {
            "metric" -> context.getString(R.string.pref_units_metric_value)

            "imperial" -> context.getString(R.string.pref_units_imperial_value)

            else -> throw IllegalArgumentException("unknown pref scenario param!")
        }

        assertThat(prefs.getString(key, "")).isEqualTo(expectedPrefValue)
    }
}