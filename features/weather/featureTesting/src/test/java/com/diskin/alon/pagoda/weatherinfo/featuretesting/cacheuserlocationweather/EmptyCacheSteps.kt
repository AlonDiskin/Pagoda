package com.diskin.alon.pagoda.weatherinfo.featuretesting.cacheuserlocationweather

import android.os.Looper
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.pagoda.common.appservices.toResult
import com.diskin.alon.pagoda.common.eventcontracts.AppEventProvider
import com.diskin.alon.pagoda.common.eventcontracts.settings.TemperatureUnitPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.UnitPrefSystem
import com.diskin.alon.pagoda.common.eventcontracts.settings.WindSpeedUnitPref
import com.diskin.alon.pagoda.common.presentation.ImageLoader
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.weatherinfo.data.BuildConfig
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.data.local.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.featuretesting.TestDatabase
import com.diskin.alon.pagoda.common.featuretesting.getJsonFromResource
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.WeatherFragment
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Observable
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.robolectric.Shadows
import java.text.SimpleDateFormat
import java.util.*

/**
 * Step definitions for 'Cache is empty' scenario.
 */
class EmptyCacheSteps(
    private val server: MockWebServer,
    private val db: TestDatabase,
    private val locationProvider: UserLocationProvider,
    private val tempUnitPrefProvider: AppEventProvider<TemperatureUnitPref>,
    private val windSpeedUnitPrefProvider: AppEventProvider<WindSpeedUnitPref>,
    private val timeFormatPrefProvider: AppEventProvider<TimeFormatPref>
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val dispatcher = TestDispatcher()

    // Stub data
    private val date = LocalDateTime(2021,8,12,23,15)

    init {
        // Set test server with test dispatcher
        server.setDispatcher(dispatcher)

        // Set location provider for scenario
        val location = UserLocation(dispatcher.locationLat,dispatcher.locationLon)
        every { locationProvider.getLocation() } returns Observable.just(location).toResult()

        // Set app prefs providers for scenario
        every { tempUnitPrefProvider.get() } returns Observable.just(TemperatureUnitPref(
            UnitPrefSystem.METRIC))
        every { windSpeedUnitPrefProvider.get() } returns Observable.just(WindSpeedUnitPref(
            UnitPrefSystem.METRIC))
        every { timeFormatPrefProvider.get() } returns Observable.just(TimeFormatPref(TimeFormatPref.HourFormat.HOUR_24))

        // Set image loader for scenario
        mockkObject(ImageLoader)
        every { ImageLoader.loadIconResIntoImageView(any(),any()) } returns Unit

        // Set system calendar for scenario
        val calendar: Calendar = mockk()

        mockkStatic(Calendar::class)
        every { Calendar.getInstance() } returns calendar
        every { calendar.timeInMillis } returns date.toDate().time

    }

    @Given("^App has no cached location weather data$")
    fun app_has_no_cached_location_weather_data() {
        // Verify test db weather table is empty
        val rowsCount = db.compileStatement("SELECT COUNT(*) FROM current_weather")
            .simpleQueryForLong()

        assertThat(rowsCount).isEqualTo(0)
    }

    @When("^User open current weather screen$")
    fun user_open_current_weather_screen() {
        // Launch weather fragment
        scenario = launchFragmentInHiltContainer<WeatherFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^App should loaded weather data and cache it$")
    fun app_should_load_weather_data_and_cache_it() {
        // Verify app loaded weather data from test server
        assertThat(server.requestCount).isEqualTo(2)

        // Verify app saved weather data from server in test db
        val serverWeather = getServerWeather()
        val query = SimpleSQLiteQuery("SELECT * FROM current_weather WHERE id = 1")
        val cursor = db.query(query).also { it.moveToFirst() }

        assertThat(cursor.count).isEqualTo(1)
        assertThat(cursor.getDouble(cursor.getColumnIndex("lat")))
            .isEqualTo(serverWeather.weather.lat)
        assertThat(cursor.getDouble(cursor.getColumnIndex("lon")))
            .isEqualTo(serverWeather.weather.lon)
        assertThat(cursor.getString(cursor.getColumnIndex("name")))
            .isEqualTo(serverWeather.location.name)
        assertThat(cursor.getString(cursor.getColumnIndex("country")))
            .isEqualTo(serverWeather.location.country)
        assertThat(cursor.getString(cursor.getColumnIndex("timeZone")))
            .isEqualTo(serverWeather.weather.timezone)
        assertThat(cursor.getDouble(cursor.getColumnIndex("currentTemp")))
            .isEqualTo(serverWeather.weather.current.temp)
        assertThat(cursor.getDouble(cursor.getColumnIndex("feelTemp")))
            .isEqualTo(serverWeather.weather.current.feels_like)
        assertThat(cursor.getDouble(cursor.getColumnIndex("humidity")))
            .isEqualTo(serverWeather.weather.current.humidity)
        assertThat(cursor.getDouble(cursor.getColumnIndex("windSpeed")))
            .isEqualTo(serverWeather.weather.current.wind_speed)
        assertThat(cursor.getLong(cursor.getColumnIndex("sunrise")))
            .isEqualTo(LocalDateTime(
                serverWeather.weather.current.sunrise * 1000,
                DateTimeZone.forID(serverWeather.weather.timezone)
            ).toDate().time)
        assertThat(cursor.getLong(cursor.getColumnIndex("sunset")))
            .isEqualTo(LocalDateTime(
                serverWeather.weather.current.sunset * 1000,
                DateTimeZone.forID(serverWeather.weather.timezone)
            ).toDate().time)
        assertThat(cursor.getDouble(cursor.getColumnIndex("uvIndexValue")))
            .isEqualTo(serverWeather.weather.current.uvi)
        assertThat(cursor.getInt(cursor.getColumnIndex("unitSystem")))
            .isEqualTo(0)
        assertThat(cursor.getLong(cursor.getColumnIndex("updated")))
            .isEqualTo(date.toDate().time)
    }

    @And("^Weather should be shown with cache update time$")
    fun weather_should_be_shown_with_cache_update_time() {
        // Verify cache update time shown
        val format = "dd MMM, HH:mm"
        val time = SimpleDateFormat(format).format(Date(date.toDate().time))
        val expectedUpdate = "Last updated: ".plus(time)

        onView(withId(R.id.updated))
            .check(matches(withText(expectedUpdate)))
    }

    private fun getServerWeather(): ServerWeather {
        val weatherJson = getJsonFromResource(dispatcher.locationWeatherRes)
        val locationJson = getJsonFromResource(dispatcher.locationGeoRes)
        val gson = Gson()
        val weatherResponse = gson.fromJson(weatherJson, ServerWeatherResponse::class.java)
        val locationResponse = gson.fromJson(locationJson, Array<ServerLocationResponse>::class.java)
            .toList().first()

        return ServerWeather(weatherResponse,locationResponse)
    }

    private class ServerWeather(val weather: ServerWeatherResponse,
                                val location: ServerLocationResponse)

    data class ServerWeatherResponse(val lat: Double,
                                     val lon: Double,
                                     val timezone: String,
                                     val current: Current,
                                     val hourly: List<Hourly>,
                                     val daily: List<Daily>) {

        data class Current(val sunrise: Long,
                           val sunset: Long,
                           val temp: Double,
                           val feels_like: Double,
                           val humidity: Double,
                           val wind_speed: Double,
                           val uvi: Double,
                           val weather: List<Weather>)

        data class Weather(val id: Int, val icon: String)

        data class Hourly(val dt: Long,val temp: Double,val weather: List<Weather>)

        data class Daily(val dt: Long, val temp: Temp, val weather: List<Weather>)

        data class Temp(val day: Double,
                        val min: Double,
                        val max: Double)
    }

    data class ServerLocationResponse(val name: String, val country: String)

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