package com.diskin.alon.pagoda.common.events

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.diskin.alon.pagoda.common.eventcontracts.AppEventProvider
import com.diskin.alon.pagoda.common.eventcontracts.AppEventPublisher
import io.reactivex.Observable

abstract class SharedPrefsEventHandler<E : Any>(
    private val default: E,
    private val app: Application
) : AppEventProvider<E>, AppEventPublisher<E> {

    companion object { const val EVENTS_FILE = "app_events" }

    override fun get(): Observable<E> {
        return Observable.create { emitter ->
            val sp = app.getSharedPreferences(EVENTS_FILE,Context.MODE_PRIVATE)
            val current = sp.getStringSet(eventKey(), convert(default))!!
            val unitsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                if (key == eventKey()) {
                    val event = sharedPreferences.getStringSet(eventKey(), convert(default))!!
                    emitter.onNext(create(event))
                }
            }

            // Propagate current event value
            emitter.onNext(create(current))

            // Register sharedPreferences listener to propagate unit changes
            sp.registerOnSharedPreferenceChangeListener(unitsListener)

            // Set observable to unregister listener when terminated
            emitter.setCancellable { sp.unregisterOnSharedPreferenceChangeListener(unitsListener) }
        }
    }

    override fun publish(event: E) {
        app.getSharedPreferences(EVENTS_FILE, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(eventKey(),convert(event))
            .apply()
    }

    abstract fun eventKey(): String

    abstract fun convert(event: E): Set<String>

    abstract fun create(set: Set<String>): E
}