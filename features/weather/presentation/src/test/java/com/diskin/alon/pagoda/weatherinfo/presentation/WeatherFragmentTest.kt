package com.diskin.alon.pagoda.weatherinfo.presentation

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.os.Looper
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.core.app.ActivityOptionsCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.diskin.alon.pagoda.common.appservices.results.AppError
import com.diskin.alon.pagoda.common.appservices.results.ErrorType.*
import com.diskin.alon.pagoda.common.presentation.ImageLoader
import com.diskin.alon.pagoda.common.presentation.SingleLiveEvent
import com.diskin.alon.pagoda.common.presentation.UpdateViewData
import com.diskin.alon.pagoda.common.uitesting.*
import com.diskin.alon.pagoda.common.uitesting.RecyclerViewMatcher.withRecyclerView
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.HourlyForecastAdapter.HourlyForecastViewHolder
import com.diskin.alon.pagoda.weatherinfo.presentation.controller.WeatherFragment
import com.diskin.alon.pagoda.weatherinfo.presentation.model.UiWeather
import com.diskin.alon.pagoda.weatherinfo.presentation.viewmodel.WeatherViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.material.appbar.AppBarLayout
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowToast

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
    private val navController: TestNavHostController = TestNavHostController(ApplicationProvider.getApplicationContext())

    // Stub data
    private val weather = MutableLiveData<UiWeather>()
    private val isCurrentLocationWeather = MutableLiveData<Boolean>()
    private val update = MutableLiveData<UpdateViewData>()
    private val error = SingleLiveEvent<AppError>()
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
        every { viewModel.isCurrentLocationWeather } returns isCurrentLocationWeather

        mockkObject(ImageLoader)
        every { ImageLoader.loadIconResIntoImageView(any(),any()) } returns Unit

        // Launch fragment under test
        scenario = launchFragmentInHiltContainer<WeatherFragment>(
            factory = object :FragmentFactory() {
                override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                    return WeatherFragment(testRegistry)
                }
            }
        )

        // Set test nav controller
        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments.first() as WeatherFragment

            navController.setGraph(R.navigation.weather_graph)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun showWeather_WhenDataAvailable() {
        // Given

        // When
        val weatherData = createTestWeather()
        weather.value = weatherData
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then

        // Verify main weather icon loaded
        scenario.onActivity {
            val imageView = it.findViewById<ImageView>(R.id.mainWeatherIcon)
            verify { ImageLoader.loadIconResIntoImageView(imageView,weatherData.conditionIconRes) }
        }

        // Verify location name
        onView(withId(R.id.location_name))
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

        // Verify current temp unit
        onView(withId(R.id.currentTempUnit))
            .check(matches(withText(weatherData.currentTempUnit)))

        // Verify feel temp data
        onView(withId(R.id.feelTemp))
            .check(matches(withText(weatherData.feelTemp)))

        // Verify last update time
        onView(withId(R.id.updated))
            .check(matches(withText(weatherData.updated)))

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
    fun showRefreshIndicator_WhenWeatherDataRefreshed() {
        // Given

        // When
        update.value = UpdateViewData.Refresh
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            val refreshLayout = it.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
            assertThat(refreshLayout.isRefreshing).isTrue()
        }

        // When
        update.value = UpdateViewData.EndRefresh
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        scenario.onActivity {
            val refreshLayout = it.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
            assertThat(refreshLayout.isRefreshing).isFalse()
        }
    }

    @Test
    fun refreshWeatherData_WhenSwipedToRefresh() {
        // Given
        every { viewModel.refresh() } returns Unit

        // When
        onView(withId(R.id.swipeRefresh))
            .perform(swipeToRefresh())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        verify { viewModel.refresh() }
    }

    @Test
    fun askUserForLocationPermission_UponPermissionError() {
        // Given
        every { viewModel.refresh() } returns Unit

        // When view model update a device network error
        error.value = AppError(LOCATION_PERMISSION)
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

        error.value = AppError(DEVICE_LOCATION,errorOrigin)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        verify { viewModel.refresh() }
    }

    @Test
    fun notifyUserOfNetworkError_WhenDeviceNetworkFail() {
        // Given
        every { viewModel.refresh() } returns Unit

        // When
        error.value = AppError(DEVICE_NETWORK)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        val expectedToastMessage = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.device_network_error)
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(expectedToastMessage)
    }

    @Test
    fun notifyUserOfRemoteServerError_WhenRemoteServerFailFail() {
        // Given
        every { viewModel.refresh() } returns Unit

        // When
        error.value = AppError(REMOTE_SERVER)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        val expectedToastMessage = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.remote_server_error)
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(expectedToastMessage)
    }

    @Test
    fun notifyUserErrorMessage_UponUnknownError() {
        // Given

        // When
        error.value = AppError(UNKNOWN_ERR)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        val expectedToastMessage = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.unknown_error)
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(expectedToastMessage)
    }

    @Test
    fun showLocationNameAsAppbarTitle_WhenAppbarCollapses() {
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
    fun hideLocationNameAsAppbarTitle_WheAppbarExpanded() {
        // Given
        weather.value = createTestWeather()
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
    fun hideCurrentLocationIndicator_WhenShowsLocationWeather() {
        // Given

        // Then
        verify(exactly = 0) { ImageLoader.loadIconResIntoImageView(any(),R.drawable.ic_baseline_location_24) }
    }

    @Test
    fun navigateToLocationsScreen_WhenSelectedFromMenu() {
        // Given

        // When
        onView(withId(R.id.action_locations))
            .perform(click())

        // Then
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.locationsFragment)
    }

    @Test
    fun showCurrentLocationWeather_WhenSelectedFromMenu() {
        // Given
        every { viewModel.loadCurrentLocationWeather() } returns Unit

        // When
        onView(withId(R.id.action_current_location_weather))
            .perform(click())

        // Then
        verify { viewModel.loadCurrentLocationWeather() }
    }

    @Test
    fun showLocationWeather_WhenLocationReturnedAsResult() {
        // Given
        every { viewModel.loadLocationWeather(any(),any()) } returns Unit

        // When
        val lat = 23.68
        val lon = 67.24

        scenario.onActivity {
            val fragment = it.supportFragmentManager.fragments[0] as WeatherFragment
            val bundle = bundleOf(
                it.getString(R.string.arg_lat_key) to lat,
                it.getString(R.string.arg_lon_key) to lon
            )
            val requestKey = it.getString(R.string.locaiton_request_key)

            fragment.setFragmentResult(requestKey,bundle)
        }

        Shadows.shadowOf(Looper.getMainLooper()).idle()


        // Then
        verify { viewModel.loadLocationWeather(lat,lon) }
    }

    @Test
    fun resolveCurrentWeatherIndicatorUiVisibility_WhenIndicatorUpdated() {
        // Given
        weather.value = createTestWeather()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // When
        isCurrentLocationWeather.value = true
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.locationIndicator))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        // When
        isCurrentLocationWeather.value = false
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        // Then
        onView(withId(R.id.locationIndicator))
            .check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))
    }
}