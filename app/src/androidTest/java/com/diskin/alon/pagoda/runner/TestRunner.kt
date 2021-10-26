package com.diskin.alon.pagoda.runner

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import androidx.test.espresso.IdlingRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.runner.AndroidJUnitRunner
import com.diskin.alon.pagoda.R
import com.diskin.alon.pagoda.common.presentation.EspressoIdlingResource
import com.diskin.alon.pagoda.common.uitesting.LoadingIdlingResource
import com.diskin.alon.pagoda.home.presentation.MainActivity
import com.diskin.alon.pagoda.util.DeviceUtil
import com.diskin.alon.pagoda.util.FileUtil
import com.diskin.alon.pagoda.util.NetworkUtil
import com.diskin.alon.pagoda.weather.infrastructure.WeatherAlertSettingHandlerService
import com.google.android.gms.location.LocationServices
import com.squareup.rx2.idler.Rx2Idler
import dagger.hilt.android.testing.HiltTestApplication
import io.reactivex.plugins.RxJavaPlugins
import org.json.JSONObject
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class TestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        var hasNavListener = false
        val app =  super.newApplication(cl, HiltTestApplication::class.java.name, context)

        // Register loading idling resource
        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            private lateinit var loadingIdlingResource: LoadingIdlingResource

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if(activity is MainActivity) {
                    loadingIdlingResource = LoadingIdlingResource(
                        activity as FragmentActivity,
                        R.id.progress_bar,R.id.swipeRefresh
                    )
                    IdlingRegistry.getInstance().register(loadingIdlingResource)

                }
            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {
                if(activity is MainActivity) {
                    if (!hasNavListener) {
                        Navigation.findNavController(activity,R.id.nav_host_container)
                            .addOnDestinationChangedListener { _, destination, _ ->
                                if (destination.id == R.id.settingsFragment) {
                                    context!!.startService(Intent(
                                        context,
                                        WeatherAlertSettingHandlerService::class.java)
                                    )
                                } else {
                                    context!!.stopService(
                                        Intent(
                                            context,
                                            WeatherAlertSettingHandlerService::class.java)
                                    )
                                }
                            }

                        hasNavListener = true
                    }
                }

            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {
                if(activity is MainActivity) {
                    IdlingRegistry.getInstance().unregister(loadingIdlingResource)
                }
            }
        })

        return app
    }

    override fun onStart() {
        // Register espresso idling resource
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)

        // Disable data binding Choreographer
        setFinalStatic(ViewDataBinding::class.java.getDeclaredField("USE_CHOREOGRAPHER"),false)

        // Init RxIdler
        RxJavaPlugins.setInitIoSchedulerHandler(
            Rx2Idler.create("RxJava 2.x IO Scheduler"))

        // Start test server
        NetworkUtil.initServer()

        // Set initial mock location for all test cases
        val locationProviderClient = LocationServices.getFusedLocationProviderClient(getInstrumentation().targetContext)
        val weatherJson = FileUtil.readStringFromFile("assets/json/weather.json")
        val mockLocation = Location("pagoda test mock location provider").also {
            it.accuracy = 10.0f
            it.time = System.currentTimeMillis()
            it.longitude = JSONObject(weatherJson).getDouble("lon")
            it.latitude = JSONObject(weatherJson).getDouble("lat")
            it.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        }

        DeviceUtil.getDevice().executeShellCommand("appops set com.diskin.alon.pagoda android:mock_location allow")
        locationProviderClient.setMockMode(true)
        locationProviderClient.setMockLocation(mockLocation)

        super.onStart()
    }

    override fun onDestroy() {
        NetworkUtil.server.shutdown()
        super.onDestroy()
    }

    private fun setFinalStatic(field: Field, newValue: Any?) {
        field.isAccessible = true
        val modifiersField: Field = try {
            Field::class.java.getDeclaredField("accessFlags")
        } catch (e: NoSuchFieldException) {
            //This is an emulator JVM  ¯\_(ツ)_/¯
            Field::class.java.getDeclaredField("modifiers")
        }
        modifiersField.isAccessible = true
        modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
        field.set(null, newValue)
    }
}