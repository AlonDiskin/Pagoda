package com.diskin.alon.pagoda.settings.featuretesting

import android.content.Context
import android.os.Looper
import androidx.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.pagoda.common.eventcontracts.AppEventPublisher
import com.diskin.alon.pagoda.common.eventcontracts.settings.TemperatureUnitPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.UnitPrefSystem
import com.diskin.alon.pagoda.common.eventcontracts.settings.WindSpeedUnitPref
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
class UnitTypePrefChangedSteps(
    private val tempUnitPublisher: AppEventPublisher<TemperatureUnitPref>,
    private val windSpeedUnitPublisher: AppEventPublisher<WindSpeedUnitPref>,
    private val timeFormatPublisher: AppEventPublisher<TimeFormatPref>
) : GreenCoffeeSteps(){

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val sp = PreferenceManager.getDefaultSharedPreferences(context)

    init {
        // Prepare mock event publishers for test
        every { tempUnitPublisher.publish(any()) } returns Unit
        every { windSpeedUnitPublisher.publish(any()) } returns Unit
        every { timeFormatPublisher.publish(any()) } returns Unit
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

    @Then("^Unit \"([^\"]*)\" should be set as default \"([^\"]*)\"$")
    fun unit_should_be_set_as_default(unit: String, default: String){
        when(unit) {
            "temperature" -> {
                val key = context.getString(R.string.pref_temperature_unit_key)
                val actual = sp.getString(key,null)
                val expected = when(default) {
                    "metric" -> context.getString(R.string.pref_units_metric_value)
                    "imperial" -> context.getString(R.string.pref_units_imperial_value)
                    else -> throw IllegalArgumentException("Unknown scenario arg:${default}")
                }

                assertThat(actual).isEqualTo(expected)
            }

            "wind speed" -> {
                val key = context.getString(R.string.pref_wind_speed_unit_key)
                val actual = sp.getString(key,null)
                val expected = when(default) {
                    "metric" -> context.getString(R.string.pref_units_metric_value)
                    "imperial" -> context.getString(R.string.pref_units_imperial_value)
                    else -> throw IllegalArgumentException("Unknown scenario arg:${default}")
                }

                assertThat(actual).isEqualTo(expected)
            }

            "time format" -> {
                val key = context.getString(R.string.pref_time_format_key)
                val actual = sp.getString(key,null)
                val expected = when(default) {
                    "24" -> context.getString(R.string.pref_time_format_24_value)
                    "12" -> context.getString(R.string.pref_time_format_12_value)
                    else -> throw IllegalArgumentException("Unknown scenario arg:${default}")
                }

                assertThat(actual).isEqualTo(expected)
            }

            else -> throw IllegalArgumentException("Unknown scenario arg:${unit}")
        }
    }

    @When("^User select unit \"([^\"]*)\" as new selection \"([^\"]*)\"$")
    fun user_select_unit_as_new_selection(unit: String, selection: String) {
        when(unit) {
            "temperature" -> {
                onView(withText(R.string.pref_temperature_title))
                    .perform(click())
                Shadows.shadowOf(Looper.getMainLooper()).idle()

                when(selection) {
                    "metric" -> {
                        onView(withText(R.string.pref_temperature_metric_entry))
                            .inRoot(RootMatchers.isDialog())
                            .perform(click())
                    }

                    "imperial" -> {
                        onView(withText(R.string.pref_temperature_imperial_entry))
                            .inRoot(RootMatchers.isDialog())
                            .perform(click())
                    }

                    else -> throw IllegalArgumentException("Unknown scenario arg:${selection}")
                }

                Shadows.shadowOf(Looper.getMainLooper()).idle()
            }

            "wind speed" -> {
                onView(withText(R.string.pref_wind_speed_title))
                    .perform(click())
                Shadows.shadowOf(Looper.getMainLooper()).idle()

                when(selection) {
                    "metric" -> {
                        onView(withText(R.string.pref_wind_speed_metric_entry))
                            .inRoot(RootMatchers.isDialog())
                            .perform(click())
                    }

                    "imperial" -> {
                        onView(withText(R.string.pref_wind_speed_imperial_entry))
                            .inRoot(RootMatchers.isDialog())
                            .perform(click())
                    }

                    else -> throw IllegalArgumentException("Unknown scenario arg:${selection}")
                }

                Shadows.shadowOf(Looper.getMainLooper()).idle()
            }

            "time format" -> {
                onView(withText(R.string.pref_time_format_title))
                    .perform(click())
                Shadows.shadowOf(Looper.getMainLooper()).idle()

                when(selection) {
                    "24" -> {
                        onView(withText(R.string.pref_time_format_24_value))
                            .inRoot(RootMatchers.isDialog())
                            .perform(click())
                    }

                    "12" -> {
                        onView(withText(R.string.pref_time_format_12_value))
                            .inRoot(RootMatchers.isDialog())
                            .perform(click())
                    }

                    else -> throw IllegalArgumentException("Unknown scenario arg:${selection}")
                }

                Shadows.shadowOf(Looper.getMainLooper()).idle()
            }

            else -> throw IllegalArgumentException("Unknown scenario arg:${unit}")
        }
    }

    @Then("^App should set unit preference \"([^\"]*)\" as selected \"([^\"]*)\"$")
    fun app_should_set_unit_preference_as_selected(unit: String, selection: String) {
        when(unit) {
            "temperature" -> {
                val key = context.getString(R.string.pref_temperature_unit_key)
                val actual = sp.getString(key,null)
                val expected = when(selection) {
                    "metric" -> context.getString(R.string.pref_units_metric_value)
                    "imperial" -> context.getString(R.string.pref_units_imperial_value)
                    else -> throw IllegalArgumentException("Unknown scenario arg:${selection}")
                }

                assertThat(actual).isEqualTo(expected)
            }

            "wind speed" -> {
                val key = context.getString(R.string.pref_wind_speed_unit_key)
                val actual = sp.getString(key,null)
                val expected = when(selection) {
                    "metric" -> context.getString(R.string.pref_units_metric_value)
                    "imperial" -> context.getString(R.string.pref_units_imperial_value)
                    else -> throw IllegalArgumentException("Unknown scenario arg:${selection}")
                }

                assertThat(actual).isEqualTo(expected)
            }

            "time format" -> {
                val key = context.getString(R.string.pref_time_format_key)
                val actual = sp.getString(key,null)
                val expected = when(selection) {
                    "24" -> context.getString(R.string.pref_time_format_24_value)
                    "12" -> context.getString(R.string.pref_time_format_12_value)
                    else -> throw IllegalArgumentException("Unknown scenario arg:${selection}")
                }

                assertThat(actual).isEqualTo(expected)
            }

            else -> throw IllegalArgumentException("Unknown scenario arg:${unit}")
        }
    }

    @And("^Update weather data to selected unit \"([^\"]*)\" as selected \"([^\"]*)\"$")
    fun update_weather_data_to_selected_unit_as_selected(unit: String, selection: String) {
        when(unit) {
            "temperature" -> {
                when(selection) {
                    "metric" -> verify { tempUnitPublisher.publish(TemperatureUnitPref(UnitPrefSystem.METRIC)) }
                    "imperial" -> verify { tempUnitPublisher.publish(TemperatureUnitPref(UnitPrefSystem.IMPERIAL)) }
                    else -> throw IllegalArgumentException("Unknown scenario arg:${selection}")
                }
            }

            "wind speed" -> {
                when(selection) {
                    "metric" -> verify { windSpeedUnitPublisher.publish(WindSpeedUnitPref(UnitPrefSystem.METRIC)) }
                    "imperial" -> verify { windSpeedUnitPublisher.publish(WindSpeedUnitPref(UnitPrefSystem.IMPERIAL)) }
                    else -> throw IllegalArgumentException("Unknown scenario arg:${selection}")
                }
            }

            "time format" -> {
                when(selection) {
                    "24" -> verify { timeFormatPublisher.publish(TimeFormatPref(TimeFormatPref.HourFormat.HOUR_24)) }
                    "12" -> verify { timeFormatPublisher.publish(TimeFormatPref(TimeFormatPref.HourFormat.HOUR_12)) }
                    else -> throw IllegalArgumentException("Unknown scenario arg:${selection}")
                }
            }

            else -> throw IllegalArgumentException("Unknown scenario arg:${unit}")
        }
    }
}