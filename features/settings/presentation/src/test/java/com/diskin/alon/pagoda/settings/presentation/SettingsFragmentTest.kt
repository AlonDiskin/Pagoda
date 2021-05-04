package com.diskin.alon.pagoda.settings.presentation

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.ViewModelLazy
import androidx.preference.ListPreference
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import androidx.preference.get
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.ErrorType.LOCATION_BACKGROUND_PERMISSION
import com.diskin.alon.pagoda.common.presentation.SingleLiveEvent
import com.diskin.alon.pagoda.settings.appservices.model.WeatherUnit.*
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
class SettingsFragmentTest() {

    // Test subject
    private lateinit var scenario: FragmentScenario<SettingsFragment>

    // Collaborators
    private val viewModel = mockk<SettingsViewModel>()

    // Stub data
    private val error = SingleLiveEvent<AppError>()
    private val testRegistry = object : ActivityResultRegistry() {

        private var locationAccessRequested = false
        val isLocationAccessRequested get() = locationAccessRequested
        var locationPermissionResult = true

        override fun <I : Any?, O : Any?> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?
        ) {
            when(contract) {
                is ActivityResultContracts.RequestPermission -> {
                    if (input!! == Manifest.permission.ACCESS_BACKGROUND_LOCATION) {
                        locationAccessRequested = true
                        dispatchResult(requestCode, locationPermissionResult)
                    }
                }
            }
        }
    }

    @Before
    fun setUp() {
        // Reset shared prefs settings
        clearSharedPrefs()

        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<*>>().value } returns viewModel

        // Stub view model
        every { viewModel.error } returns error

        // Launch fragment under test
        scenario = FragmentScenario.launchInContainer(
            SettingsFragment::class.java,
            null,
            R.style.Theme_AppCompat_Light_DarkActionBar,
            object : FragmentFactory() {
                override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                    return SettingsFragment(testRegistry)
                }
            }
        )
    }

    @Test
    fun showTemperatureUnitSelectionPreference() {
        // Given

        scenario.onFragment {
            val key = it.getString(R.string.pref_temperature_unit_key)
            val prefUi = it.preferenceScreen.get<ListPreference>(key)!!
            val prefValue = it.getString(R.string.pref_units_metric_value)
            val prefUiEntry = it.getString(R.string.pref_temperature_metric_entry)

            // Then fragment should show temperature unit selection preference with
            // summary entry as metric
            assertThat(prefUi.isShown).isTrue()
            assertThat(prefUi.summary).isEqualTo(prefUiEntry)

            // And default preference should be set as metric
            assertThat(prefUi.value).isEqualTo(prefValue)
        }
    }

    @Test
    fun changeTemperatureUnitWhenPreferenceChanged() {
        // Test cas fixture
        every { viewModel.changeWeatherUnits(any()) } returns Unit

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
        verify { viewModel.changeWeatherUnits(Temperature(UnitSystem.IMPERIAL)) }
    }

    @Test
    fun showTimeFormatUnitSelectionPreference() {
        // Given

        scenario.onFragment {
            val key = it.getString(R.string.pref_time_format_key)
            val prefUi = it.preferenceScreen.get<ListPreference>(key)!!
            val prefValue = it.getString(R.string.pref_time_format_24_value)

            // Then
            assertThat(prefUi.isShown).isTrue()
            assertThat(prefUi.summary).isEqualTo(prefValue)

            // And d
            assertThat(prefUi.value).isEqualTo(prefValue)
        }
    }

    @Test
    fun changeTimeFormatUnitWhenPreferenceChanged() {
        // Test cas fixture
        every { viewModel.changeWeatherUnits(any()) } returns Unit

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
        verify { viewModel.changeWeatherUnits(TimeFormat(HourFormat.HOUR_12)) }
    }

    @Test
    fun showWindSpeedUnitSelectionPreference() {
        // Given

        scenario.onFragment {
            val key = it.getString(R.string.pref_wind_speed_unit_key)
            val prefUi = it.preferenceScreen.get<ListPreference>(key)!!
            val prefValue = it.getString(R.string.pref_units_metric_value)
            val prefUiEntry = it.getString(R.string.pref_wind_speed_metric_entry)

            // Then
            assertThat(prefUi.isShown).isTrue()
            assertThat(prefUi.summary).isEqualTo(prefUiEntry)

            // And d
            assertThat(prefUi.value).isEqualTo(prefValue)
        }
    }

    @Test
    fun changeWindUnitWhenPreferenceChanged() {
        // Test cas fixture
        every { viewModel.changeWeatherUnits(any()) } returns Unit

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
        verify { viewModel.changeWeatherUnits(WindSpeed(UnitSystem.IMPERIAL)) }
    }

    @Test
    fun showWeatherAlertsNotificationPreference() {
        // Given

        scenario.onFragment {
            val key = it.getString(R.string.pref_alert_notification_key)
            val prefUi = it.preferenceScreen.get<SwitchPreference>(key)!!
            val prefValue = it.getString(R.string.pref_units_metric_value).toBoolean()
            val prefSummary = it.getString(R.string.pref_alert_notification_summary)

            // Then
            assertThat(prefUi.isShown).isTrue()
            assertThat(prefUi.summary).isEqualTo(prefSummary)

            // And
            assertThat(prefUi.isChecked).isEqualTo(prefValue)
        }
    }

    @Test
    fun setWeatherAlertNotificationWhenPreferenceChanged() {
        // Test cas fixture
        every { viewModel.enableWeatherAlertNotification(any()) } returns Unit

        // Given

        // When (since pref ui is hidden in scrollable ui and not present in hierarchy,click via
        // preference api directly
        scenario.onFragment {
            val key = it.getString(R.string.pref_alert_notification_key)
            val prefUi = it.preferenceScreen.get<SwitchPreference>(key)!!

            prefUi.performClick()
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.enableWeatherAlertNotification(true) }
    }

    @Test
    fun askUserForLocationPermissionUponPermissionError() {
        // Test cas fixture
        every { viewModel.enableWeatherAlertNotification(any()) } returns Unit

        // Given

        // When
        error.value = AppError(LOCATION_BACKGROUND_PERMISSION)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        //assertThat(testRegistry.isLocationAccessRequested).isTrue()

        // And
        //verify { viewModel.enableWeatherAlertNotification(true) }
        //TODO add support for sdk 29 in robolectric
    }

    @Test
    fun setAlertNotificationPrefAsDisabledWhenUserDenyLocationPermission() {
        // Test case fixture
        this.testRegistry.locationPermissionResult = false
        every { viewModel.enableWeatherAlertNotification(any()) } returns Unit

        // Given

        // When
        error.value = AppError(LOCATION_BACKGROUND_PERMISSION)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onFragment {
            val key = it.getString(R.string.pref_alert_notification_key)
            val  prefValue = it.preferenceManager.sharedPreferences.getBoolean(key,true)
            val prefUi = it.preferenceScreen.get<SwitchPreference>(key)!!

            assertThat(prefValue).isFalse()
            assertThat(prefUi.isChecked).isFalse()
        }
    }

    @Test
    fun notifyThatPermissionNeededWhenUserDenyLocationPermission() {
        // Test case fixture
        this.testRegistry.locationPermissionResult = false
        every { viewModel.enableWeatherAlertNotification(any()) } returns Unit

        // Given

        // When
        error.value = AppError(LOCATION_BACKGROUND_PERMISSION)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        val expectedToastMessage = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.text_location_permission_denied)
        //assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(expectedToastMessage)
        //TODO add support for sdk 29 in robolectric
    }

    private fun clearSharedPrefs() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.clear()
        editor.commit()
    }
}