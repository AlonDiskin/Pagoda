package com.diskin.alon.pagoda.settings.presentation

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceFragmentCompat
import com.diskin.alon.pagoda.common.events.UnitSystemEvent
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject

@OptionalInject
@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel: SettingsViewModel by viewModels()
    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
        when(key){
            getString(R.string.pref_units_key) -> handleUnitPrefChange(sp,key)
        }
    }

    private fun handleUnitPrefChange(sp: SharedPreferences, key: String) {
        val defaultUnit = getString(R.string.pref_units_default_value)

        when(sp.getString(key,defaultUnit)) {
            getString(R.string.pref_units_metric_value) ->
                viewModel.updateWeatherUnit(UnitSystemEvent.METRIC)
            getString(R.string.pref_units_imperial_value) ->
                viewModel.updateWeatherUnit(UnitSystemEvent.IMPERIAL)
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
}