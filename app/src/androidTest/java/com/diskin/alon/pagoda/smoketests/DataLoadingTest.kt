package com.diskin.alon.pagoda.smoketests

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.diskin.alon.pagoda.di.AppDataModule
import com.diskin.alon.pagoda.settings.di.SettingsNetworkingModule
import com.diskin.alon.pagoda.util.DeviceUtil
import com.diskin.alon.pagoda.util.FileUtil
import com.diskin.alon.pagoda.util.NetworkUtil
import com.diskin.alon.pagoda.util.TestDatabase
import com.diskin.alon.pagoda.weatherinfo.data.BuildConfig
import com.diskin.alon.pagoda.weatherinfo.di.WeatherNetworkingModule
import com.google.common.truth.Truth.assertThat
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
 * App e2e smoke test that verify expected function of networking and db loadings.
 */
@HiltAndroidTest
@UninstallModules(SettingsNetworkingModule::class, WeatherNetworkingModule::class, AppDataModule::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
class DataLoadingTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)!!

    @Inject
    lateinit var db: TestDatabase

    @Test
    fun weatherDataNetworkUsage() {
        // Test case fixture

        // Prepare test server
        val dispatcher = object : Dispatcher() {
            private val locationWeatherRes = "assets/json/location_weather.json"
            private val locationGeoRes = "assets/json/location_geocoding.json"
            private val weatherPath = "/data/2.5/onecall"
            private val geocodingPath = "/geo/1.0/reverse"
            private var successfulResponses = 0

            override fun dispatch(request: RecordedRequest): MockResponse {
                return when(request.requestUrl.uri().path){
                    weatherPath -> {
                        if (request.requestUrl.queryParameterNames().contains("lat") &&
                            request.requestUrl.queryParameterNames().contains("lon") &&
                            request.requestUrl.queryParameter("exclude") == "minutely,alerts" &&
                            request.requestUrl.queryParameter("units") == "metric" &&
                            request.requestUrl.queryParameter("appid") == BuildConfig.OPEN_WEATHER_MAP_API_KEY
                        ) {
                            successfulResponses++
                            MockResponse()
                                .setBody(FileUtil.readStringFromFile(locationWeatherRes))
                                .setResponseCode(200)

                        } else {
                            successfulResponses--
                            MockResponse().setResponseCode(404)
                        }
                    }

                    geocodingPath -> {
                        if (request.requestUrl.queryParameterNames().contains("lat") &&
                            request.requestUrl.queryParameterNames().contains("lon") &&
                            request.requestUrl.queryParameter("limit") == "1" &&
                            request.requestUrl.queryParameter("appid") == BuildConfig.OPEN_WEATHER_MAP_API_KEY
                        ) {
                            successfulResponses++
                            MockResponse()
                                .setBody(FileUtil.readStringFromFile(locationGeoRes))
                                .setResponseCode(200)

                        } else {
                            successfulResponses--
                            MockResponse().setResponseCode(404)
                        }
                    }

                    else -> {
                        successfulResponses--
                        MockResponse().setResponseCode(404)
                    }
                }
            }

            fun getSuccessfulResponsesCount() = successfulResponses
        }
        NetworkUtil.server.setDispatcher(dispatcher)

        // Given
        DeviceUtil.launchAppFromHome()
        Thread.sleep(2000)

        // When
        DeviceUtil.rotateDeviceLand()
        Thread.sleep(2000)

        // Then
        assertThat(NetworkUtil.server.requestCount).isEqualTo(2)
        assertThat(dispatcher.getSuccessfulResponsesCount()).isEqualTo(2)
    }
}