package com.diskin.alon.pagoda.weatherinfo.data

import com.diskin.alon.pagoda.weatherinfo.data.remote.interfaces.OpenWeatherMapApi
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiLocationResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiWeatherAlertResponse
import com.diskin.alon.pagoda.weatherinfo.data.remote.model.ApiWeatherResponse
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

/**
 * [OpenWeatherMapApi] integration test class.
 */
class OpenWeatherMapApiTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            // Fix MockWebServer 'No password supplied for PKCS#12 KeyStore' bug on CI machine
            System.setProperty("javax.net.ssl.trustStore", "NONE")
        }
    }

    // System under test
    private lateinit var api: OpenWeatherMapApi
    private val server = MockWebServer()

    // Test helpers
    private val gson = Gson()

    @Before
    fun setUp() {
        // Start mocked web server
        server.start()

        // Init api client
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
            .create(OpenWeatherMapApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun loadCurrentWeatherFromApiAndMapResponse() {
        // Test case fixture
        val dispatcher = object : Dispatcher() {
            val weatherRes = "current_weather.json"
            val locationLat = 55.7558
            val locationLon = 37.6173
            private val path = "/data/2.5/onecall"

            override fun dispatch(request: RecordedRequest): MockResponse {
                return when(request.method) {
                    "GET" -> {
                        if (request.requestUrl.uri().path == path &&
                            request.requestUrl.queryParameter("lat") == locationLat.toString() &&
                            request.requestUrl.queryParameter("lon") == locationLon.toString() &&
                            request.requestUrl.queryParameter("exclude") == "minutely,alerts" &&
                            request.requestUrl.queryParameter("units") == "metric" &&
                            request.requestUrl.queryParameter("appid") == BuildConfig.OPEN_WEATHER_MAP_API_KEY) {
                            MockResponse()
                                .setBody(getJsonFromResource(weatherRes))
                                .setResponseCode(200)

                        } else {
                            MockResponse().setResponseCode(404)
                        }
                    }

                    else -> MockResponse().setResponseCode(404)
                }
            }

        }

        // Given remote api server is running and api client is initialized
        server.setDispatcher(dispatcher)

        // When api sends get request to server for location weather data
        val observer = api.getCurrentWeather(dispatcher.locationLat,dispatcher.locationLon).test()

        // Then api should map server response to expected data model
        val serverJson = getJsonFromResource(dispatcher.weatherRes)
        val expectedApiResponse = gson.fromJson(serverJson, ApiWeatherResponse::class.java)
        observer.assertValue { it == expectedApiResponse }
    }

    @Test
    fun loadLocationDetailFromApiAndMapResponse() {
        // Test case fixture
        val dispatcher = object : Dispatcher() {
            val locationRes = "reverse_geocoding.json"
            val locationLat = 55.7558
            val locationLon = 37.6173
            private val path = "/geo/1.0/reverse"

            override fun dispatch(request: RecordedRequest): MockResponse {
                return when(request.method) {
                    "GET" -> {
                        if (request.requestUrl.uri().path == path &&
                            request.requestUrl.queryParameter("lat") == locationLat.toString() &&
                            request.requestUrl.queryParameter("lon") == locationLon.toString() &&
                            request.requestUrl.queryParameter("limit") == "1" &&
                            request.requestUrl.queryParameter("appid") == BuildConfig.OPEN_WEATHER_MAP_API_KEY) {
                            MockResponse()
                                .setBody(getJsonFromResource(locationRes))
                                .setResponseCode(200)

                        } else {
                            MockResponse().setResponseCode(404)
                        }
                    }

                    else -> MockResponse().setResponseCode(404)
                }
            }

        }

        // Given remote api server is running and api client is initialized
        server.setDispatcher(dispatcher)

        // When api sends get request to server for location detail
        val observer = api.getLocationDetail(dispatcher.locationLat,dispatcher.locationLon).test()

        // Then api should map server response to expected data model
        val serverJson = getJsonFromResource(dispatcher.locationRes)
        val expectedApiResponse = gson.fromJson(serverJson, Array<ApiLocationResponse>::class.java).toList()
        observer.assertValue { it == expectedApiResponse }
    }

    @Test
    fun loadWeatherAlertFromApiAndMapResponse() {
        // Test case fixture
        val dispatcher = object : Dispatcher() {
            val alertRes = "weather_alert.json"
            val locationLat = 55.7558
            val locationLon = 37.6173
            private val path = "/data/2.5/onecall"

            override fun dispatch(request: RecordedRequest): MockResponse {
                return when(request.method) {
                    "GET" -> {
                        if (request.requestUrl.uri().path == path &&
                            request.requestUrl.queryParameter("lat") == locationLat.toString() &&
                            request.requestUrl.queryParameter("lon") == locationLon.toString() &&
                            request.requestUrl.queryParameter("exclude") == "current,minutely,hourly,daily" &&
                            request.requestUrl.queryParameter("units") == "metric" &&
                            request.requestUrl.queryParameter("appid") == BuildConfig.OPEN_WEATHER_MAP_API_KEY) {
                            MockResponse()
                                .setBody(getJsonFromResource(alertRes))
                                .setResponseCode(200)

                        } else {
                            MockResponse().setResponseCode(404)
                        }
                    }

                    else -> MockResponse().setResponseCode(404)
                }
            }

        }

        // Given remote api server is running and api client is initialized
        server.setDispatcher(dispatcher)

        // When api sends get request to server for weather alert data
        val observer = api.getWeatherAlert(dispatcher.locationLat,dispatcher.locationLon).test()

        // Then api should map server response to expected data model
        val serverJson = getJsonFromResource(dispatcher.alertRes)
        val expectedApiResponse = gson.fromJson(serverJson, ApiWeatherAlertResponse::class.java)
        observer.assertValue { it == expectedApiResponse }
    }

    private fun getJsonFromResource(resource: String): String {
        val topLevelClass = object : Any() {}.javaClass.enclosingClass!!
        val jsonResource = topLevelClass.classLoader!! // javaClass.classLoader
            .getResource(resource)

        return File(jsonResource.toURI()).readText()
    }
}