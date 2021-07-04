package com.diskin.alon.pagoda.weatherinfo.featuretesting.cacheuserlocationweather

import android.app.PendingIntent
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
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.appservices.AppResult
import com.diskin.alon.pagoda.common.appservices.ErrorType
import com.diskin.alon.pagoda.common.appservices.toResult
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
import com.diskin.alon.pagoda.weatherinfo.data.local.interfaces.UserLocationProvider
import com.diskin.alon.pagoda.weatherinfo.data.local.model.CurrentWeatherEntity
import com.diskin.alon.pagoda.weatherinfo.data.local.model.UserLocation
import com.diskin.alon.pagoda.weatherinfo.domain.*
import com.diskin.alon.pagoda.weatherinfo.featuretesting.TestDatabase
import com.diskin.alon.pagoda.weatherinfo.presentation.R
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.HourlyForecastAdapter.HourlyForecastViewHolder
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.WeatherFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import com.mauriciotogneri.greencoffee.annotations.When
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.reactivex.Observable
import okhttp3.mockwebserver.*
import org.joda.time.LocalDateTime
import org.json.JSONArray
import org.json.JSONObject
import org.robolectric.Shadows
import java.text.SimpleDateFormat
import java.util.*

/**
 * Step definitions for 'Weather cache update fail' scenario.
 */
class CacheUpdateFailSteps(
    private val server: MockWebServer,
    private val db: TestDatabase,
    private val locationProvider: UserLocationProvider,
    private val tempUnitPrefProvider: AppEventProvider<TemperatureUnitPref>,
    private val windSpeedUnitPrefProvider: AppEventProvider<WindSpeedUnitPref>,
    private val timeFormatPrefProvider: AppEventProvider<TimeFormatPref>
) : GreenCoffeeSteps() {

    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    init {
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

    @Given("^App has cached location weather data$")
    fun app_has_cached_location_weather_data() {
        // Set test db
        db.currentWeatherDao().insert(createCachedWeather()).blockingAwait()
    }

    @And("^Existing error \"([^\"]*)\" that fail update$")
    fun existing_error_something_that_fail_update(error: String) {
        when(error) {
            "app location permission" -> {
                val permissionError = AppResult.Error<UserLocation>(AppError(ErrorType.LOCATION_PERMISSION))
                every { locationProvider.getLocation() } returns Observable.just(permissionError)
            }

            "device location sensor" -> {
                val errorOrigin: ResolvableApiException = mockk()
                val pendingIntent: PendingIntent = mockk()
                val permissionError = AppResult.Error<UserLocation>(AppError(ErrorType.DEVICE_LOCATION,errorOrigin))

                every { errorOrigin.resolution } returns pendingIntent
                every { pendingIntent.intentSender } returns mockk()
                every { locationProvider.getLocation() } returns Observable.just(permissionError)
            }

            "device network" -> {
                // Prepare test server for scenario
                val dispatcher = object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        return MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
                    }
                }

                server.setDispatcher(dispatcher)

                // Prepare location provider for scenario
                val location = UserLocation(10.0, 10.0)
                every { locationProvider.getLocation() } returns Observable.just(location).toResult()
            }

            "remote server" -> {
                // Prepare test server for scenario
                val dispatcher = object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        return MockResponse().setResponseCode(500)
                    }
                }

                server.setDispatcher(dispatcher)

                // Prepare location provider for scenario
                val location = UserLocation(10.0, 10.0)
                every { locationProvider.getLocation() } returns Observable.just(location).toResult()
            }
        }
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
            .check(
                matches(
                    withText(
                        when {
                            (cachedUvIndex < 3) -> "Low"
                            (cachedUvIndex >= 3 && cachedUvIndex < 6) -> "Moderate"
                            (cachedUvIndex >= 6 && cachedUvIndex < 8) -> "High"
                            else -> "Very high"
                        }
                    )
                )
            )

        // Verify humidity
        onView(withId(R.id.humidityValue))
            .check(matches(withText("${cachedHumidity.toInt()}%")))

        // Verify wind speed
        onView(withId(R.id.windSpeedValue))
            .check(matches(withText("${cachedWindSpeed.toInt()}km/h")))

        // Verify sunrise and sunset data
        onView(withId(R.id.sunriseValue))
            .check(
                matches(
                    withText(
                        SimpleDateFormat("HH:mm").format(
                            LocalDateTime(cachedSunrise).toDate()
                        )
                    )
                )
            )

        onView(withId(R.id.sunsetValue))
            .check(
                matches(
                    withText(
                        SimpleDateFormat("HH:mm").format(
                            LocalDateTime(cachedSunset).toDate()
                        )
                    )
                )
            )

        // Verify last update time
        onView(withId(R.id.updated))
            .check(
                matches(
                    withText(
                        "Last updated: ".plus(
                            SimpleDateFormat("dd MMM, HH:mm")
                                .format(Date(cachedUpdateStamp))
                        )
                    )
                )
            )

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
                .perform(
                    scrollToPosition<HourlyForecastViewHolder>(i)
                )
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(
                withRecyclerView(R.id.hourForecast)
                    .atPositionOnView(i, R.id.hour)
            )
                .check(
                    matches(
                        withText(
                            SimpleDateFormat("HH:mm").format(
                                LocalDateTime(hour).toDate()
                            )
                        )
                    )
                )

            onView(
                withRecyclerView(R.id.hourForecast)
                    .atPositionOnView(i, R.id.temp)
            )
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

            onView(
                withRecyclerView(R.id.dailyForecast)
                    .atPositionOnView(i, R.id.day)
            )
                .check(
                    matches(
                        withText(
                            when (day) {
                                1 -> "Sun"
                                2 -> "Mon"
                                3 -> "Tue"
                                4 -> "Wed"
                                5 -> "Thu"
                                6 -> "Fri"
                                7 -> "Sat"
                                else -> throw IllegalArgumentException("Wrong day of week arg:$day")
                            }
                        )
                    )
                )

            onView(
                withRecyclerView(R.id.dailyForecast)
                    .atPositionOnView(i, R.id.minTemp)
            )
                .check(matches(withText("${minTemp}°")))

            onView(
                withRecyclerView(R.id.dailyForecast)
                    .atPositionOnView(i, R.id.maxTemp)
            )
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
            listOf(
                HourForecast(1629788400000, WeatherCondition(WeatherDescription.Clouds,true),30.2),
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
                HourForecast(1629824400000, WeatherCondition(WeatherDescription.Rain,true),26.0)
            ),
            listOf(
                DayForecast(1,34.6, WeatherCondition(WeatherDescription.Mist,true),23.4,32.7),
                DayForecast(2,24.6, WeatherCondition(WeatherDescription.Drizzle,true),21.4,32.7),
                DayForecast(3,30.6, WeatherCondition(WeatherDescription.Clear,true),23.4,33.7),
                DayForecast(4,30.6, WeatherCondition(WeatherDescription.Clear,true),23.4,33.7),
                DayForecast(5,24.6, WeatherCondition(WeatherDescription.Drizzle,true),21.4,32.7),
                DayForecast(6,27.2, WeatherCondition(WeatherDescription.Thunderstorm,true),25.3,30.9),
                DayForecast(7,27.2, WeatherCondition(WeatherDescription.Thunderstorm,true),25.3,30.9),
                DayForecast(1,20.6, WeatherCondition(WeatherDescription.Mist,true),25.3,30.9)
            ),
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
}