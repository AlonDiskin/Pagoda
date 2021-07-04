package com.diskin.alon.pagoda.weatherinfo.featuretesting.cacheuserlocationweather

import android.os.Looper
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.eventcontracts.AppEventProvider
import com.diskin.alon.pagoda.common.eventcontracts.settings.TemperatureUnitPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.TimeFormatPref
import com.diskin.alon.pagoda.common.eventcontracts.settings.UnitPrefSystem
import com.diskin.alon.pagoda.common.eventcontracts.settings.WindSpeedUnitPref
import com.diskin.alon.pagoda.common.presentation.ImageLoader
import com.diskin.alon.pagoda.common.uitesting.HiltTestActivity
import com.diskin.alon.pagoda.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.pagoda.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.pagoda.common.uitesting.launchFragmentInHiltContainer
import com.diskin.alon.pagoda.common.uitesting.withTimeZone
import com.diskin.alon.pagoda.weatherinfo.data.BuildConfig
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.data.local.model.CurrentWeatherEntity
import com.diskin.alon.pagoda.weatherinfo.data.local.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.domain.*
import com.diskin.alon.pagoda.weatherinfo.featuretesting.TestDatabase
import com.diskin.alon.pagoda.common.featuretesting.getJsonFromResource
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.HourlyForecastAdapter.HourlyForecastViewHolder
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.WeatherFragment
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.verify
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
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
import java.util.*

/**
 * Step definitions for 'Latest cached data shown before updated' scenario.
 */
class CacheShownFirstSteps(
    server: MockWebServer,
    private val db: TestDatabase,
    private val locationProvider: UserLocationProvider,
    private val tempUnitPrefProvider: AppEventProvider<TemperatureUnitPref>,
    private val windSpeedUnitPrefProvider: AppEventProvider<WindSpeedUnitPref>,
    private val timeFormatPrefProvider: AppEventProvider<TimeFormatPref>
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>
    private val dispatcher = TestDispatcher()

    // Stub data
    private val locationSubject = BehaviorSubject.create<AppResult<UserLocation>>()

    init {
        // Set test server
        server.setDispatcher(dispatcher)

        // Stub mock location provider
        every { locationProvider.getLocation() } returns locationSubject

        // Stub mock app prefs providers
        every { tempUnitPrefProvider.get() } returns Observable.just(TemperatureUnitPref(
            UnitPrefSystem.METRIC))
        every { windSpeedUnitPrefProvider.get() } returns Observable.just(WindSpeedUnitPref(
            UnitPrefSystem.METRIC))
        every { timeFormatPrefProvider.get() } returns Observable.just(TimeFormatPref(TimeFormatPref.HourFormat.HOUR_24))

        // Stub static image loader
        mockkObject(ImageLoader)
        every { ImageLoader.loadIconResIntoImageView(any(),any()) } returns Unit
    }

    @Given("^App has cached weather for current user location$")
    fun app_has_cached_weather_for_current_user_location() {
        // Set test db
        db.currentWeatherDao().insert(createCachedWeather()).blockingAwait()
    }

    @When("^User open current weather screen$")
    fun user_open_current_weather_screen() {
        // Launch weather fragment
        scenario = launchFragmentInHiltContainer<WeatherFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Then("^App should show cached weather data with update timestamp$")
    fun app_should_show_cached_weather_data_with_update_timestamp() {
        // Verify cached weather data shown in ui
        val query = SimpleSQLiteQuery("SELECT * FROM current_weather WHERE id = 1")
        val cursor = db.query(query).also { it.moveToFirst() }
        val cachedName = cursor.getString(cursor.getColumnIndex("name"))
        val cachedCurrentTemp = cursor.getDouble(cursor.getColumnIndex("currentTemp"))
        val cachedFeelTemp = cursor.getDouble(cursor.getColumnIndex("feelTemp"))
        val cachedTimeZone = cursor.getString(cursor.getColumnIndex("timeZone"))
        val cachedWeatherDescription = JSONObject(
            cursor.getString(cursor.getColumnIndex("condition"))
        ).getString("description")
        val cachedDescriptionIsDay =  JSONObject(
            cursor.getString(cursor.getColumnIndex("condition"))
        ).getString("isDay").toBoolean()
        val cachedMinTemp = cursor.getDouble(cursor.getColumnIndex("minTemp"))
        val cachedMaxTemp = cursor.getDouble(cursor.getColumnIndex("maxTemp"))
        val cachedUvIndex = cursor.getDouble(cursor.getColumnIndex("uvIndexValue"))
        val cachedHumidity = cursor.getDouble(cursor.getColumnIndex("humidity"))
        val cachedWindSpeed = cursor.getDouble(cursor.getColumnIndex("windSpeed"))
        val cachedSunrise = cursor.getLong(cursor.getColumnIndex("sunrise"))
        val cachedSunset = cursor.getLong(cursor.getColumnIndex("sunset"))
        val cachedUpdateStamp = cursor.getLong(cursor.getColumnIndex("updated"))
        val cachedHourlyForecast = JSONArray(
            cursor.getString(cursor.getColumnIndex("hourlyForecast"))
        )
        val cachedDailyForecast = JSONArray(
            cursor.getString(cursor.getColumnIndex("dailyForecast"))
        )

        // Verify location name
        onView(withId(R.id.location_name))
            .check(matches(withText(cachedName)))

        // Verify feel temp data
        onView(withId(R.id.feelTemp))
            .check(matches(withText("Feels like ${cachedFeelTemp.toInt()}°")))

        // Verify current temp data
        onView(withId(R.id.currentTemp))
            .check(matches(withText("${cachedCurrentTemp.toInt()}°")))

        // Verify clock data
        onView(withId(R.id.textClock))
            .check(matches(withTimeZone(cachedTimeZone)))

        // Verify main weather icon loaded
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.fragments[0]
            val imageView = fragment.view!!.findViewById<ImageView>(R.id.mainWeatherIcon)
            verifyConditionIconLoaded(imageView,cachedWeatherDescription,cachedDescriptionIsDay)
        }

        // Verify min and max data
        onView(withId(R.id.minMaxTemp))
            .check(matches(withText("min ${cachedMinTemp.toInt()}°/max ${cachedMaxTemp.toInt()}°")))

        // Verify index value
        onView(withId(R.id.uvValue))
            .check(matches(withText(
                when {
                    (cachedUvIndex < 3) -> "Low"
                    (cachedUvIndex >= 3 && cachedUvIndex < 6) -> "Moderate"
                    (cachedUvIndex >= 6 && cachedUvIndex < 8) -> "High"
                    else -> "Very high"
                }
            )))

        // Verify humidity
        onView(withId(R.id.humidityValue))
            .check(matches(withText("${cachedHumidity.toInt()}%")))

        // Verify wind speed
        onView(withId(R.id.windSpeedValue))
            .check(matches(withText("${cachedWindSpeed.toInt()}km/h")))

        // Verify sunrise and sunset data
        onView(withId(R.id.sunriseValue))
            .check(matches(withText(
                SimpleDateFormat("HH:mm").format(
                    LocalDateTime(cachedSunrise).toDate()
                )
            )))

        onView(withId(R.id.sunsetValue))
            .check(matches(withText(
                SimpleDateFormat("HH:mm").format(
                    LocalDateTime(cachedSunset).toDate()
                )
            )))

        // Verify last update time
        onView(withId(R.id.updated))
            .check(matches(withText("Last updated: ".plus(
                SimpleDateFormat("dd MMM, HH:mm")
                    .format(Date(cachedUpdateStamp))
            ))))

        // Verify hourly forecast
        onView(withId(R.id.hourForecast))
            .check(matches(isRecyclerViewItemsCount(12)))

        for (i in 0 until 12) {
            val hour = cachedHourlyForecast.getJSONObject(i).getLong("hour")
            val temp = cachedHourlyForecast.getJSONObject(i).getDouble("temp")
            val condition = cachedHourlyForecast.getJSONObject(i).getJSONObject("condition")
                .getString("description")
            val isDay = cachedHourlyForecast.getJSONObject(i).getJSONObject("condition")
                .getString("isDay").toBoolean()

            onView(withId(R.id.hourForecast))
                .perform(scrollToPosition<HourlyForecastViewHolder>(i))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.hourForecast).atPositionOnView(i,R.id.hour))
                .check(matches(withText(
                    SimpleDateFormat("HH:mm").format(
                        LocalDateTime(hour).toDate()
                ))))

            onView(withRecyclerView(R.id.hourForecast).atPositionOnView(i,R.id.temp))
                .check(matches(withText("${temp.toInt()}°")))

            // Verify hour forecast icon loaded
            scenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.fragments[0]
                val rv = fragment.view!!.findViewById<RecyclerView>(R.id.hourForecast)
                val imageView = rv.findViewHolderForAdapterPosition(i)!!.itemView
                    .findViewById<ImageView>(R.id.hourWeatherIcon)

                verifyConditionIconLoaded(imageView,condition,isDay)
            }
        }

        // Verify daily forecast
        onView(withId(R.id.dailyForecast))
            .check(matches(isRecyclerViewItemsCount(8)))

        for (i in 0 until 8) {
            val day = cachedDailyForecast.getJSONObject(i).getInt("dayOfWeek")
            val minTemp = cachedDailyForecast.getJSONObject(i).getInt("minTemp")
            val maxTemp = cachedDailyForecast.getJSONObject(i).getInt("maxTemp")
            val condition = cachedDailyForecast.getJSONObject(i).getJSONObject("condition")
                .getString("description")

            onView(withRecyclerView(R.id.dailyForecast).atPositionOnView(i,R.id.day))
                .check(matches(withText(
                    when(day) {
                        1 -> "Sun"
                        2 -> "Mon"
                        3 -> "Tue"
                        4 -> "Wed"
                        5 -> "Thu"
                        6 -> "Fri"
                        7 -> "Sat"
                        else -> throw IllegalArgumentException("Wrong day of week arg:$day")
                    }
                )))

            onView(withRecyclerView(R.id.dailyForecast).atPositionOnView(i,R.id.minTemp))
                .check(matches(withText("${minTemp}°")))

            onView(withRecyclerView(R.id.dailyForecast).atPositionOnView(i,R.id.maxTemp))
                .check(matches(withText("${maxTemp}°")))

            // Verify day forecast icon loaded
            scenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.fragments[0]
                val rv = fragment.view!!.findViewById<RecyclerView>(R.id.dailyForecast)
                val imageView = rv.findViewHolderForAdapterPosition(i)!!.itemView
                    .findViewById<ImageView>(R.id.dailyWeatherIcon)

                verifyConditionIconLoaded(imageView,condition,true)
            }
        }
    }

    @And("^Load weather update for current location and cache it$")
    fun load_weather_update_for_current_location_and_cache_it() {
        locationSubject.onNext(
            AppResult.Success(UserLocation(dispatcher.locationLat,dispatcher.locationLon))
        )
    }

    @And("^App should show it with update timestamp$")
    fun app_should_show_it_with_update_timestamp() {
        val weatherJson = getJsonFromResource(dispatcher.locationWeatherRes)
        val locationJson = getJsonFromResource(dispatcher.locationGeoRes)
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
        val query = SimpleSQLiteQuery("SELECT * FROM current_weather WHERE id = 1")
        val cursor = db.query(query).also { it.moveToFirst() }
        val cachedUpdateStamp = cursor.getLong(cursor.getColumnIndex("updated"))
        val hourlyForecast = JSONObject(weatherJson).getJSONArray("hourly")
        val dailyForecast = JSONObject(weatherJson).getJSONArray("daily")

        // Verify location name
        onView(withId(R.id.location_name))
            .check(matches(withText(name)))

        // Verify feel temp data
        onView(withId(R.id.feelTemp))
            .check(matches(withText("Feels like ${feelTemp.toInt()}°")))

        // Verify current temp data
        onView(withId(R.id.currentTemp))
            .check(matches(withText("${currentTemp.toInt()}°")))

        // Verify clock data
        onView(withId(R.id.textClock))
            .check(matches(withTimeZone(timeZone)))

        // Verify main weather icon loaded
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.fragments[0]
            val imageView = fragment.view!!.findViewById<ImageView>(R.id.mainWeatherIcon)
            verifyConditionIconLoaded(
                imageView,
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
                isDayCondition)
        }

        // Verify min and max data
        onView(withId(R.id.minMaxTemp))
            .check(matches(withText("min ${minTemp.toInt()}°/max ${maxTemp.toInt()}°")))

        // Verify index value
        onView(withId(R.id.uvValue))
            .check(matches(withText(uv)))

        // Verify humidity
        onView(withId(R.id.humidityValue))
            .check(matches(withText("${humidity.toInt()}%")))

        // Verify wind speed
        onView(withId(R.id.windSpeedValue))
            .check(matches(withText("${windSpeed.toInt()}km/h")))

        // Verify sunrise and sunset data
        onView(withId(R.id.sunriseValue))
            .check(matches(withText(
                SimpleDateFormat("HH:mm").format(
                    LocalDateTime(
                        sunrise * 1000,
                        DateTimeZone.forID(timeZone)
                    ).toDate()
                )
            )))

        onView(withId(R.id.sunsetValue))
            .check(matches(withText(
                SimpleDateFormat("HH:mm").format(
                    LocalDateTime(
                        sunset * 1000,
                        DateTimeZone.forID(timeZone)
                    ).toDate()
                )
            )))

        // Verify last update time
        onView(withId(R.id.updated))
            .check(matches(withText("Last updated: ".plus(
                SimpleDateFormat("dd MMM, HH:mm")
                    .format(Date(cachedUpdateStamp))
            ))))

        // Verify hourly forecast
        onView(withId(R.id.hourForecast))
            .check(matches(isRecyclerViewItemsCount(12)))

        for (i in 0 until 12) {
            val hour = hourlyForecast.getJSONObject(i).getLong("dt")
            val temp =  hourlyForecast.getJSONObject(i)
                .getDouble("temp")
            val hourConditionCode = hourlyForecast.getJSONObject(i).getJSONArray("weather")
                .getJSONObject(0).getInt("id")
            val isHourDay = hourlyForecast.getJSONObject(i).getJSONArray("weather")
                .getJSONObject(0).getString("icon").contains('d')

            onView(withId(R.id.hourForecast))
                .perform(scrollToPosition<HourlyForecastViewHolder>(i))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.hourForecast).atPositionOnView(i,R.id.hour))
                .check(matches(withText(
                    SimpleDateFormat("HH:mm").format(
                        LocalDateTime(
                            hour * 1000,
                            DateTimeZone.forID(timeZone)
                        ).toDate()
                    )
                )))

            onView(withRecyclerView(R.id.hourForecast).atPositionOnView(i,R.id.temp))
                .check(matches(withText("${temp.toInt()}°")))

            // Verify hour forecast icon loaded
            scenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.fragments[0]
                val rv = fragment.view!!.findViewById<RecyclerView>(R.id.hourForecast)
                val imageView = rv.findViewHolderForAdapterPosition(i)!!.itemView
                    .findViewById<ImageView>(R.id.hourWeatherIcon)

                verifyConditionIconLoaded(
                    imageView,
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
                    isHourDay)
            }
        }
    }

    private fun createCachedWeather(): CurrentWeatherEntity {
        return CurrentWeatherEntity(
            90.6,
            89.7,
            "Paris",
            "France",
            "Europe/Paris",
            34.2,
            35.0,
            28.5,
            35.0,
            15.0,
            4.6,
            1629776338000,
            1629823680000,
            1.33,
            WeatherCondition(WeatherDescription.Fog,true),
            UnitSystem.METRIC,
            listOf(HourForecast(1629788400000, WeatherCondition(WeatherDescription.Clouds,true),30.2),
                HourForecast(1629792000000, WeatherCondition(WeatherDescription.Clouds,true),30.2),
                HourForecast(1629795600000, WeatherCondition(WeatherDescription.Clouds,true),31.2),
                HourForecast(1629795600000, WeatherCondition(WeatherDescription.Clouds,true),31.2),
                HourForecast(1629799200000, WeatherCondition(WeatherDescription.Clouds,true),31.2),
                HourForecast(1629802800000, WeatherCondition(WeatherDescription.Clouds,true),32.2),
                HourForecast(1629806400000, WeatherCondition(WeatherDescription.Clouds,true),33.2),
                HourForecast(1629810000000, WeatherCondition(WeatherDescription.Drizzle,true),33.2),
                HourForecast(1629813600000, WeatherCondition(WeatherDescription.Drizzle,true),33.2),
                HourForecast(1629817200000, WeatherCondition(WeatherDescription.Drizzle,true),30.1),
                HourForecast(1629820800000, WeatherCondition(WeatherDescription.Rain,true),28.7),
                HourForecast(1629824400000, WeatherCondition(WeatherDescription.Rain,true),26.0)),
        listOf(DayForecast(1,34.6, WeatherCondition(WeatherDescription.Mist,true),23.4,32.7),
            DayForecast(2,24.6, WeatherCondition(WeatherDescription.Drizzle,true),21.4,32.7),
            DayForecast(3,30.6, WeatherCondition(WeatherDescription.Clear,true),23.4,33.7),
            DayForecast(4,30.6, WeatherCondition(WeatherDescription.Clear,true),23.4,33.7),
            DayForecast(5,24.6, WeatherCondition(WeatherDescription.Drizzle,true),21.4,32.7),
            DayForecast(6,27.2, WeatherCondition(WeatherDescription.Thunderstorm,true),25.3,30.9),
            DayForecast(7,27.2, WeatherCondition(WeatherDescription.Thunderstorm,true),25.3,30.9),
            DayForecast(1,20.6, WeatherCondition(WeatherDescription.Mist,true),25.3,30.9)),
            Calendar.getInstance().timeInMillis,
            CurrentWeatherEntity.WEATHER_ID
        )
    }

    private fun verifyConditionIconLoaded(imageView: ImageView, condition: String,isDay: Boolean) {
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