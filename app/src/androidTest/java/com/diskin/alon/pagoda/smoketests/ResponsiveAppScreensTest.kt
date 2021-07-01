package com.diskin.alon.pagoda.smoketests

import android.Manifest
import androidx.appcompat.widget.SearchView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.open
import androidx.test.espresso.contrib.NavigationViewActions.navigateTo
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.diskin.alon.pagoda.R
import com.diskin.alon.pagoda.common.uitesting.typeSearchViewText
import com.diskin.alon.pagoda.di.AppDataModule
import com.diskin.alon.pagoda.settings.di.SettingsNetworkingModule
import com.diskin.alon.pagoda.util.DeviceUtil
import com.diskin.alon.pagoda.util.FileUtil
import com.diskin.alon.pagoda.util.NetworkUtil
import com.diskin.alon.pagoda.util.TestDatabase
import com.diskin.alon.pagoda.weatherinfo.di.WeatherNetworkingModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * App e2e smoke test that verify expected function of UI for different screen variations.
 */
@HiltAndroidTest
@UninstallModules(SettingsNetworkingModule::class, WeatherNetworkingModule::class, AppDataModule::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
class ResponsiveAppScreensTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)!!

    @Inject
    lateinit var db: TestDatabase

    @Test
    fun showLandscapeUiWhenRotatePortraitWeatherInfoScreen() {
        // Test case fixture

        // Prepare test server
        NetworkUtil.server.setDispatcher(object : Dispatcher() {
            private val locationWeatherRes = "assets/json/location_weather.json"
            private val locationGeoRes = "assets/json/location_geocoding.json"
            private val weatherPath = "/data/2.5/onecall"
            private val geocodingPath = "/geo/1.0/reverse"

            override fun dispatch(request: RecordedRequest): MockResponse {
                return when(request.requestUrl.uri().path){
                    weatherPath -> MockResponse()
                        .setBody(FileUtil.readStringFromFile(locationWeatherRes))
                        .setResponseCode(200)

                    geocodingPath -> MockResponse()
                        .setBody(FileUtil.readStringFromFile(locationGeoRes))
                        .setResponseCode(200)

                    else -> MockResponse().setResponseCode(404)
                }
            }
        })

        // Given
        DeviceUtil.launchAppFromHome()
        Thread.sleep(1000)

        // When
        DeviceUtil.rotateDeviceLand()
        Thread.sleep(1000)

        // Then
        onView(withId(R.id.two_pane_weather_data))
            .check(matches(withEffectiveVisibility(VISIBLE)))
    }

    @Test
    fun showLandscapeUiWhenRotatePortraitLocationsScreen() {
        // Test case fixture

        // Prepare test server
        NetworkUtil.server.setDispatcher(object : Dispatcher() {

            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().setResponseCode(404)
            }
        })

        // Prepare test db
        val insertSql = "INSERT INTO locations (lat,lon,name,country,state,bookmarked)" +
                "VALUES(23.5,45.73,'paris','FR','',1);"

        hiltRule.inject()
        db.compileStatement(insertSql).executeInsert()

        // Given
        DeviceUtil.launchAppFromHome()
        Thread.sleep(1000)

        // When
        onView(withId(R.id.drawerLayout))
            .perform(open())

        onView(withId(R.id.nav_view))
            .perform(navigateTo(R.id.nav_locations))

        DeviceUtil.rotateDeviceLand()
        Thread.sleep(1000)

        // Then
        onView(withId(R.id.bookmark_land_root))
            .check(matches(withEffectiveVisibility(VISIBLE)))
    }

    @Test
    fun showLandscapeUiWhenRotatePortraitLocationSearchScreen() {
        // Test case fixture

        // Prepare test server
        NetworkUtil.server.setDispatcher(object : Dispatcher() {

            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().setResponseCode(404)
            }
        })

        // Prepare test db
        val insertSql = "INSERT INTO locations (lat,lon,name,country,state,bookmarked)" +
                "VALUES(23.5,45.73,'paris','FR','',0);"

        hiltRule.inject()
        db.compileStatement(insertSql).executeInsert()

        // Given
        DeviceUtil.launchAppFromHome()
        Thread.sleep(1000)

        // When
        onView(withId(R.id.drawerLayout))
            .perform(open())

        onView(withId(R.id.nav_view))
            .perform(navigateTo(R.id.nav_search))

        DeviceUtil.rotateDeviceLand()
        Thread.sleep(1000)

        // And
        onView(ViewMatchers.isAssignableFrom(SearchView::class.java))
            .perform(typeSearchViewText("paris"))

        // Then
        onView(withId(R.id.search_result_land_root))
            .check(matches(withEffectiveVisibility(VISIBLE)))
    }
}