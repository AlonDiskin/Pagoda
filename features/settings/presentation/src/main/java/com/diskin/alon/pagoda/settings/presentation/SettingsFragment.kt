package com.diskin.alon.pagoda.settings.presentation

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceFragmentCompat
import com.diskin.alon.pagoda.settings.appservices.WeatherUnit.*
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject

@OptionalInject
@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel: SettingsViewModel by viewModels()
    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
        when(key){
            getString(R.string.pref_temperature_unit_key) -> handleTemperatureUnitPrefChange(sp,key)
            getString(R.string.pref_wind_speed_unit_key) -> handleWindSpeedUnitPrefChange(sp,key)
            getString(R.string.pref_time_format_key) -> handleTimeFormatUnitPrefChange(sp,key)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
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
                viewModel.updateWeatherUnits(Temperature(UnitSystem.METRIC))
            getString(R.string.pref_units_imperial_value) ->
                viewModel.updateWeatherUnits(Temperature(UnitSystem.IMPERIAL))
        }
    }

    private fun handleWindSpeedUnitPrefChange(sp: SharedPreferences, key: String) {
        val defaultUnit = getString(R.string.pref_wind_speed_default_value)

        when(sp.getString(key,defaultUnit)) {
            getString(R.string.pref_units_metric_value) ->
                viewModel.updateWeatherUnits(WindSpeed(UnitSystem.METRIC))
            getString(R.string.pref_units_imperial_value) ->
                viewModel.updateWeatherUnits(WindSpeed(UnitSystem.IMPERIAL))
        }
    }

    private fun handleTimeFormatUnitPrefChange(sp: SharedPreferences, key: String) {
        val defaultUnit = getString(R.string.pref_time_format_default_value)

        when(sp.getString(key,defaultUnit)) {
            getString(R.string.pref_time_format_12_value) ->
                viewModel.updateWeatherUnits(TimeFormat(HourFormat.HOUR_12))
            getString(R.string.pref_time_format_24_value) ->
                viewModel.updateWeatherUnits(TimeFormat(HourFormat.HOUR_24))
        }
    }
}