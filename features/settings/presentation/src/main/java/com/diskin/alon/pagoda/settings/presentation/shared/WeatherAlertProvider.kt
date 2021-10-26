package com.diskin.alon.pagoda.settings.presentation.shared

import android.content.SharedPreferences
import android.content.res.Resources
import com.diskin.alon.pagoda.common.shared.AppDataProvider
import com.diskin.alon.pagoda.settings.presentation.R
import com.diskin.alon.pagoda.settings.shared.WeatherAlertEnabled
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class WeatherAlertProvider @Inject constructor(
    private val sp: SharedPreferences,
    private val resources: Resources
) : AppDataProvider<Observable<WeatherAlertEnabled>> {

    override fun get(): Observable<WeatherAlertEnabled> {
        return Observable.create<WeatherAlertEnabled> { emitter ->
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                val prefKey = resources.getString(R.string.pref_alert_notification_key)

                if (key == prefKey) {
                    emitter.onNext(createEventFromPref())
                }
            }

            // Register sharedPreferences listener to propagate unit changes
            sp.registerOnSharedPreferenceChangeListener(listener)

            // Set observable to unregister listener when terminated
            emitter.setCancellable { sp.unregisterOnSharedPreferenceChangeListener(listener) }
        }.subscribeOn(Schedulers.io())
    }

    private fun createEventFromPref(): WeatherAlertEnabled {
        val prefKey = resources.getString(R.string.pref_alert_notification_key)
        val default = resources.getString(R.string.pref_alert_notification_default_value).toBoolean()
        val prefValue = sp.getBoolean(prefKey,default)

        return WeatherAlertEnabled(prefValue)
    }
}