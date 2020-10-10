package com.diskin.alon.pagoda.settings.featuretesting

import android.content.Context
import android.os.Looper
import android.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.pagoda.settings.presentation.SettingsActivity
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import org.hamcrest.CoreMatchers.equalTo
import org.robolectric.Shadows

/**
 * Step definitions for 'App define default data unit type' scenario.
 */
class DefaultDataUnitSteps : GreenCoffeeSteps(){

    private lateinit var scenario: ActivityScenario<SettingsActivity>

    @Given("^User open settings screen$")
    fun userOpenSettingsScreen() {
        // Reset shared preferences
        clearSharedPrefs()

        // Launch settings activity
        scenario = ActivityScenario.launch(SettingsActivity::class.java)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^Unit type preference should be set to metric$")
    fun unitTypePreferenceShouldBeSetToMetric() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val key = context.getString(R.string.pref_units_key)
        val metricValue = context.getString(R.string.pref_units_metric_value)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        // Verify 'unit type' preference is shown
        onView(withClassName(equalTo(RecyclerView::class.java.name)))
            .perform(
                scrollTo<ViewHolder>(
                    hasDescendant(
                        withText(R.string.pref_units_title)
                    )
                )
            )
            .check(matches(isDisplayed()))

        // Verify 'unit type' preference should be set as 'metric'
        assertThat(prefs.getString(key,"")).isEqualTo(metricValue)
    }
}