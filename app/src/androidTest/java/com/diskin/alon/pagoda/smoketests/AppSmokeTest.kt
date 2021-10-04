package com.diskin.alon.pagoda.smoketests

import android.Manifest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.diskin.alon.pagoda.R
import com.diskin.alon.pagoda.settings.di.SettingsNetworkingModule
import com.diskin.alon.pagoda.util.DeviceUtil
import com.diskin.alon.pagoda.util.FileUtil
import com.diskin.alon.pagoda.util.NetworkUtil
import com.diskin.alon.pagoda.weatherinfo.di.WeatherNetworkingModule
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.json.JSONArray
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * App e2e smoke tests for crucial app functionality verification.
 */
@HiltAndroidTest
@UninstallModules(SettingsNetworkingModule::class, WeatherNetworkingModule::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
class AppSmokeTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)!!

    @Inject
    lateinit var locationProviderClient: FusedLocationProviderClient

    @Before
    fun setUp() {
        // Inject test app dependencies
        hiltRule.inject()
    }

    @Test
    fun weatherDataNetworkUsage() {
        // Given
        DeviceUtil.launchAppFromHome()
        DeviceUtil.approveLocationDialogIfExist()

        // When
        DeviceUtil.rotateDeviceLand()
        DeviceUtil.rotateDevicePort()

        // Then
        val locationName = JSONArray(FileUtil.readStringFromFile(NetworkUtil.dispatcher.geoRes))
            .getJSONObject(0).getString("name")
        val countryName = JSONArray(FileUtil.readStringFromFile(NetworkUtil.dispatcher.geoRes))
            .getJSONObject(0).getString("country")

        onView(withId(R.id.location_name))
            .check(matches(withText(locationName.plus(", ").plus(countryName))))
        assertThat(NetworkUtil.server.requestCount).isEqualTo(2)
    }
}