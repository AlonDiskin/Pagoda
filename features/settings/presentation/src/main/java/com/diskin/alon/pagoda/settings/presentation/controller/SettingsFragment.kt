package com.diskin.alon.pagoda.settings.presentation.controller

import android.Manifest
import android.content.SharedPreferences
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
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.preference.get
import com.diskin.alon.pagoda.common.appservices.results.AppError
import com.diskin.alon.pagoda.common.appservices.results.ErrorType
import com.diskin.alon.pagoda.settings.presentation.R
import com.diskin.alon.pagoda.settings.presentation.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject

/**
 * Provides the ui for app preference configuration.
 */
@OptionalInject
@AndroidEntryPoint
class SettingsFragment(registry: ActivityResultRegistry? = null) : PreferenceFragmentCompat() {

    private val viewModel: SettingsViewModel by viewModels()
    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
        when(key){
            getString(R.string.pref_dark_mode_key) -> {
                val default = getString(R.string.pref_dark_mode_default_value).toBoolean()
                val darkModeEnabled = sp.getBoolean(key,default)
                viewModel.enableDarkMode(darkModeEnabled)
            }
        }
    }
    private val locationPermissionLauncher = createActivityResultLauncher(ActivityResultContracts.RequestPermission(),registry) {
        it?.let { granted ->
            val alertKey = getString(R.string.pref_alert_notification_key)

            if (granted) {
                preferenceScreen.get<SwitchPreference>(alertKey)!!.isChecked = true
                preferenceScreen.sharedPreferences
                    .edit()
                    .putBoolean(alertKey,true)
                    .apply()
            } else {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.error.observe(viewLifecycleOwner) { it?.let(this::resolveSettingsError) }
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

    private fun resolveSettingsError(appError: AppError) {
        when(appError.type) {
            ErrorType.LOCATION_BACKGROUND_PERMISSION -> {
                val alertKey = getString(R.string.pref_alert_notification_key)
                preferenceScreen.get<SwitchPreference>(alertKey)!!.isChecked = false
                preferenceScreen.sharedPreferences
                    .edit()
                    .putBoolean(alertKey,false)
                    .apply()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    askUserForLocationPermission()
                }
            }
        }
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