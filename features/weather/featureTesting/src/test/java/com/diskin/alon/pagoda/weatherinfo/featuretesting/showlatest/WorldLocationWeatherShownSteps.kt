package com.diskin.alon.pagoda.weatherinfo.featuretesting.showlatest

import android.os.Looper
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.pagoda.common.eventcontracts.AppEventProvider
import com.diskin.alon.pagoda.common.eventcontracts.settings.TemperatureUnitPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.UnitPrefSystem
import com.diskin.alon.pagoda.common.eventcontracts.settings.WindSpeedUnitPref
import com.diskin.alon.pagoda.common.presentation.ImageLoader
import com.diskin.alon.pagoda.common.presentation.LOCATION_LAT
import com.diskin.alon.pagoda.common.presentation.LOCATION_LON
import com.diskin.alon.pagoda.common.uitesting.*
import com.diskin.alon.pagoda.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.pagoda.weatherinfo.data.BuildConfig
import com.diskin.alon.pagoda.common.featuretesting.getJsonFromResource
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.HourlyForecastAdapter.HourlyForecastViewHolder
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.WeatherFragment
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.verify
import io.reactivex.Observable
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.json.JSONArray
import org.json.JSONObject
import org.robolectric.Shadows
import java.text.SimpleDateFormat

/**
 * Step definitions runner for 'Latest weather shown for world location' scenario.
 */
class WorldLocationWeatherShownSteps(
    server: MockWebServer,
    tempUnitPrefProvider: AppEventProvider<TemperatureUnitPref>,
    windSpeedUnitPrefProvider: AppEventProvider<WindSpeedUnitPref>,
    timeFormatPrefProvider: AppEventProvider<TimeFormatPref>
) : GreenCoffeeSteps() {
    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val dispatcher = TestDispatcher()

    init {
        // Prepare mock server for test scenario
        server.setDispatcher(dispatcher)

        // Prepare app prefs providers for scenario
        every { tempUnitPrefProvider.get() } returns Observable.just(TemperatureUnitPref(UnitPrefSystem.METRIC))
        every { windSpeedUnitPrefProvider.get() } returns Observable.just(WindSpeedUnitPref(UnitPrefSystem.METRIC))
        every { timeFormatPrefProvider.get() } returns Observable.just(TimeFormatPref(TimeFormatPref.HourFormat.HOUR_24))

        // Prepare image loader for test
        mockkObject(ImageLoader)
        every { ImageLoader.loadIconResIntoImageView(any(),any()) } returns Unit
    }

    @Given("^Weather screen was opened to show world location weather$")
    fun weather_screen_was_opened_to_show_world_location_weather() {
        val bundle = bundleOf(
            LOCATION_LAT to dispatcher.locationLat,
            LOCATION_LON to dispatcher.locationLon
        )
        scenario = launchFragmentInHiltContainer<WeatherFragment>(fragmentArgs = bundle)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^Weather detail of user current location is shown$")
    fun weather_detail_of_user_current_location_is_shown() {
        val weather = getExpectedUiWeather()

        // Verify location name
        onView(withId(R.id.location_name))
            .check(matches(withText(weather.name)))

        // Verify location weather clock data
        onView(withId(R.id.textClock))
            .check(matches(withTimeZone(weather.timeZone)))

        onView(withId(R.id.textClock))
            .check(matches(withTimeFormat12(null)))

        onView(withId(R.id.textClock))
            .check(matches(withTimeFormat24(weather.clockFormat24Hour)))

        // Verify current temp data
        onView(withId(R.id.currentTemp))
            .check(matches(withText(weather.currentTemp)))

        // Verify main weather icon loaded
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.fragments[0]
            val imageView = fragment.view!!.findViewById<ImageView>(R.id.mainWeatherIcon)
            verifyConditionIconLoaded(imageView,weather.description,weather.isDay)
        }

        // Verify feel temp data
        onView(withId(R.id.feelTemp))
            .check(matches(withText(weather.feelTemp)))

        // Verify weather description
        onView(withId(R.id.description))
            .check(matches(withText(weather.description)))

        // Verify min and max data
        onView(withId(R.id.minMaxTemp))
            .check(matches(withText(weather.minMaxTemp)))

        // Verify index value
        onView(withId(R.id.uvValue))
            .check(matches(withText(weather.uv)))

        // Verify humidity
        onView(withId(R.id.humidityValue))
            .check(matches(withText(weather.humidity)))

        // Verify wind speed
        onView(withId(R.id.windSpeedValue))
            .check(matches(withText(weather.windSpeed)))

        // Verify sunrise and sunset data
        onView(withId(R.id.sunriseValue))
            .check(matches(withText(weather.sunrise)))

        onView(withId(R.id.sunsetValue))
            .check(matches(withText(weather.sunset)))

        // Verify hourly forecast
        onView(withId(R.id.hourForecast))
            .check(matches(isRecyclerViewItemsCount(12)))

        weather.hourlyForecast.forEachIndexed { index, hourForecast ->
            onView(withId(R.id.hourForecast))
                .perform(
                    scrollToPosition<HourlyForecastViewHolder>(
                        index
                    )
                )
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(
                withRecyclerView(R.id.hourForecast)
                    .atPositionOnView(index, R.id.temp)
            )
                .check(matches(withText(hourForecast.temp)))

            onView(
                withRecyclerView(R.id.hourForecast)
                    .atPositionOnView(index, R.id.hour)
            )
                .check(matches(withText(hourForecast.hour)))

            // Verify hour forecast icon loaded
            scenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.fragments[0]
                val rv = fragment.view!!.findViewById<RecyclerView>(R.id.hourForecast)
                val imageView = rv.findViewHolderForAdapterPosition(index)!!.itemView
                    .findViewById<ImageView>(R.id.hourWeatherIcon)

                verifyConditionIconLoaded(imageView,hourForecast.condition,hourForecast.isDay)
            }
        }

        // Verify daily forecast
        onView(withId(R.id.dailyForecast))
            .check(matches(isRecyclerViewItemsCount(8)))

        weather.dailyForecast.forEachIndexed { index, dayForecast ->
            onView(
                withRecyclerView(R.id.dailyForecast)
                    .atPositionOnView(index, R.id.day)
            )
                .check(matches(withText(dayForecast.day)))

            onView(
                withRecyclerView(R.id.dailyForecast)
                    .atPositionOnView(index, R.id.minTemp)
            )
                .check(matches(withText(dayForecast.minTemp)))

            onView(
                withRecyclerView(R.id.dailyForecast)
                    .atPositionOnView(index, R.id.maxTemp)
            )
                .check(matches(withText(dayForecast.maxTemp)))

            // Verify day forecast icon loaded
            scenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.fragments[0]
                val rv = fragment.view!!.findViewById<RecyclerView>(R.id.dailyForecast)
                val imageView = rv.findViewHolderForAdapterPosition(index)!!.itemView
                    .findViewById<ImageView>(R.id.dailyWeatherIcon)

                verifyConditionIconLoaded(imageView,dayForecast.condition,true)
            }
        }
    }

    private fun getExpectedUiWeather(): UiWeatherData {
        val weatherJson = getJsonFromResource(dispatcher.locationWeatherRes)
        val locationJson = getJsonFromResource(dispatcher.locationGeoRes
        )
        val name = JSONArray(locationJson).getJSONObject(0).getString("name")
        val timeZone = JSONObject(weatherJson).getString("timezone")
        val currentTemp = JSONObject(weatherJson).getJSONObject("current")
            .getDouble("temp")
        val feelTemp = JSONObject(weatherJson).getJSONObject("current")
            .getDouble("feels_like")
        val minTemp = JSONObject(weatherJson).getJSONArray("daily")
            .getJSONObject(0).getJSONObject("temp").getDouble("min")
        val maxTemp = JSONObject(weatherJson).getJSONArray("daily")
            .getJSONObject(0).getJSONObject("temp").getDouble("max")
        val conditionCode = JSONObject(weatherJson).getJSONObject("current").getJSONArray("weather")
            .getJSONObject(0).getInt("id")
        val isDayCondition = JSONObject(weatherJson).getJSONObject("current").getJSONArray("weather")
            .getJSONObject(0).getString("icon").contains('d')
        val humidity = JSONObject(weatherJson).getJSONObject("current").getDouble("humidity")
        val windSpeed = JSONObject(weatherJson).getJSONObject("current").getDouble("wind_speed")
        val sunrise = JSONObject(weatherJson).getJSONObject("current").getLong("sunrise")
        val sunset = JSONObject(weatherJson).getJSONObject("current").getLong("sunset")
        val uvIndexValue = JSONObject(weatherJson).getJSONObject("current")
            .getDouble("uvi")
        val uv = when {
            (uvIndexValue < 3) -> "Low"
            (uvIndexValue >= 3 && uvIndexValue < 6) -> "Moderate"
            (uvIndexValue >= 6 && uvIndexValue < 8) -> "High"
            else -> "Very high"
        }
        val hourlyUiForecast = mutableListOf<UiHourForecast>()
        val hourlyForecast = JSONObject(weatherJson).getJSONArray("hourly")

        for (i in 0 until 12) {
            val hour = hourlyForecast.getJSONObject(i).getLong("dt")
            val hourTemp =  hourlyForecast.getJSONObject(i)
                .getDouble("temp")
            val hourConditionCode = hourlyForecast.getJSONObject(i).getJSONArray("weather")
                .getJSONObject(0).getInt("id")
            val isHourDay = hourlyForecast.getJSONObject(i).getJSONArray("weather")
                .getJSONObject(0).getString("icon").contains('d')

            hourlyUiForecast.add(
                UiHourForecast(
                    SimpleDateFormat("HH:mm").format(
                        LocalDateTime(
                            hour * 1000,
                            DateTimeZone.forID(timeZone)
                        ).toDate()
                    ),
                    hourTemp.toInt().toString().plus("°"),
                    when {
                        (hourConditionCode in 200..299) -> "Thunderstorm"
                        (hourConditionCode in 300..399) -> "Drizzle"
                        (hourConditionCode in 500..599) -> "Rain"
                        (hourConditionCode in 600..699) -> "Snow"
                        (hourConditionCode == 800) -> "Clear"
                        (hourConditionCode in 801..804) -> "Clouds"
                        (hourConditionCode == 701) -> "Mist"
                        (hourConditionCode == 721) -> "Haze"
                        (hourConditionCode == 731) -> "Dust"
                        (hourConditionCode == 741) -> "Fog"
                        (hourConditionCode == 751) -> "Sand"
                        (hourConditionCode == 781) -> "Tornado"
                        else -> "Unknown"
                    },
                    isHourDay
                )
            )
        }

        val dailyUiForecast = mutableListOf<UiDayForecast>()
        val dailyForecast = JSONObject(weatherJson).getJSONArray("daily")

        for (i in 0 until 8) {
            val day = dailyForecast.getJSONObject(i).getLong("dt")
            val dayMinTemp = dailyForecast.getJSONObject(i).getJSONObject("temp").getDouble("min")
            val dayMaxTemp = dailyForecast.getJSONObject(i).getJSONObject("temp").getDouble("max")
            val dayConditionCode = dailyForecast.getJSONObject(i).getJSONArray("weather")
                .getJSONObject(0).getInt("id")

            dailyUiForecast.add(
                UiDayForecast(
                    when(LocalDateTime(day * 1000, DateTimeZone.forID(timeZone)).dayOfWeek) {
                        1 -> "Mon"
                        2 -> "Tue"
                        3 -> "Wed"
                        4 -> "Thu"
                        5 -> "Fri"
                        6 -> "Sat"
                        7 -> "Sun"
                        else -> throw IllegalArgumentException("Wrong day of week arg:$day")
                    },
                    dayMinTemp.toInt().toString().plus("°"),
                    dayMaxTemp.toInt().toString().plus("°"),
                    when {
                        (dayConditionCode in 200..299) -> "Thunderstorm"
                        (dayConditionCode in 300..399) -> "Drizzle"
                        (dayConditionCode in 500..599) -> "Rain"
                        (dayConditionCode in 600..699) -> "Snow"
                        (dayConditionCode == 800) -> "Clear"
                        (dayConditionCode in 801..804) -> "Clouds"
                        (dayConditionCode == 701) -> "Mist"
                        (dayConditionCode == 721) -> "Haze"
                        (dayConditionCode == 731) -> "Dust"
                        (dayConditionCode == 741) -> "Fog"
                        (dayConditionCode == 751) -> "Sand"
                        (dayConditionCode == 781) -> "Tornado"
                        else -> "Unknown"
                    }
                )
            )
        }

        return UiWeatherData(
            name,
            timeZone,
            "E, dd MMM yyyy HH:mm",
            currentTemp.toInt().toString().plus("°"),
            "Feels like ${feelTemp.toInt()}°",
            "min ${minTemp.toInt()}°/max ${maxTemp.toInt()}°",
            when {
                (conditionCode in 200..299) -> "Thunderstorm"
                (conditionCode in 300..399) -> "Drizzle"
                (conditionCode in 500..599) -> "Rain"
                (conditionCode in 600..699) -> "Snow"
                (conditionCode == 800) -> "Clear"
                (conditionCode in 801..804) -> "Clouds"
                (conditionCode == 701) -> "Mist"
                (conditionCode == 721) -> "Haze"
                (conditionCode == 731) -> "Dust"
                (conditionCode == 741) -> "Fog"
                (conditionCode == 751) -> "Sand"
                (conditionCode == 781) -> "Tornado"
                else -> "Unknown"
            },
            isDayCondition,
            humidity.toInt().toString().plus("%"),
            windSpeed.toInt().toString().plus("km/h"),
            SimpleDateFormat("HH:mm").format(
                LocalDateTime(
                    sunrise * 1000,
                    DateTimeZone.forID(timeZone)
                ).toDate()
            ),
            SimpleDateFormat("HH:mm").format(
                LocalDateTime(
                    sunset * 1000,
                    DateTimeZone.forID(timeZone)
                ).toDate()
            ),
            uv,
            hourlyUiForecast,
            dailyUiForecast
        )
    }

    private fun verifyConditionIconLoaded(imageView: ImageView, condition: String, isDay: Boolean) {
        when(condition) {
            "Thunderstorm" -> {
                when(isDay) {
                    true -> verify { ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_thunder_day_96) }
                    else -> verify { ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_thunder_night_96) }
                }
            }

            "Drizzle","Rain" -> {
                when(isDay) {
                    true -> verify { ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_rain_day_96) }
                    else -> verify { ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_rain_night_96) }
                }
            }

            "Snow" -> {
                when(isDay) {
                    true -> verify { ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_snow_day_96) }
                    else -> verify { ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_snow_night_96) }
                }
            }

            "Mist", "Fog" -> {
                when(isDay) {
                    true -> verify { ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_fog_day_96) }
                    else -> verify { ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_fog_night_96) }
                }
            }

            "Clear" -> {
                when(isDay) {
                    true -> verify { ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_clear_day_96) }
                    else -> verify { ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_clear_night_96) }
                }
            }

            "Clouds" -> {
                when(isDay) {
                    true -> verify { ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_clouds_day_96) }
                    else -> verify { ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_clouds_night_96) }
                }
            }

            "Haze", "Dust", "Sand" -> {
                when(isDay) {
                    true -> verify { ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_haze_day_96) }
                    else -> verify { ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_haze_night_96) }
                }
            }

            "Tornado" -> {
                verify { ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_tornado_96) }
            }

            "Unknown" -> {
                verify { ImageLoader.loadIconResIntoImageView(imageView, R.drawable.ic_weather_unknown_96) }
            }
        }
    }

    private data class UiWeatherData(
        val name: String,
        val timeZone: String,
        val clockFormat24Hour: String,
        val currentTemp: String,
        val feelTemp: String,
        val minMaxTemp: String,
        val description: String,
        val isDay: Boolean,
        val humidity: String,
        val windSpeed: String,
        val sunrise: String,
        val sunset: String,
        val uv: String,
        val hourlyForecast: List<UiHourForecast>,
        val dailyForecast: List<UiDayForecast>
    )

    private data class UiHourForecast(val hour: String, val temp: String,val condition: String,val isDay: Boolean)

    private data class UiDayForecast(val day: String, val minTemp: String, val maxTemp: String,val condition: String)

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