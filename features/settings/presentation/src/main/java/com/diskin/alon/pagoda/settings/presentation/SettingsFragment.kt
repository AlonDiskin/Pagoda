package com.diskin.alon.pagoda.settings.presentation

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.preference.get
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.ErrorType
import com.diskin.alon.pagoda.settings.appservices.model.WeatherUnit.*
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject

@OptionalInject
@AndroidEntryPoint
class SettingsFragment(registry: ActivityResultRegistry? = null) : PreferenceFragmentCompat() {

    private val viewModel: SettingsViewModel by viewModels()
    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
        when(key){
            getString(R.string.pref_temperature_unit_key) -> handleTemperatureUnitPrefChange(sp,key)
            getString(R.string.pref_wind_speed_unit_key) -> handleWindSpeedUnitPrefChange(sp,key)
            getString(R.string.pref_time_format_key) -> handleTimeFormatUnitPrefChange(sp,key)
            getString(R.string.pref_alert_notification_key) -> handleEnableAlertNotificationPref(sp,key)
        }
    }
    private val locationPermissionLauncher = createActivityResultLauncher(ActivityResultContracts.RequestPermission(),registry) {
        it?.let { granted ->
            if (granted) {
                viewModel.enableWeatherAlertNotification(true)
            } else {
                val alertKey = getString(R.string.pref_alert_notification_key)
                preferenceScreen.sharedPreferences
                    .unregisterOnSharedPreferenceChangeListener(prefListener)
                preferenceScreen.get<SwitchPreference>(alertKey)!!.isChecked = false

                preferenceScreen.sharedPreferences
                    .edit()
                    .putBoolean(alertKey,false)
                    .apply()
                preferenceScreen.sharedPreferences
                    .registerOnSharedPreferenceChangeListener(prefListener)
                Toast.makeText(requireContext(),
                    getString(R.string.text_location_permission_denied),
                    Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            == PackageManager.PERMISSION_DENIED) {
            val key = getString(R.string.pref_alert_notification_key)
            preferenceManager
                .sharedPreferences
                .edit()
                .putBoolean(key,false)
                .apply()
        }

        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.error.observe(viewLifecycleOwner) { it?.let(this::resolveSettingsError) }
    }

    private fun resolveSettingsError(appError: AppError) {
        when(appError.type) {
            ErrorType.LOCATION_BACKGROUND_PERMISSION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    askUserForLocationPermission()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences
            .registerOnSharedPreferenceChangeListener(prefListener)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences
            .unregisterOnSharedPreferenceChangeListener(prefListener)
    }

    private fun handleTemperatureUnitPrefChange(sp: SharedPreferences, key: String) {
        val defaultUnit = getString(R.string.pref_temperature_default_value)

        when(sp.getString(key,defaultUnit)) {
            getString(R.string.pref_units_metric_value) ->
                viewModel.changeWeatherUnits(Temperature(UnitSystem.METRIC))
            getString(R.string.pref_units_imperial_value) ->
                viewModel.changeWeatherUnits(Temperature(UnitSystem.IMPERIAL))
        }
    }

    private fun handleWindSpeedUnitPrefChange(sp: SharedPreferences, key: String) {
        val defaultUnit = getString(R.string.pref_wind_speed_default_value)

        when(sp.getString(key,defaultUnit)) {
            getString(R.string.pref_units_metric_value) ->
                viewModel.changeWeatherUnits(WindSpeed(UnitSystem.METRIC))
            getString(R.string.pref_units_imperial_value) ->
                viewModel.changeWeatherUnits(WindSpeed(UnitSystem.IMPERIAL))
        }
    }

    private fun handleTimeFormatUnitPrefChange(sp: SharedPreferences, key: String) {
        val defaultUnit = getString(R.string.pref_time_format_default_value)

        when(sp.getString(key,defaultUnit)) {
            getString(R.string.pref_time_format_12_value) ->
                viewModel.changeWeatherUnits(TimeFormat(HourFormat.HOUR_12))
            getString(R.string.pref_time_format_24_value) ->
                viewModel.changeWeatherUnits(TimeFormat(HourFormat.HOUR_24))
        }
    }

    private fun handleEnableAlertNotificationPref(sp: SharedPreferences, key: String) {
        val defaultValue = getString(R.string.pref_alert_notification_default_value).toBoolean()

        viewModel.enableWeatherAlertNotification(sp.getBoolean(key,defaultValue))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun askUserForLocationPermission() {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    private fun <I,O> createActivityResultLauncher(
        contract: ActivityResultContract<I, O>,
        registry: ActivityResultRegistry? = null,
        callback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I> {
        return when(registry) {
            null -> registerForActivityResult(contract,callback)
            else -> registerForActivityResult(contract,registry, callback)
        }
    }
}