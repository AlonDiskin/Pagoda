package com.diskin.alon.pagoda.settings.presentation

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.pagoda.settings.presentation.shared.TempUnitProvider
import com.diskin.alon.pagoda.settings.shared.TempUnit
import com.diskin.alon.pagoda.settings.shared.UnitSystem
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * [TempUnitProvider] integration test class.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class TempUnitProviderTest {

    // Test subject
    private lateinit var provider: TempUnitProvider
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setUp() {
        provider = TempUnitProvider(
            PreferenceManager.getDefaultSharedPreferences(context),
            context.resources
        )
    }

    @Test
    fun provideCurrentTempUnit_WhenClientSubscribes() {
        // Given
        val currentUnit = TempUnit(
            enumValueOf(context.getString(R.string.pref_temperature_default_value).uppercase())
        )

        // When
        val observer = provider.get().test()

        // Then
        observer.assertValue(currentUnit)
    }

    @Test
    fun updateClientTempUnit_WhenUnitChanges() {
        // Given
        val expectedUpdate = TempUnit(UnitSystem.IMPERIAL)
        val observer = provider.get().test()

        // When
        val unit = context.getString(R.string.pref_units_imperial_value)
        val key = context.getString(R.string.pref_temperature_unit_key)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(key,unit)
            .commit()

        // Then
        observer.assertValueAt(1,expectedUpdate)
    }
}