package com.diskin.alon.pagoda.weatherinfo.featuretesting.browse_current_location_weather

import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.pagoda.common.appservices.results.Result
import com.diskin.alon.pagoda.common.featuretesting.getJsonFromResource
import com.diskin.alon.pagoda.common.shared.AppDataProvider
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.common.uitesting.withTimeFormat12
import com.diskin.alon.pagoda.common.uitesting.withTimeFormat24
import com.diskin.alon.pagoda.settings.shared.TempUnit
import com.diskin.alon.pagoda.settings.shared.TimeFormat
import com.diskin.alon.pagoda.settings.shared.UnitSystem
import com.diskin.alon.pagoda.settings.shared.WindSpeedUnit
import com.diskin.alon.pagoda.weatherinfo.data.BuildConfig
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.data.local.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.featuretesting.util.TestDatabase
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.HourlyForecastAdapter.HourlyForecastViewHolder
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.WeatherFragment
import com.google.common.truth.Truth.assertThat
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.json.JSONObject
import org.robolectric.Shadows
import java.text.SimpleDateFormat
import kotlin.math.roundToInt

/**
 * Step definitions for 'Weather units preference changed' scenario.
 */
class UnitsPrefChangedSteps(
    private val locationProvider: UserLocationProvider,
    server: MockWebServer,
    private val db: TestDatabase,
    private val tempUnitProvider: AppDataProvider<Observable<TempUnit>>,
    private val windSpeedUnitProvider: AppDataProvider<Observable<WindSpeedUnit>>,
    private val timeFormatProvider: AppDataProvider<Observable<TimeFormat>>
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val dispatcher = TestDispatcher()
    private lateinit var tempUnitProviderSubject: BehaviorSubject<TempUnit>
    private lateinit var windUnitProviderSubject: BehaviorSubject<WindSpeedUnit>
    private lateinit var timeFormatProviderSubject: BehaviorSubject<TimeFormat>

    init {
        // Stub mock server
        server.setDispatcher(dispatcher)

        // Stub location provider
        val location = UserLocation(dispatcher.locationLat, dispatcher.locationLon)
        every { locationProvider.getLocation() } returns Single.just(Result.Success(location))
    }

    @Given("^Unit \"([^\"]*)\" preference is set as \"([^\"]*)\"$")
    fun unit_preference_is_set_as(unit: String, current: String) {
        // Prepare app prefs according to test arguments
        when(unit){
            "temperature" -> {
                windUnitProviderSubject = BehaviorSubject.createDefault(
                    WindSpeedUnit(
                        UnitSystem.METRIC
                    )
                )
                timeFormatProviderSubject = BehaviorSubject.createDefault(
                    TimeFormat(
                        TimeFormat.HourFormat.HOUR_24
                    )
                )
                tempUnitProviderSubject = when (current) {
                    "metric" -> BehaviorSubject.createDefault(TempUnit(UnitSystem.METRIC))
                    "imperial" -> BehaviorSubject.createDefault(TempUnit(UnitSystem.IMPERIAL))
                    else -> throw IllegalArgumentException("Unknown step arg: $current")
                }
            }

            "wind speed" -> {
                timeFormatProviderSubject = BehaviorSubject.createDefault(
                    TimeFormat(
                        TimeFormat.HourFormat.HOUR_24
                    )
                )
                tempUnitProviderSubject = BehaviorSubject.createDefault(
                    TempUnit(
                        UnitSystem.METRIC
                    )
                )
                windUnitProviderSubject = when (current) {
                    "metric" -> BehaviorSubject.createDefault(WindSpeedUnit(UnitSystem.METRIC))
                    "imperial" -> BehaviorSubject.createDefault(WindSpeedUnit(UnitSystem.IMPERIAL))
                    else -> throw IllegalArgumentException("Unknown step arg: $current")
                }
            }

            "time format" -> {
                tempUnitProviderSubject = BehaviorSubject.createDefault(
                    TempUnit(
                        UnitSystem.METRIC
                    )
                )
                windUnitProviderSubject = BehaviorSubject.createDefault(
                    WindSpeedUnit(
                        UnitSystem.METRIC
                    )
                )
                timeFormatProviderSubject = when (current) {
                    "24 hour" -> BehaviorSubject.createDefault(
                        TimeFormat(
                            TimeFormat.HourFormat.HOUR_24)
                    )
                    "12 hour" -> BehaviorSubject.createDefault(
                        TimeFormat(
                            TimeFormat.HourFormat.HOUR_12)
                    )
                    else -> throw IllegalArgumentException("Unknown step arg: $current")
                }
            }

            else -> throw IllegalArgumentException("Unknown step arg: $unit")
        }

        every { tempUnitProvider.get() } returns tempUnitProviderSubject
        every { windSpeedUnitProvider.get() } returns windUnitProviderSubject
        every { timeFormatProvider.get() } returns timeFormatProviderSubject
    }

    @And("^App has no cached weather$")
    fun app_has_no_cached_weather() {
        // Verify cache is empty
        val cursor = db.query("SELECT * FROM current_weather",null)
        assertThat(cursor.count).isEqualTo(0)
    }

    @When("^User open weather screen$")
    fun user_open_weather_screen() {
        // Launch weather fragment
        scenario = launchFragmentInHiltContainer<WeatherFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^Weather data unit of type \"([^\"]*)\" is shown according to \"([^\"]*)\" preference$")
    fun weather_data_unit_of_type_is_shown_according_to_preference(unit: String, current: String) {
        when(unit){
            "temperature" -> {
                when (current) {
                    "metric" -> checkUiWeatherTemperatureShownAsMetric()
                    "imperial" -> checkUiWeatherTemperatureShownAsImperial()
                    else -> throw IllegalArgumentException("Unknown step arg: $current")
                }
            }

            "wind speed" -> {
                when (current) {
                    "metric" -> checkUiWeatherWindSpeedShownAsMetric()
                    "imperial" -> checkUiWeatherWindSpeedShownAsImperial()
                    else -> throw IllegalArgumentException("Unknown step arg: $current")
                }
            }

            "time format" -> {
                when (current) {
                    "24 hour" -> {
                        checkUiWeatherTimeShownIn24HourFormat()
                    }
                    "12 hour" -> checkUiWeatherTimeShownIn12HourFormat()
                    else -> throw IllegalArgumentException("Unknown step arg: $current")
                }
            }

            else -> throw IllegalArgumentException("Unknown step arg: $unit")
        }
    }

    @When("^Unit \"([^\"]*)\" preference is changed to \"([^\"]*)\"$")
    fun unit_preference_is_changed(unit: String, changed: String) {
        when(unit){
            "temperature" -> {
                when (changed) {
                    "metric" -> tempUnitProviderSubject.onNext(
                        TempUnit(
                            UnitSystem.METRIC
                        )
                    )
                    "imperial" -> tempUnitProviderSubject.onNext(
                        TempUnit(
                            UnitSystem.IMPERIAL
                        )
                    )
                    else -> throw IllegalArgumentException("Unknown step arg: $changed")
                }
            }

            "wind speed" -> {
                when (changed) {
                    "metric" -> windUnitProviderSubject.onNext(WindSpeedUnit(UnitSystem.METRIC))
                    "imperial" -> windUnitProviderSubject.onNext(
                        WindSpeedUnit(
                            UnitSystem.IMPERIAL
                        )
                    )
                    else -> throw IllegalArgumentException("Unknown step arg: $changed")
                }
            }

            "time format" -> {
                when (changed) {
                    "24 hour" -> timeFormatProviderSubject.onNext(
                        TimeFormat(
                            TimeFormat.HourFormat.HOUR_24)
                    )
                    "12 hour" -> timeFormatProviderSubject.onNext(
                        TimeFormat(
                            TimeFormat.HourFormat.HOUR_12)
                    )
                    else -> throw IllegalArgumentException("Unknown step arg: $changed")
                }
            }

            else -> throw IllegalArgumentException("Unknown step arg: $unit")
        }

        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^Shown weather data of type \"([^\"]*)\" should be updated to \"([^\"]*)\" type$")
    fun shown_weather_data_should_be_updated(unit: String, changed: String) {
        when(unit){
            "temperature" -> {
                when (changed) {
                    "metric" -> checkUiWeatherTemperatureShownAsMetric()
                    "imperial" -> checkUiWeatherTemperatureShownAsImperial()
                    else -> throw IllegalArgumentException("Unknown step arg: $changed")
                }
            }

            "wind speed" -> {
                when (changed) {
                    "metric" -> checkUiWeatherWindSpeedShownAsMetric()
                    "imperial" -> checkUiWeatherWindSpeedShownAsImperial()
                    else -> throw IllegalArgumentException("Unknown step arg: $changed")
                }
            }

            "time format" -> {
                when (changed) {
                    "24 hour" -> checkUiWeatherTimeShownIn24HourFormat()
                    "12 hour" -> checkUiWeatherTimeShownIn12HourFormat()
                    else -> throw IllegalArgumentException("Unknown step arg: $changed")
                }
            }

            else -> throw IllegalArgumentException("Unknown step arg: $unit")
        }
    }

    private fun checkUiWeatherTemperatureShownAsMetric() {
        val weatherJson = getJsonFromResource(dispatcher.locationWeatherRes)
        val currentTemp = JSONObject(weatherJson).getJSONObject("current").getDouble("temp")
        val feelTemp = JSONObject(weatherJson).getJSONObject("current").getDouble("feels_like")
        val minTemp = JSONObject(weatherJson).getJSONArray("daily")
            .getJSONObject(0).getJSONObject("temp").getDouble("min")
        val maxTemp = JSONObject(weatherJson).getJSONArray("daily")
            .getJSONObject(0).getJSONObject("temp").getDouble("max")
        val hourlyForecast = JSONObject(weatherJson).getJSONArray("hourly")
        val dailyForecast = JSONObject(weatherJson).getJSONArray("daily")

        // check current temp
        onView(withId(R.id.currentTemp))
            .check(matches(withText("${currentTemp.roundToInt()}°")))

        // check current temp
        onView(withId(R.id.currentTempUnit))
            .check(matches(withText("C")))

        // check feels temp
        onView(withId(R.id.feelTemp))
            .check(matches(withText("Feels like ${feelTemp.roundToInt()}°")))

        // check min max temp
        onView(withId(R.id.minMaxTemp))
            .check(matches(withText("min ${minTemp.roundToInt()}°/max ${maxTemp.roundToInt()}°")))

        // check hour forecast temp
        for (i in 0 until 12) {
            val hourTemp =  hourlyForecast.getJSONObject(i)
                .getDouble("temp")

            onView(withId(R.id.hourForecast))
                .perform(scrollToPosition<HourlyForecastViewHolder>(i))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.hourForecast).atPositionOnView(i, R.id.temp))
                .check(matches(withText("${hourTemp.roundToInt()}°")))
        }

        // check day forecast temp
        for (i in 0 until 8) {
            val dayMinTemp = dailyForecast.getJSONObject(i).getJSONObject("temp").getDouble("min")
            val dayMaxTemp = dailyForecast.getJSONObject(i).getJSONObject("temp").getDouble("max")

            onView(withRecyclerView(R.id.dailyForecast).atPositionOnView(i, R.id.minTemp))
                .check(matches(withText("${dayMinTemp.roundToInt()}°")))

            onView(withRecyclerView(R.id.dailyForecast).atPositionOnView(i, R.id.maxTemp))
                .check(matches(withText("${dayMaxTemp.roundToInt()}°")))
        }
    }

    private fun checkUiWeatherTemperatureShownAsImperial() {
        val weatherJson = getJsonFromResource(dispatcher.locationWeatherRes)
        val currentTemp = celsiusToFahrenheit(
            JSONObject(weatherJson).getJSONObject("current")
                .getDouble("temp")
        )
        val feelTemp = celsiusToFahrenheit(
            JSONObject(weatherJson).getJSONObject("current")
                .getDouble("feels_like")
        )
        val minTemp = celsiusToFahrenheit(
            JSONObject(weatherJson).getJSONArray("daily")
                .getJSONObject(0).getJSONObject("temp").getDouble("min")
        )
        val maxTemp = celsiusToFahrenheit(
            JSONObject(weatherJson).getJSONArray("daily")
                .getJSONObject(0).getJSONObject("temp").getDouble("max")
        )
        val hourlyForecast = JSONObject(weatherJson).getJSONArray("hourly")
        val dailyForecast = JSONObject(weatherJson).getJSONArray("daily")

        // check current temp
        onView(withId(R.id.currentTemp))
            .check(matches(withText("${currentTemp.roundToInt()}°")))

        // check current temp
        onView(withId(R.id.currentTempUnit))
            .check(matches(withText("F")))

        // check feels temp
        onView(withId(R.id.feelTemp))
            .check(matches(withText("Feels like ${feelTemp.roundToInt()}°")))

        // check min max temp
        onView(withId(R.id.minMaxTemp))
            .check(matches(withText("min ${minTemp.roundToInt()}°/max ${maxTemp.roundToInt()}°")))

        // check hour forecast temp
        for (i in 0 until 12) {
            val hourTemp = celsiusToFahrenheit(
                hourlyForecast.getJSONObject(i)
                    .getDouble("temp")
            )

            onView(withId(R.id.hourForecast))
                .perform(scrollToPosition<HourlyForecastViewHolder>(i))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.hourForecast).atPositionOnView(i, R.id.temp))
                .check(matches(withText("${hourTemp.roundToInt()}°")))
        }

        // check day forecast temp
        for (i in 0 until 8) {
            val dayMinTemp = celsiusToFahrenheit(
                dailyForecast.getJSONObject(i).getJSONObject("temp")
                    .getDouble("min")
            )
            val dayMaxTemp = celsiusToFahrenheit(
                dailyForecast.getJSONObject(i).getJSONObject("temp")
                    .getDouble("max")
            )

            onView(withRecyclerView(R.id.dailyForecast).atPositionOnView(i, R.id.minTemp))
                .check(matches(withText("${dayMinTemp.roundToInt()}°")))

            onView(withRecyclerView(R.id.dailyForecast).atPositionOnView(i, R.id.maxTemp))
                .check(matches(withText("${dayMaxTemp.roundToInt()}°")))
        }
    }

    private fun checkUiWeatherWindSpeedShownAsMetric() {
        val weatherJson = getJsonFromResource(dispatcher.locationWeatherRes)
        val windSpeed = JSONObject(weatherJson).getJSONObject("current").getDouble("wind_speed")

        // check wind speed
        onView(withId(R.id.windSpeedValue))
            .check(matches(withText("${windSpeed.roundToInt()}km/h")))
    }

    private fun checkUiWeatherWindSpeedShownAsImperial() {
        val weatherJson = getJsonFromResource(dispatcher.locationWeatherRes)
        val windSpeed = kphToMph(
            JSONObject(weatherJson).getJSONObject("current")
                .getDouble("wind_speed")
        )

        // check wind speed
        onView(withId(R.id.windSpeedValue))
            .check(matches(withText("${windSpeed.roundToInt()}mph")))
    }

    private fun checkUiWeatherTimeShownIn24HourFormat() {
        val weatherJson = getJsonFromResource(dispatcher.locationWeatherRes)
        val timeZone = JSONObject(weatherJson).getString("timezone")
        val sunrise = JSONObject(weatherJson).getJSONObject("current").getLong("sunrise")
        val sunset = JSONObject(weatherJson).getJSONObject("current").getLong("sunset")
        val formattedSunrise = SimpleDateFormat("HH:mm").format(
            LocalDateTime(
                sunrise * 1000,
                DateTimeZone.forID(timeZone)
            ).toDate()
        )
        val formattedSunset = SimpleDateFormat("HH:mm").format(
            LocalDateTime(
                sunset * 1000,
                DateTimeZone.forID(timeZone)
            ).toDate()
        )
        val hourlyForecast = JSONObject(weatherJson).getJSONArray("hourly")

        onView(withId(R.id.textClock))
            .check(matches(withTimeFormat12(null)))

        onView(withId(R.id.textClock))
            .check(matches(withTimeFormat24("E, dd MMM yyyy HH:mm")))

        onView(withId(R.id.sunriseValue))
            .check(matches(withText(formattedSunrise)))

        onView(withId(R.id.sunsetValue))
            .check(matches(withText(formattedSunset)))

        for (i in 0 until 12) {
            val hour = hourlyForecast.getJSONObject(i).getLong("dt")
            val formattedHour = SimpleDateFormat("HH:mm").format(
                LocalDateTime(
                    hour * 1000,
                    DateTimeZone.forID(timeZone)
                ).toDate()
            )

            onView(withId(R.id.hourForecast))
                .perform(scrollToPosition<HourlyForecastViewHolder>(i))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.hourForecast).atPositionOnView(i, R.id.hour))
                .check(matches(withText(formattedHour)))
        }
    }

    private fun checkUiWeatherTimeShownIn12HourFormat() {
        val weatherJson = getJsonFromResource(dispatcher.locationWeatherRes)
        val timeZone = JSONObject(weatherJson).getString("timezone")
        val sunrise = JSONObject(weatherJson).getJSONObject("current").getLong("sunrise")
        val sunset = JSONObject(weatherJson).getJSONObject("current").getLong("sunset")
        val formattedSunrise = SimpleDateFormat("hh:mm aa").format(
            LocalDateTime(
                sunrise * 1000,
                DateTimeZone.forID(timeZone)
            ).toDate()
        )
        val formattedSunset = SimpleDateFormat("hh:mm aa").format(
            LocalDateTime(
                sunset * 1000,
                DateTimeZone.forID(timeZone)
            ).toDate()
        )
        val hourlyForecast = JSONObject(weatherJson).getJSONArray("hourly")

        onView(withId(R.id.textClock))
            .check(matches(withTimeFormat12("E, dd MMM yyyy hh:mm aa")))

        onView(withId(R.id.textClock))
            .check(matches(withTimeFormat24(null)))

        onView(withId(R.id.sunriseValue))
            .check(matches(withText(formattedSunrise)))

        onView(withId(R.id.sunsetValue))
            .check(matches(withText(formattedSunset)))

        for (i in 0 until 12) {
            val hour = hourlyForecast.getJSONObject(i).getLong("dt")
            val formattedHour = SimpleDateFormat("hh:mm aa").format(
                LocalDateTime(
                    hour * 1000,
                    DateTimeZone.forID(timeZone)
                ).toDate()
            )

            onView(withId(R.id.hourForecast))
                .perform(scrollToPosition<HourlyForecastViewHolder>(i))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.hourForecast).atPositionOnView(i, R.id.hour))
                .check(matches(withText(formattedHour)))
        }
    }

    private fun celsiusToFahrenheit(celsius: Double): Double {
        return ((celsius.roundToInt() * (9.0 / 5.0)) + 32)
    }

    private fun kphToMph(kph: Double): Double {
        return (kph / 1.6)
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