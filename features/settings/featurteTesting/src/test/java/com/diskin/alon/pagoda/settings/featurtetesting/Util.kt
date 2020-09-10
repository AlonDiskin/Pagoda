package com.diskin.alon.pagoda.settings.featurtetesting

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider

fun clearSharedPrefs() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = prefs.edit()
    editor.clear()
    editor.commit()
}
