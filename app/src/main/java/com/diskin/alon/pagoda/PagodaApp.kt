package com.diskin.alon.pagoda

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import androidx.work.Configuration
import com.diskin.alon.pagoda.home.presentation.MainActivity
import com.diskin.alon.pagoda.weather.infrastructure.WeatherAlertSettingHandlerService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PagodaApp : Application(), Configuration.Provider, Application.ActivityLifecycleCallbacks {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    private var hasNavListener = false

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        restoreTheme()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {
        if (activity is MainActivity) {
            if (!hasNavListener) {
                Navigation.findNavController(activity,R.id.nav_host_container)
                    .addOnDestinationChangedListener { _, destination, _ ->
                        if (destination.id == R.id.settingsFragment) {
                            startService(Intent(
                                this,
                                WeatherAlertSettingHandlerService::class.java)
                            )
                        } else {
                            stopService(
                                Intent(
                                    this,
                                    WeatherAlertSettingHandlerService::class.java)
                            )
                        }
                    }

                hasNavListener = true
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }

    private fun restoreTheme() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val darkModeKey = getString(R.string.pref_dark_mode_key)
        val default = getString(R.string.pref_dark_mode_default_value).toBoolean()

        when(sp.getBoolean(darkModeKey,default)) {
            true -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            false -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}