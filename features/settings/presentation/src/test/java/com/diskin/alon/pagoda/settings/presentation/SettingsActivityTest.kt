package com.diskin.alon.pagoda.settings.presentation

import androidx.appcompat.widget.Toolbar
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/**
 * [SettingsActivity] hermetic ui test.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(sdk = [28])
class SettingsActivityTest {

    // Test subject
    private lateinit var scenario: ActivityScenario<SettingsActivity>

    @Before
    fun setUp() {
        // Launch activity under test
        scenario = ActivityScenario.launch(SettingsActivity::class.java)
    }

    @Test
    fun showTitleInAppBar() {
        // Given a resumed activity

        // Then activity should show activity title in toolbar
        scenario.onActivity { activity ->
            val actualTitle = activity.findViewById<Toolbar>(R.id.toolbar).title!!
            val expectedTitle = activity.getString(R.string.title_settings_activity)

            assertThat(actualTitle).isEqualTo(expectedTitle)
        }
    }

    @Test
    fun showSettingsFragment() {
        // Given a resumed activity

        // Then activity should show settings fragment in its ui
        scenario.onActivity {
            val actualDisplayed =  it.supportFragmentManager.fragments.first()!!

            assertThat(actualDisplayed).isInstanceOf(SettingsFragment::class.java)
        }
    }
}