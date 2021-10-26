package com.diskin.alon.pagoda.settings.presentation

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diskin.alon.pagoda.settings.presentation.shared.TimeFormatProvider
import com.diskin.alon.pagoda.settings.shared.TimeFormat
import com.diskin.alon.pagoda.settings.shared.TimeFormat.HourFormat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * [TimeFormatProvider] integration test class.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class TimeFormatProviderTest {

    // Test subject
    private lateinit var provider: TimeFormatProvider
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setUp() {
        provider = TimeFormatProvider(
            PreferenceManager.getDefaultSharedPreferences(context),
            context.resources
        )
    }

    @Test
    fun provideCurrentTimeFormat_WhenClientSubscribes() {
        // Given
        val currentUnit = TimeFormat(
            when(context.getString(R.string.pref_time_format_default_value)) {
                "24" -> HourFormat.HOUR_24
                else -> HourFormat.HOUR_12
            }
        )

        // When
        val observer = provider.get().test()

        // Then
        observer.assertValue(currentUnit)
    }

    @Test
    fun updateClientTimeFormat_WhenUnitChanges() {
        // Given
        val expectedUpdate = TimeFormat(HourFormat.HOUR_12)
        val observer = provider.get().test()

        // When
        val unit = context.getString(R.string.pref_time_format_12_value)
        val key = context.getString(R.string.pref_time_format_key)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(key,unit)
            .commit()

        // Then
        observer.assertValueAt(1,expectedUpdate)
    }
}