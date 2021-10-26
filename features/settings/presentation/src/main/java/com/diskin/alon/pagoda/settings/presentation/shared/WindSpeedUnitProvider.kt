package com.diskin.alon.pagoda.settings.presentation.shared

import android.content.SharedPreferences
import android.content.res.Resources
import com.diskin.alon.pagoda.common.shared.AppDataProvider
import com.diskin.alon.pagoda.settings.presentation.R
import com.diskin.alon.pagoda.settings.shared.UnitSystem
import com.diskin.alon.pagoda.settings.shared.WindSpeedUnit
import io.reactivex.Observable
import javax.inject.Inject

class WindSpeedUnitProvider @Inject constructor(
    private val sp: SharedPreferences,
    private val resources: Resources
) : AppDataProvider<Observable<WindSpeedUnit>> {
    override fun get(): Observable<WindSpeedUnit> {
        return Observable.create { emitter ->
            val current = createEventFromPref()
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                val prefKey = resources.getString(R.string.pref_wind_speed_unit_key)

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

    private fun createEventFromPref(): WindSpeedUnit {
        val prefKey = resources.getString(R.string.pref_wind_speed_unit_key)
        val default = resources.getString(R.string.pref_temperature_default_value)
        val prefValue = sp.getString(prefKey,default)!!.uppercase()

        return WindSpeedUnit(UnitSystem.valueOf(prefValue))
    }
}