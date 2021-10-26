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
import androidx.lifecycle.ViewModelLazy
import androidx.preference.ListPreference
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import androidx.preference.get
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.pagoda.common.appservices.results.AppError
import com.diskin.alon.pagoda.common.appservices.results.ErrorType.LOCATION_BACKGROUND_PERMISSION
import com.diskin.alon.pagoda.common.presentation.SingleLiveEvent
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.settings.presentation.controller.SettingsFragment
import com.diskin.alon.pagoda.settings.presentation.viewmodel.SettingsViewModel
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
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val viewModel: SettingsViewModel = mockk()

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
        scenario = launchFragmentInHiltContainer<SettingsFragment>(
            factory = object :FragmentFactory() {
                override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                    return SettingsFragment(testRegistry)
                }
            }
        )
    }

    @Test
    fun showTemperatureUnitSelectionPreference() {
        // Given

        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first()!! as SettingsFragment
            val key = fragment.getString(R.string.pref_temperature_unit_key)
            val prefUi = fragment.preferenceScreen.get<ListPreference>(key)!!
            val prefValue = fragment.getString(R.string.pref_units_metric_value)
            val prefUiEntry = fragment.getString(R.string.pref_temperature_metric_entry)

            // Then fragment should show temperature unit selection preference with
            // summary entry as metric
            assertThat(prefUi.isShown).isTrue()
            assertThat(prefUi.summary).isEqualTo(prefUiEntry)

            // And default preference should be set as metric
            assertThat(prefUi.value).isEqualTo(prefValue)
        }
    }

    @Test
    fun showTimeFormatUnitSelectionPreference() {
        // Given

        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first()!! as SettingsFragment
            val key = fragment.getString(R.string.pref_time_format_key)
            val prefUi = fragment.preferenceScreen.get<ListPreference>(key)!!
            val prefValue = fragment.getString(R.string.pref_time_format_24_value)

            // Then
            assertThat(prefUi.isShown).isTrue()
            assertThat(prefUi.summary).isEqualTo(prefValue)

            // And d
            assertThat(prefUi.value).isEqualTo(prefValue)
        }
    }

    @Test
    fun showWindSpeedUnitSelectionPreference() {
        // Given

        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first()!! as SettingsFragment
            val key = it.getString(R.string.pref_wind_speed_unit_key)
            val prefUi = fragment.preferenceScreen.get<ListPreference>(key)!!
            val prefValue = fragment.getString(R.string.pref_units_metric_value)
            val prefUiEntry = fragment.getString(R.string.pref_wind_speed_metric_entry)

            // Then
            assertThat(prefUi.isShown).isTrue()
            assertThat(prefUi.summary).isEqualTo(prefUiEntry)

            // And d
            assertThat(prefUi.value).isEqualTo(prefValue)
        }
    }

    @Test
    fun showWeatherAlertsNotificationPreference() {
        // Given

        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first()!! as SettingsFragment
            val key = fragment.getString(R.string.pref_alert_notification_key)
            val prefUi = fragment.preferenceScreen.get<SwitchPreference>(key)!!
            val prefValue = fragment.getString(R.string.pref_units_metric_value).toBoolean()
            val prefSummary = fragment.getString(R.string.pref_alert_notification_summary)

            // Then
            assertThat(prefUi.isShown).isTrue()
            assertThat(prefUi.summary).isEqualTo(prefSummary)

            // And
            assertThat(prefUi.isChecked).isEqualTo(prefValue)
        }
    }

    @Test
    fun showDarkModeEnablingPreference() {
        // Given

        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first()!! as SettingsFragment
            val key = fragment.getString(R.string.pref_dark_mode_key)
            val prefUi = fragment.preferenceScreen.get<SwitchPreference>(key)!!
            val prefValue = fragment.getString(R.string.pref_dark_mode_default_value).toBoolean()

            // Then
            assertThat(prefUi.isShown).isTrue()

            // And
            assertThat(prefUi.isChecked).isEqualTo(prefValue)
        }
    }

    @Test
    fun enableAppDarkMode_WhenUserEnablePref() {
        // Given
        every { viewModel.enableDarkMode(any()) } returns Unit

        // When (since pref ui is hidden in scrollable ui and not present in hierarchy,click via
        // preference api directly
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first()!! as SettingsFragment
            val key = fragment.getString(R.string.pref_dark_mode_key)
            val prefUi = fragment.preferenceScreen.get<SwitchPreference>(key)!!

            prefUi.performClick()
            prefUi.performClick()
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.enableDarkMode(true)}
        verify { viewModel.enableDarkMode(false)}
    }

    @Test
    fun askUserForLocationPermission_UponPermissionError() {
        // Given

        // When
        error.value = AppError(LOCATION_BACKGROUND_PERMISSION)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        //assertThat(testRegistry.isLocationAccessRequested).isTrue()
        //TODO add support for sdk 29 in robolectric
    }

    @Test
    fun setAlertNotificationPrefAsDisabled_UponAlertError() {
        // Given

        // When
        error.value = AppError(LOCATION_BACKGROUND_PERMISSION)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first()!! as SettingsFragment
            val key = fragment.getString(R.string.pref_alert_notification_key)
            val  prefValue = fragment.preferenceManager.sharedPreferences.getBoolean(key,true)
            val prefUi = fragment.preferenceScreen.get<SwitchPreference>(key)!!

            assertThat(prefValue).isFalse()
            assertThat(prefUi.isChecked).isFalse()
        }
    }

    @Test
    fun notifyThatPermissionNeeded_WhenUserDenyLocationPermission() {
        // Test case fixture
        this.testRegistry.locationPermissionResult = false

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