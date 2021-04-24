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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.core.app.ActivityScenario
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
import com.diskin.alon.pagoda.weatherinfo.errors.*
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.HourlyForecastAdapter.HourlyForecastViewHolder
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.WeatherFragment
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiWeather
import com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel.WeatherViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.material.appbar.AppBarLayout
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/**
 * [WeatherFragment] hermetic ui test.
 */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@SmallTest
@Config(sdk = [28])
class WeatherFragmentTest {

    // Test subject
    private lateinit var scenario: ActivityScenario<HiltTestActivity>

    // Collaborators
    private val viewModel: WeatherViewModel = mockk()

    // Stub data
    private val isCurrentLocation = true
    private val weather = MutableLiveData<UiWeather>()
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
        every { viewModel.isCurrentLocation } returns isCurrentLocation

        // Test require AppCompatActivity properties verification,which HiltTestActivity inherits from
        // Launch fragment under test
        scenario = launchFragmentInHiltContainer<WeatherFragment>(
            factory = object :FragmentFactory() {
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
        scenario.onActivity {
            val imageView = it.findViewById<ImageView>(R.id.mainWeatherIcon)
            verify { ImageLoader.loadIconResIntoImageView(imageView,weatherData.conditionIconRes) }
        }

        // Verify location name
        onView(withId(R.id.locationName))
            .check(matches(withText(weatherData.locationName)))

        // Verify location weather clock data
        onView(withId(R.id.textClock))
            .check(matches(withTimeZone(weatherData.timeZone)))

        onView(withId(R.id.textClock))
            .check(matches(withTimeFormat12(weatherData.clock12HourFormat)))

        onView(withId(R.id.textClock))
            .check(matches(withTimeFormat24(weatherData.clock24HourFormat)))

        // Verify current temp data
        onView(withId(R.id.currentTemp))
            .check(matches(withText(weatherData.currentTemp)))

        // Verify feel temp data
        onView(withId(R.id.feelTemp))
            .check(matches(withText(weatherData.feelTemp)))

        // Verify weather description
        onView(withId(R.id.description))
            .check(matches(withText(weatherData.weatherCondition)))

        // Verify min and max data
        onView(withId(R.id.minMaxTemp))
            .check(matches(withText(weatherData.minMaxTemp)))

        // Verify index value
        onView(withId(R.id.uvValue))
            .check(matches(withText(weatherData.uvIndex)))

        // Verify humidity
        onView(withId(R.id.humidityValue))
            .check(matches(withText(weatherData.humidity)))

        // Verify wind speed
        onView(withId(R.id.windSpeedValue))
            .check(matches(withText(weatherData.windSpeed)))

        // Verify sunrise and sunset data
        onView(withId(R.id.sunriseValue))
            .check(matches(withText(weatherData.sunrise)))

        onView(withId(R.id.sunsetValue))
            .check(matches(withText(weatherData.sunset)))

        // Verify hourly forecast
        onView(withId(R.id.hourForecast))
            .check(matches(isRecyclerViewItemsCount(weatherData.hourlyForecast.size)))

        weatherData.hourlyForecast.forEachIndexed { index, hourForecast ->
            onView(withId(R.id.hourForecast))
                .perform(scrollToPosition<HourlyForecastViewHolder>(index))
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            onView(withRecyclerView(R.id.hourForecast).atPositionOnView(index,R.id.hour))
                .check(matches(withText(hourForecast.hour)))

            onView(withRecyclerView(R.id.hourForecast).atPositionOnView(index,R.id.temp))
                .check(matches(withText(hourForecast.temp)))

            // Verify hourly forecast icon loaded
            scenario.onActivity {
                val rv = it.findViewById<RecyclerView>(R.id.hourForecast)
                val imageView = rv.findViewHolderForAdapterPosition(index)!!.itemView
                    .findViewById<ImageView>(R.id.hourWeatherIcon)
                verify { ImageLoader.loadIconResIntoImageView(imageView,hourForecast.conditionIconRes) }
            }
        }

        // Verify daily forecast
        onView(withId(R.id.dailyForecast))
            .check(matches(isRecyclerViewItemsCount(weatherData.dailyForecast.size)))

        weatherData.dailyForecast.forEachIndexed { index, dayForecast ->
            onView(withRecyclerView(R.id.dailyForecast).atPositionOnView(index,R.id.day))
                .check(matches(withText(dayForecast.dayOfWeek)))

            onView(withRecyclerView(R.id.dailyForecast).atPositionOnView(index,R.id.maxTemp))
                .check(matches(withText(dayForecast.maxTemp)))

            onView(withRecyclerView(R.id.dailyForecast).atPositionOnView(index,R.id.minTemp))
                .check(matches(withText(dayForecast.minTemp)))

            // Verify day forecast icon loaded
            scenario.onActivity {
                val rv = it.findViewById<RecyclerView>(R.id.dailyForecast)
                val imageView = rv.findViewHolderForAdapterPosition(index)!!.itemView
                    .findViewById<ImageView>(R.id.dailyWeatherIcon)
                verify { ImageLoader.loadIconResIntoImageView(imageView,dayForecast.conditionIconRes) }
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
        scenario.onActivity {
            val refreshLayout = it.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
            assertThat(refreshLayout.isRefreshing).isTrue()
        }

        // When view model finished weather data refresh
        update.value = UpdateViewData.EndRefresh
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then fragment should hide refresh indicator
        scenario.onActivity {
            val refreshLayout = it.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
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

    @Test
    fun showLocationNameAsAppbarTitleWheAppbarCollapses() {
        // Given
        val weatherData = createTestWeather()
        this.weather.value = weatherData
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        scenario.onActivity {
            it.findViewById<AppBarLayout>(R.id.appBar).setExpanded(false)
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }

        // Then
        scenario.onActivity {
            assertThat(it.supportActionBar!!.title).isEqualTo(weatherData.locationName)
        }
    }

    @Test
    fun hideLocationNameAsAppbarTitleWheAppbarExpanded() {
        // Given
        val weatherData = createTestWeather()
        this.weather.value = weatherData
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        scenario.onActivity {
            it.findViewById<AppBarLayout>(R.id.appBar).setExpanded(true)
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }

        // Then
        scenario.onActivity {
            assertThat(it.supportActionBar!!.title).isEqualTo("")
        }
    }

    @Test
    fun showCurrentLocationIndicatorWhenShowsCurrentLocationWeather() {
        // Test fixture
        mockkObject(ImageLoader)
        every { ImageLoader.loadIconResIntoImageView(any(),any()) } returns Unit

        // Given
        every { viewModel.isCurrentLocation } returns true

        // When
        scenario.recreate()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { ImageLoader.loadIconResIntoImageView(any(),R.drawable.ic_baseline_location_24) }
    }

    @Test
    fun hideCurrentLocationIndicatorWhenShowsLocationWeather() {
        // Test fixture
        mockkObject(ImageLoader)
        every { ImageLoader.loadIconResIntoImageView(any(),any()) } returns Unit

        // Given

        // Then
        verify(exactly = 0) { ImageLoader.loadIconResIntoImageView(any(),R.drawable.ic_baseline_location_24) }
    }
}