package com.diskin.alon.pagoda.weatherinfo.featuretesting.browse_world_location_weather

import android.os.Bundle
import android.os.Looper
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.test.core.app.ActivityScenario
import com.diskin.alon.pagoda.common.featuretesting.getJsonFromResource
import com.diskin.alon.pagoda.common.presentation.ImageLoader
import com.diskin.alon.pagoda.common.shared.AppDataProvider
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.settings.shared.TempUnit
import com.diskin.alon.pagoda.settings.shared.TimeFormat
import com.diskin.alon.pagoda.settings.shared.UnitSystem
import com.diskin.alon.pagoda.settings.shared.WindSpeedUnit
import com.diskin.alon.pagoda.weatherinfo.data.BuildConfig
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.featuretesting.util.verifyServerWeatherShown
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.WeatherFragment
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import io.mockk.every
import io.mockk.mockkObject
import io.reactivex.Observable
import io.reactivex.subjects.SingleSubject
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.robolectric.Shadows

/**
 * Step definitions runner for 'Latest weather shown for world location' scenario.
 */
class WorldLocationWeatherShownSteps(
    server: MockWebServer,
    tempUnitProvider: AppDataProvider<Observable<TempUnit>>,
    windSpeedUnitProvider: AppDataProvider<Observable<WindSpeedUnit>>,
    timeFormatProvider: AppDataProvider<Observable<TimeFormat>>,
    locationProvider: UserLocationProvider,
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val dispatcher = TestDispatcher()

    // Stub data
    private lateinit var bundle: Bundle

    init {
        // Stub location provider
        every { locationProvider.getLocation() } returns SingleSubject.create()
        // Stub mock server
        server.setDispatcher(dispatcher)

        // Stub app prefs providers
        every { tempUnitProvider.get() } returns Observable.just(TempUnit(UnitSystem.METRIC))
        every { windSpeedUnitProvider.get() } returns Observable.just(WindSpeedUnit(UnitSystem.METRIC))
        every { timeFormatProvider.get() } returns Observable.just(TimeFormat(TimeFormat.HourFormat.HOUR_24))

        // Mock and stub image loader
        mockkObject(ImageLoader)
        every { ImageLoader.loadIconResIntoImageView(any(),any()) } returns Unit
    }

    @Given("^User open weather screen for world location$")
    fun user_open_weather_screen_for_world_location() {
        scenario = launchFragmentInHiltContainer<WeatherFragment>()

        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as WeatherFragment
            bundle = bundleOf(
                it.getString(R.string.arg_lat_key) to dispatcher.locationLat,
                it.getString(R.string.arg_lon_key) to dispatcher.locationLon
            )
            val requestKey = it.getString(R.string.locaiton_request_key)

            fragment.setFragmentResult(requestKey,bundle)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^Weather is shown for selected location$")
    fun weather_is_shown_for_selected_location() {
        verifyServerWeatherShown(dispatcher.locationWeatherRes,dispatcher.locationGeoRes,scenario)
    }

    private class TestDispatcher: Dispatcher() {
        val locationWeatherRes = "json/current_weather_location1.json"
        val locationLat = 55.7558
        val locationLon = 37.6173
        val locationGeoRes = "json/geocoding_location1.json"
        private val weatherPath = "/data/2.5/onecall"
        private val geocodingPath = "/geo/1.0/reverse"

        override fun dispatch(request: RecordedRequest): MockResponse {
            return when(request.requestUrl.uri().path){
                weatherPath -> {
                    if (request.requestUrl.queryParameter("lat") == locationLat.toString() &&
                        request.requestUrl.queryParameter("lon") == locationLon.toString() &&
                        request.requestUrl.queryParameter("exclude") == "minutely,alerts" &&
                        request.requestUrl.queryParameter("units") == "metric" &&
                        request.requestUrl.queryParameter("appid") == BuildConfig.OPEN_WEATHER_MAP_API_KEY
                    ) {

                        MockResponse()
                            .setBody(getJsonFromResource(locationWeatherRes))
                            .setResponseCode(200)

                    } else {
                        MockResponse().setResponseCode(404)
                    }
                }

                geocodingPath -> {
                    if (request.requestUrl.queryParameter("lat") == locationLat.toString() &&
                        request.requestUrl.queryParameter("lon") == locationLon.toString() &&
                        request.requestUrl.queryParameter("limit") == "1" &&
                        request.requestUrl.queryParameter("appid") == BuildConfig.OPEN_WEATHER_MAP_API_KEY
                    ) {

                        MockResponse()
                            .setBody(getJsonFromResource(locationGeoRes))
                            .setResponseCode(200)

                    } else {
                        MockResponse().setResponseCode(404)
                    }
                }

                else -> MockResponse().setResponseCode(404)
            }
        }
    }
}