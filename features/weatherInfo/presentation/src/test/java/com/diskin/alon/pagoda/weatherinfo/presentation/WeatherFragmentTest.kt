package com.diskin.alon.pagoda.weatherinfo.presentation

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.os.Looper
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.pagoda.common.appservices.AppError
import com.diskin.alon.pagoda.common.presentation.ErrorViewData
import com.diskin.alon.pagoda.common.presentation.ImageLoader
import com.diskin.alon.pagoda.common.presentation.UpdateViewData
import com.diskin.alon.pagoda.common.uitesting.*
import com.diskin.alon.pagoda.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.HourlyForecastAdapter.HourlyForecastViewHolder
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.WeatherFragment
import com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel.WeatherViewModel
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationWeatherDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.UvIndexDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.WeatherConditionDto
import com.diskin.alon.pagoda.weatherinfo.appservices.model.WeatherDescriptionDto
import com.diskin.alon.pagoda.weatherinfo.errors.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.text.SimpleDateFormat
import java.util.*

/**
 * [WeatherFragment] hermetic ui test.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(sdk = [28])
class WeatherFragmentTest {

    // Test subject
    private lateinit var scenario: FragmentScenario<WeatherFragment>

    // Collaborators
    private val viewModel: WeatherViewModel = mockk()

    // Stub data
    private val weather = MutableLiveData<LocationWeatherDto>()
    private val update = MutableLiveData<UpdateViewData>()
    private val error = MutableLiveData<ErrorViewData>()
    private val testRegistry = object : ActivityResultRegistry() {
        override fun <I : Any?, O : Any?> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?
        ) {
            when(contract) {
                is RequestPermission -> {
                    if (input!! == Manifest.permission.ACCESS_FINE_LOCATION) {
                        dispatchResult(requestCode, true)
                    }
                }

                is StartIntentSenderForResult -> {
                    dispatchResult(requestCode,ActivityResult(Activity.RESULT_OK,null))
                }
            }
        }
    }

    @Before
    fun setUp() {
        // Stub view model creation with test mock
        mockkConstructor(ViewModelLazy::class)
        every { anyConstructed<ViewModelLazy<ViewModel>>().value } returns viewModel

        // Stub mocked collaborators
        every { viewModel.weather } returns weather
        every { viewModel.update } returns update
        every { viewModel.error } returns error

        // Launch fragment under test
        scenario = FragmentScenario.launchInContainer(
            WeatherFragment::class.java,
            null,
            R.style.AppTheme,
            object :FragmentFactory() {
                override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                    return WeatherFragment(testRegistry)
                }
            }
        )
    }

    @Test
    fun showWeatherWhenDataAvailable() {
        // Test fixture
        mockkObject(ImageLoader)
        every { ImageLoader.loadIconResIntoImageView(any(),any()) } returns Unit

        // Given a resumed fragment

        // When view model update weather state
        val weatherData = createTestWeather()
        this.weather.value = weatherData
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then fragment should show weather data in ui

        // Verify main weather icon loaded
        scenario.onFragment { fragment ->
            val imageView = fragment.view!!.findViewById<ImageView>(R.id.mainWeatherIcon)
            verifyConditionIconLoaded(imageView,weatherData.condition)
        }

        // Verify location name
        onView(withId(R.id.locationName))
            .check(matches(withText(weatherData.name)))

        // Verify location weather clock data
        onView(withId(R.id.textClock))
            .check(matches(withTimeZone(weatherData.timeZone)))

        onView(withId(R.id.textClock))
            .check(matches(withTimeFormat12("E, dd MMM yyyy hh:mm aa")))

        onView(withId(R.id.textClock))
            .check(matches(withTimeFormat24("E, dd MMM yyyy HH:mm")))

        // Verify current temp data
        onView(withId(R.id.currentTemp))
            .check(matches(withText("${weatherData.currentTemp.toInt()}°")))

        // Verify feel temp data
        onView(withId(R.id.feelTemp))
            .check(matches(withText("Feels like ${weatherData.feelTemp.toInt()}°")))

        // Verify weather description
        onView(withId(R.id.description))
            .check(matches(withText(weatherData.condition.description.name)))

        // Verify min and max data
        onView(withId(R.id.minMaxTemp))
            .check(matches(withText("min ${weatherData.minTemp.toInt()}°/max ${weatherData.maxTemp.toInt()}°")))

        // Verify index value
        val uv = when(weatherData.uvIndex) {
            UvIndexDto.LOW -> "Low"
            UvIndexDto.MODERATE -> "Moderate"
            UvIndexDto.HIGH -> "High"
            UvIndexDto.VERY_HIGH -> "Very high"
        }
        onView(withId(R.id.uvValue))
            .check(matches(withText(uv)))

        // Verify humidity
        onView(withId(R.id.humidityValue))
            .check(matches(withText("${weatherData.humidity.toInt()}%")))

        // Verify wind speed
        onView(withId(R.id.windSpeedValue))
            .check(matches(withText("${weatherData.windSpeed.toInt()}km/h")))

        // Verify sunrise and sunset data
        onView(withId(R.id.sunriseValue))
            .check(matches(withText(SimpleDateFormat("HH:mm")
                .format(Date(weatherData.sunrise)))))

        onView(withId(R.id.sunsetValue))
            .check(matches(withText(SimpleDateFormat("HH:mm")
                .format(Date(weatherData.sunset)))))

        // Verify hourly forecast
        onView(withId(R.id.hourForecast))
            .check(matches(isRecyclerViewItemsCount(weatherData.hourlyForecast.size)))

        weatherData.hourlyForecast.forEachIndexed { index, hourForecastDto ->
            onView(withId(R.id.hourForecast))
                .perform(scrollToPosition<HourlyForecastViewHolder>(index))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.hourForecast).atPositionOnView(index,R.id.hour))
                .check(matches(withText("${hourForecastDto.hour}:00")))

            onView(withRecyclerView(R.id.hourForecast).atPositionOnView(index,R.id.temp))
                .check(matches(withText("${hourForecastDto.temp.toInt()}°")))

            // Verify hourly forecast icon loaded
            scenario.onFragment { fragment ->
                val rv = fragment.view!!.findViewById<RecyclerView>(R.id.hourForecast)
                val imageView = rv.findViewHolderForAdapterPosition(index)!!.itemView
                    .findViewById<ImageView>(R.id.hourWeatherIcon)

                verifyConditionIconLoaded(imageView,hourForecastDto.condition)
            }
        }

        // Verify daily forecast
        onView(withId(R.id.dailyForecast))
            .check(matches(isRecyclerViewItemsCount(weatherData.dailyForecast.size)))

        weatherData.dailyForecast.forEachIndexed { index, dayForecastDto ->
            onView(withRecyclerView(R.id.dailyForecast).atPositionOnView(index,R.id.day))
                .check(
                    matches(
                        withText(
                            when(dayForecastDto.dayOfWeek) {
                                1 -> "Sunday"
                                2 -> "Monday"
                                3 -> "Tuesday"
                                4 -> "Wednesday"
                                5 -> "Thursday"
                                6 -> "Friday"
                                7 -> "Saturday"
                                else -> throw IllegalArgumentException(
                                    "Wrong day of week:${dayForecastDto.dayOfWeek}"
                                )
                            }
                        )
                    )
                )

            onView(withRecyclerView(R.id.dailyForecast).atPositionOnView(index,R.id.maxTemp))
                .check(matches(withText("${dayForecastDto.maxTemp.toInt()}°")))

            onView(withRecyclerView(R.id.dailyForecast).atPositionOnView(index,R.id.minTemp))
                .check(matches(withText("${dayForecastDto.minTemp.toInt()}°")))

            // Verify day forecast icon loaded
            scenario.onFragment { fragment ->
                val rv = fragment.view!!.findViewById<RecyclerView>(R.id.dailyForecast)
                val imageView = rv.findViewHolderForAdapterPosition(index)!!.itemView
                    .findViewById<ImageView>(R.id.dailyWeatherIcon)

                verifyConditionIconLoaded(imageView,dayForecastDto.condition)
            }
        }
    }

    @Test
    fun showRefreshIndicatorWhenWeatherDataRefreshed() {
        // Given a resumed fragment

        // When view model is refresh weather data
        update.value = UpdateViewData.Refresh
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then fragment should show refresh indicator
        scenario.onFragment {
            val refreshLayout = it.view!!.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
            assertThat(refreshLayout.isRefreshing).isTrue()
        }

        // When view model finished weather data refresh
        update.value = UpdateViewData.EndRefresh
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then fragment should hide refresh indicator
        scenario.onFragment {
            val refreshLayout = it.view!!.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
            assertThat(refreshLayout.isRefreshing).isFalse()
        }
    }

    @Test
    fun refreshWeatherDataWhenSwipedToRefresh() {
        // Test case fixture
        every { viewModel.refresh() } returns Unit

        // Given a resumed fragment

        // When user swipe to refresh
        onView(withId(R.id.swipeRefresh))
            .perform(swipeToRefresh())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then fragment should ask view model to refresh data
        verify { viewModel.refresh() }
    }

    @Test
    fun askUserForLocationPermissionUponPermissionError() {
        // Test case fixture
        every { viewModel.refresh() } returns Unit

        // Given a resumed fragment

        // When view model update a device network error
        error.value = ErrorViewData.Error(AppError(LOCATION_PERMISSION,true))
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.refresh() }
    }

    @Test
    fun askUserForLocationSettingActivationUponDeviceLocationSettingError() {
        val errorOrigin: ResolvableApiException = mockk()
        val pendingIntent: PendingIntent = mockk()

        every { errorOrigin.resolution } returns pendingIntent
        every { pendingIntent.intentSender } returns mockk()
        every { viewModel.refresh() } returns Unit

        error.value = ErrorViewData.Error(AppError(DEVICE_LOCATION,true,errorOrigin))
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        verify { viewModel.refresh() }
    }

    @Test
    fun askUserToConnectToNetworkAndRefreshUponDeviceNetworkError() {
        // Test case fixture
        every { viewModel.refresh() } returns Unit

        // Given a resumed fragment

        // When view model update a device network error
        error.value = ErrorViewData.Error(AppError(DEVICE_NETWORK,true))
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then fragment should show snackbar message with error description
        onView(withId(R.id.snackbar_text))
            .check(
                matches(
                    allOf(
                        withText(R.string.device_network_error),
                        isDisplayed()
                    )
                )
            )
    }

    @Test
    fun askUserToRefreshLaterUponRemoteServerError() {
        // Test case fixture
        every { viewModel.refresh() } returns Unit

        // Given a resumed fragment

        // When view model update a remote server error
        error.value = ErrorViewData.Error(AppError(REMOTE_SERVER,true))
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then fragment should show snackbar message with error description
        onView(withId(R.id.snackbar_text))
            .check(
                matches(
                    allOf(
                        withText(R.string.remote_server_error),
                        isDisplayed()
                    )
                )
            )
    }

    @Test
    fun notifyUserErrorMessageUponUnknownError() {
        // Given a resumed fragment

        // When view model update an unknown error
        error.value = ErrorViewData.Error(AppError(UNKNOWN_ERR,false))
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then fragment should show snackbar message with error description
        onView(withId(R.id.snackbar_text))
            .check(
                matches(
                    allOf(
                        withText(R.string.unknown_error),
                        isDisplayed()
                    )
                )
            )
    }

    @Test
    fun clearErrorNotificationsWhenNoErrorExist() {
        // Given a resumed fragment

        // When view model update that no  error exist
        error.value = ErrorViewData.NoError
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then fragment should clear all snackbar views from ui
        onView(withId(R.id.snackbar_text))
            .check(doesNotExist())
    }

    private fun verifyConditionIconLoaded(imageView: ImageView,condition: WeatherConditionDto) {
        when(condition.description) {
            WeatherDescriptionDto.Thunderstorm -> {
                verify { ImageLoader.loadIconResIntoImageView(imageView,R.drawable.ic_weather_thunder_96) }
            }

            WeatherDescriptionDto.Drizzle -> {
                verify { ImageLoader.loadIconResIntoImageView(imageView,R.drawable.ic_weather_drizzle_96) }
            }

            WeatherDescriptionDto.Rain -> {
                verify { ImageLoader.loadIconResIntoImageView(imageView,R.drawable.ic_weather_rain_96) }
            }

            WeatherDescriptionDto.Snow -> {
                verify { ImageLoader.loadIconResIntoImageView(imageView,R.drawable.ic_weather_snow_96) }
            }

            WeatherDescriptionDto.Mist, WeatherDescriptionDto.Fog -> {
                verify { ImageLoader.loadIconResIntoImageView(imageView,R.drawable.ic_weather_fog_96) }
            }

            WeatherDescriptionDto.Clear -> {
                when(condition.isDay) {
                    true -> verify { ImageLoader.loadIconResIntoImageView(imageView,R.drawable.ic_weather_clear_day_96) }
                    else -> verify { ImageLoader.loadIconResIntoImageView(imageView,R.drawable.ic_weather_clear_night_96) }
                }
            }

            WeatherDescriptionDto.Clouds -> {
                verify { ImageLoader.loadIconResIntoImageView(imageView,R.drawable.ic_weather_clouds_96) }
            }

            WeatherDescriptionDto.Haze, WeatherDescriptionDto.Dust, WeatherDescriptionDto.Sand -> {
                verify { ImageLoader.loadIconResIntoImageView(imageView,R.drawable.ic_weather_haze_96) }
            }

            WeatherDescriptionDto.Tornado -> {
                verify { ImageLoader.loadIconResIntoImageView(imageView,R.drawable.ic_weather_tornado_96) }
            }

            WeatherDescriptionDto.Unknown -> {
                verify { ImageLoader.loadIconResIntoImageView(imageView,R.drawable.ic_weather_unknown_96) }
            }
        }
    }
}