package com.diskin.alon.pagoda.common.events

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import io.reactivex.Observable
import javax.inject.Inject

class WeatherUnitsEventHandler @Inject constructor(
    private val app: Application
) : WeatherUnitsEventProvider, WeatherUnitsEventPublisher {

    companion object {
        val DEFAULT_UNIT = UnitSystemEvent.METRIC
        const val UNIT_KEY = "weather_unit_key"
        const val PREF_FILE = "weather_unit"
    }

    override fun get(): Observable<UnitSystemEvent> {
         return Observable.create { emitter ->
            val sp = app.getSharedPreferences(PREF_FILE,Context.MODE_PRIVATE)
            val current = sp.getInt(UNIT_KEY, DEFAULT_UNIT.ordinal)
            val unitsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                if (key == UNIT_KEY) {
                    val unit = sharedPreferences.getInt(UNIT_KEY, DEFAULT_UNIT.ordinal)
                    emitter.onNext(UnitSystemEvent.values()[unit])
                }
            }

            // Propagate current unit
            emitter.onNext(UnitSystemEvent.values()[current])

            // Register sharedPreferences listener to propagate unit changes
            sp.registerOnSharedPreferenceChangeListener(unitsListener)

            // Set observable to unregister listener when terminated
            emitter.setCancellable { sp.unregisterOnSharedPreferenceChangeListener(unitsListener) }
        }
    }

    override fun post(event: UnitSystemEvent) {
        app.getSharedPreferences(PREF_FILE,Context.MODE_PRIVATE)
            .edit()
            .putInt(UNIT_KEY,event.ordinal)
            .apply()
    }
}