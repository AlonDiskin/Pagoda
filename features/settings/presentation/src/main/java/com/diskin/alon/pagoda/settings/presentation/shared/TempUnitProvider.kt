package com.diskin.alon.pagoda.settings.presentation.shared

import android.content.SharedPreferences
import android.content.res.Resources
import com.diskin.alon.pagoda.common.shared.AppDataProvider
import com.diskin.alon.pagoda.settings.presentation.R
import com.diskin.alon.pagoda.settings.shared.TempUnit
import com.diskin.alon.pagoda.settings.shared.UnitSystem
import io.reactivex.Observable
import javax.inject.Inject

class TempUnitProvider @Inject constructor(
    private val sp: SharedPreferences,
    private val resources: Resources
) : AppDataProvider<Observable<TempUnit>> {

    override fun get(): Observable<TempUnit> {
        return Observable.create { emitter ->
            val current = createEventFromPref()
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                val prefKey = resources.getString(R.string.pref_temperature_unit_key)

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

    private fun createEventFromPref(): TempUnit {
        val prefKey = resources.getString(R.string.pref_temperature_unit_key)
        val default = resources.getString(R.string.pref_temperature_default_value)
        val prefValue = sp.getString(prefKey,default)!!.uppercase()

        return TempUnit(UnitSystem.valueOf(prefValue))
    }
}