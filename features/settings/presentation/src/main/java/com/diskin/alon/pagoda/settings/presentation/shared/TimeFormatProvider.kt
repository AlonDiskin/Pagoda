package com.diskin.alon.pagoda.settings.presentation.shared

import android.content.SharedPreferences
import android.content.res.Resources
import com.diskin.alon.pagoda.common.shared.AppDataProvider
import com.diskin.alon.pagoda.settings.presentation.R
import com.diskin.alon.pagoda.settings.shared.TimeFormat
import io.reactivex.Observable
import javax.inject.Inject

class TimeFormatProvider @Inject constructor(
    private val sp: SharedPreferences,
    private val resources: Resources
) : AppDataProvider<Observable<TimeFormat>> {

    override fun get(): Observable<TimeFormat> {
        return Observable.create { emitter ->
            val current = createEventFromPref()
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                val prefKey = resources.getString(R.string.pref_time_format_key)

                if (key == prefKey) {
                    emitter.onNext(createEventFromPref())
                }
            }

            // Propagate current event value
            emitter.onNext(current)

            // Register sharedPreferences listener to propagate unit changes
            sp.registerOnSharedPreferenceChangeListener(listener)

            // Set observable to unregister listener when terminated
            emitter.setCancellable { sp.unregisterOnSharedPreferenceChangeListener(listener) }
        }
    }

    private fun createEventFromPref(): TimeFormat {
        val prefKey = resources.getString(R.string.pref_time_format_key)
        val default = resources.getString(R.string.pref_time_format_default_value)
        val timeFormat = when(sp.getString(prefKey,default)!!) {
            "24" -> TimeFormat.HourFormat.HOUR_24
            else -> TimeFormat.HourFormat.HOUR_12
        }

        return TimeFormat(timeFormat)
    }
}