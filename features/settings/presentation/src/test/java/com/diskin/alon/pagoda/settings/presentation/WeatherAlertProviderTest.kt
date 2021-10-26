package com.diskin.alon.pagoda.settings.presentation

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.pagoda.settings.presentation.shared.TempUnitProvider
import com.diskin.alon.pagoda.settings.presentation.shared.WeatherAlertProvider
import com.diskin.alon.pagoda.settings.shared.TempUnit
import com.diskin.alon.pagoda.settings.shared.UnitSystem
import com.diskin.alon.pagoda.settings.shared.WeatherAlertEnabled
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * [WeatherAlertProvider] integration test class.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class WeatherAlertProviderTest {

    // Test subject
    private lateinit var provider: WeatherAlertProvider
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setUp() {
        provider = WeatherAlertProvider(
            PreferenceManager.getDefaultSharedPreferences(context),
            context.resources
        )
    }

    @Test
    fun updateClientTempUnit_WhenUnitChanges() {
        // Given
        val expectedUpdate = WeatherAlertEnabled(true)
        val observer = provider.get().test()

        // When
        val enabled = true
        val key = context.getString(R.string.pref_alert_notification_key)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key,enabled)
            .commit()

        // Then
        observer.assertValueAt(0,expectedUpdate)
    }
}