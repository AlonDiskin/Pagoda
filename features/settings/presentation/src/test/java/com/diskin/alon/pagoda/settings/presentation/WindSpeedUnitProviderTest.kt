package com.diskin.alon.pagoda.settings.presentation

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.pagoda.settings.presentation.shared.WindSpeedUnitProvider
import com.diskin.alon.pagoda.settings.shared.UnitSystem
import com.diskin.alon.pagoda.settings.shared.WindSpeedUnit
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * [WindSpeedUnitProvider] integration test class.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class WindSpeedUnitProviderTest {

    // Test subject
    private lateinit var provider: WindSpeedUnitProvider
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setUp() {
        provider = WindSpeedUnitProvider(
            PreferenceManager.getDefaultSharedPreferences(context),
            context.resources
        )
    }

    @Test
    fun provideCurrentWindSpeedUnit_WhenClientSubscribes() {
        // Given
        val currentUnit = WindSpeedUnit(
            enumValueOf(context.getString(R.string.pref_wind_speed_default_value).uppercase())
        )

        // When
        val observer = provider.get().test()

        // Then
        observer.assertValue(currentUnit)
    }

    @Test
    fun updateClientWindSpeedUnit_WhenUnitChanges() {
        // Given
        val expectedUpdate = WindSpeedUnit(UnitSystem.IMPERIAL)
        val observer = provider.get().test()

        // When
        val unit = context.getString(R.string.pref_units_imperial_value)
        val key = context.getString(R.string.pref_wind_speed_unit_key)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(key,unit)
            .commit()

        // Then
        observer.assertValueAt(1,expectedUpdate)
    }
}