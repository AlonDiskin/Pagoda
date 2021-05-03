package com.diskin.alon.pagoda.weatherinfo.featuretesting.weatherunitchange

import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.pagoda.common.appservices.toResult
import com.diskin.alon.pagoda.common.eventcontracts.AppEventProvider
import com.diskin.alon.pagoda.common.eventcontracts.settings.TemperatureUnitPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.UnitPrefSystem
import com.diskin.alon.pagoda.common.eventcontracts.settings.WindSpeedUnitPref
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.common.uitesting.withTimeFormat12
import com.diskin.alon.pagoda.common.uitesting.withTimeFormat24
import com.diskin.alon.pagoda.weatherinfo.appservices.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.data.BuildConfig
import com.diskin.alon.pagoda.weatherinfo.featuretesting.getJsonFromResource
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.HourlyForecastAdapter.HourlyForecastViewHolder
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.WeatherFragment
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.reactivex.Observable
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

/**
 * Step definitions for 'Weather units preference changed' scenario.
 */
class UnitsPrefChangedSteps(
    private val locationProvider: UserLocationProvider,
    server: MockWebServer,
    private val tempUnitPrefProvider: AppEventProvider<TemperatureUnitPref>,
    private val windSpeedUnitPrefProvider: AppEventProvider<WindSpeedUnitPref>,
    private val timeFormatPrefProvider: AppEventProvider<TimeFormatPref>
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val dispatcher = TestDispatcher()
    private lateinit var tempUnitPrefProviderSubject: BehaviorSubject<TemperatureUnitPref>
    private lateinit var windUnitPrefProviderSubject: BehaviorSubject<WindSpeedUnitPref>
    private lateinit var timeFormatPrefProviderSubject: BehaviorSubject<TimeFormatPref>

    init {
        // Prepare mock server for test scenario
        server.setDispatcher(dispatcher)

        // Prepare location provider for test scenario
        val location = UserLocation(dispatcher.locationLat, dispatcher.locationLon)
        every { locationProvider.getCurrentLocation() } returns Observable.just(location).toResult()
    }

    @Given("^Unit \"([^\"]*)\" preference is set as \"([^\"]*)\"$")
    fun unit_preference_is_set_as(unit: String, current: String) {
        // Prepare app prefs according to test arguments
        when(unit){
            "temperature" -> {
                windUnitPrefProviderSubject = BehaviorSubject.createDefault(
                    WindSpeedUnitPref(
                        UnitPrefSystem.METRIC
                    )
                )
                timeFormatPrefProviderSubject = BehaviorSubject.createDefault(
                    TimeFormatPref(
                        TimeFormatPref.HourFormat.HOUR_24
                    )
                )
                tempUnitPrefProviderSubject = when (current) {
                    "metric" -> BehaviorSubject.createDefault(TemperatureUnitPref(UnitPrefSystem.METRIC))
                    "imperial" -> BehaviorSubject.createDefault(TemperatureUnitPref(UnitPrefSystem.IMPERIAL))
                    else -> throw IllegalArgumentException("Unknown step arg: $current")
                }
            }

            "wind speed" -> {
                timeFormatPrefProviderSubject = BehaviorSubject.createDefault(
                    TimeFormatPref(
                        TimeFormatPref.HourFormat.HOUR_24
                    )
                )
                tempUnitPrefProviderSubject = BehaviorSubject.createDefault(
                    TemperatureUnitPref(
                        UnitPrefSystem.METRIC
                    )
                )
                windUnitPrefProviderSubject = when (current) {
                    "metric" -> BehaviorSubject.createDefault(WindSpeedUnitPref(UnitPrefSystem.METRIC))
                    "imperial" -> BehaviorSubject.createDefault(WindSpeedUnitPref(UnitPrefSystem.IMPERIAL))
                    else -> throw IllegalArgumentException("Unknown step arg: $current")
                }
            }

            "time format" -> {
                tempUnitPrefProviderSubject = BehaviorSubject.createDefault(
                    TemperatureUnitPref(
                        UnitPrefSystem.METRIC
                    )
                )
                windUnitPrefProviderSubject = BehaviorSubject.createDefault(
                    WindSpeedUnitPref(
                        UnitPrefSystem.METRIC
                    )
                )
                timeFormatPrefProviderSubject = when (current) {
                    "24 hour" -> BehaviorSubject.createDefault(TimeFormatPref(TimeFormatPref.HourFormat.HOUR_24))
                    "12 hour" -> BehaviorSubject.createDefault(TimeFormatPref(TimeFormatPref.HourFormat.HOUR_12))
                    else -> throw IllegalArgumentException("Unknown step arg: $current")
                }
            }

            else -> throw IllegalArgumentException("Unknown step arg: $unit")
        }

        every { tempUnitPrefProvider.get() } returns tempUnitPrefProviderSubject
        every { windSpeedUnitPrefProvider.get() } returns windUnitPrefProviderSubject
        every { timeFormatPrefProvider.get() } returns timeFormatPrefProviderSubject
    }

    @And("^User open weather screen$")
    fun user_open_weather_screen() {
        // Launch weather fragment
        scenario = launchFragmentInHiltContainer<WeatherFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(1000)
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
                    "metric" -> tempUnitPrefProviderSubject.onNext(
                        TemperatureUnitPref(
                            UnitPrefSystem.METRIC
                        )
                    )
                    "imperial" -> tempUnitPrefProviderSubject.onNext(
                        TemperatureUnitPref(
                            UnitPrefSystem.IMPERIAL
                        )
                    )
                    else -> throw IllegalArgumentException("Unknown step arg: $changed")
                }
            }

            "wind speed" -> {
                when (changed) {
                    "metric" -> windUnitPrefProviderSubject.onNext(WindSpeedUnitPref(UnitPrefSystem.METRIC))
                    "imperial" -> windUnitPrefProviderSubject.onNext(
                        WindSpeedUnitPref(
                            UnitPrefSystem.IMPERIAL
                        )
                    )
                    else -> throw IllegalArgumentException("Unknown step arg: $changed")
                }
            }

            "time format" -> {
                when (changed) {
                    "24 hour" -> timeFormatPrefProviderSubject.onNext(TimeFormatPref(TimeFormatPref.HourFormat.HOUR_24))
                    "12 hour" -> timeFormatPrefProviderSubject.onNext(TimeFormatPref(TimeFormatPref.HourFormat.HOUR_12))
                    else -> throw IllegalArgumentException("Unknown step arg: $changed")
                }
            }

            else -> throw IllegalArgumentException("Unknown step arg: $unit")
        }

        Shadows.shadowOf(Looper.getMainLooper()).idle()
        Thread.sleep(1000)
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
            .check(matches(withText("${currentTemp.toInt()}°")))

        // check feels temp
        onView(withId(R.id.feelTemp))
            .check(matches(withText("Feels like ${feelTemp.toInt()}°")))

        // check min max temp
        onView(withId(R.id.minMaxTemp))
            .check(matches(withText("min ${minTemp.toInt()}°/max ${maxTemp.toInt()}°")))

        // check hour forecast temp
        for (i in 0 until 12) {
            val hourTemp =  hourlyForecast.getJSONObject(i)
                .getDouble("temp")

            onView(withId(R.id.hourForecast))
                .perform(scrollToPosition<HourlyForecastViewHolder>(i))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.hourForecast).atPositionOnView(i, R.id.temp))
                .check(matches(withText("${hourTemp.toInt()}°")))
        }

        // check day forecast temp
        for (i in 0 until 8) {
            val dayMinTemp = dailyForecast.getJSONObject(i).getJSONObject("temp").getDouble("min")
            val dayMaxTemp = dailyForecast.getJSONObject(i).getJSONObject("temp").getDouble("max")

            onView(withRecyclerView(R.id.dailyForecast).atPositionOnView(i, R.id.minTemp))
                .check(matches(withText("${dayMinTemp.toInt()}°")))

            onView(withRecyclerView(R.id.dailyForecast).atPositionOnView(i, R.id.maxTemp))
                .check(matches(withText("${dayMaxTemp.toInt()}°")))
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
            .check(matches(withText("${currentTemp.toInt()}°")))

        // check feels temp
        onView(withId(R.id.feelTemp))
            .check(matches(withText("Feels like ${feelTemp.toInt()}°")))

        // check min max temp
        onView(withId(R.id.minMaxTemp))
            .check(matches(withText("min ${minTemp.toInt()}°/max ${maxTemp.toInt()}°")))

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
                .check(matches(withText("${hourTemp.toInt()}°")))
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
                .check(matches(withText("${dayMinTemp.toInt()}°")))

            onView(withRecyclerView(R.id.dailyForecast).atPositionOnView(i, R.id.maxTemp))
                .check(matches(withText("${dayMaxTemp.toInt()}°")))
        }
    }

    private fun checkUiWeatherWindSpeedShownAsMetric() {
        val weatherJson = getJsonFromResource(dispatcher.locationWeatherRes)
        val windSpeed = JSONObject(weatherJson).getJSONObject("current").getDouble("wind_speed")

        // check wind speed
        onView(withId(R.id.windSpeedValue))
            .check(matches(withText("${windSpeed.toInt()}km/h")))
    }

    private fun checkUiWeatherWindSpeedShownAsImperial() {
        val weatherJson = getJsonFromResource(dispatcher.locationWeatherRes)
        val windSpeed = kphToMph(
            JSONObject(weatherJson).getJSONObject("current")
                .getDouble("wind_speed")
        )

        // check wind speed
        onView(withId(R.id.windSpeedValue))
            .check(matches(withText("${windSpeed.toInt()}mph")))
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
        return String.format("%.1f", ((celsius * (9.0 / 5.0)) + 32)).toDouble()
    }

    private fun kphToMph(kph: Double): Double {
        return String.format("%.1f", (kph / 1.6)).toDouble()
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