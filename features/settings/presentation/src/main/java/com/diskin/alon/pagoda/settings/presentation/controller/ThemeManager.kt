package com.diskin.alon.pagoda.settings.presentation.controller

import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

/**
 * Handles application theme configuration.
 */
@ViewModelScoped
class ThemeManager @Inject constructor() {

    /**
     * Enable/Disable app dark theme.
     */
    fun enableDarkMode(enable: Boolean) {
        when(enable) {
            true -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            false -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}