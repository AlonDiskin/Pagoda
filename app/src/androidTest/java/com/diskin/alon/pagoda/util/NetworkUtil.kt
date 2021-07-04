package com.diskin.alon.pagoda.util

import com.diskin.alon.pagoda.weatherinfo.data.BuildConfig
import okhttp3.HttpUrl
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONObject

object NetworkUtil {
    private var _server: MockWebServer? = null
    val dispatcher = TestDispatcher()
    val server: MockWebServer
    get() {
        return _server ?:
        throw NullPointerException("MockWebServer has not been initialized yet")
    }

    private var _url: HttpUrl? = null
    val url: HttpUrl
    get() {
        return _url ?:
        throw NullPointerException("MockWebServer has not been initialized yet")
    }

    fun initServer() {
        val mockWebServer = MockWebServer()
        _server = mockWebServer
        _url = mockWebServer.url("/")

        server.setDispatcher(dispatcher)
    }

    open class TestDispatcher : Dispatcher() {
        val weatherRes = "assets/json/weather.json"
        val geoRes = "assets/json/geocoding.json"
        private val weatherPath = "/data/2.5/onecall"
        private val geocodingPath = "/geo/1.0/reverse"
        private val longitude = JSONObject(FileUtil.readStringFromFile(weatherRes))
            .getDouble("lon")
        private val latitude = JSONObject(FileUtil.readStringFromFile(weatherRes))
            .getDouble("lat")

        override fun dispatch(request: RecordedRequest): MockResponse {
            return when(request.requestUrl.uri().path){
                weatherPath -> {
                    if (request.requestUrl.queryParameter("lat") == latitude.toString() &&
                        request.requestUrl.queryParameter("lon") == longitude.toString() &&
                        request.requestUrl.queryParameter("exclude") == "minutely,alerts" &&
                        request.requestUrl.queryParameter("units") == "metric" &&
                        request.requestUrl.queryParameter("appid") == BuildConfig.OPEN_WEATHER_MAP_API_KEY
                    ) {
                        MockResponse()
                            .setBody(FileUtil.readStringFromFile(weatherRes))
                            .setResponseCode(200)

                    } else {
                        MockResponse().setResponseCode(404)
                    }
                }

                geocodingPath -> {
                    if (request.requestUrl.queryParameter("lat") == latitude.toString() &&
                        request.requestUrl.queryParameter("lon") == longitude.toString() &&
                        request.requestUrl.queryParameter("limit") == "1" &&
                        request.requestUrl.queryParameter("appid") == BuildConfig.OPEN_WEATHER_MAP_API_KEY
                    ) {
                        MockResponse()
                            .setBody(FileUtil.readStringFromFile(geoRes))
                            .setResponseCode(200)

                    } else {
                        MockResponse().setResponseCode(404)
                    }
                }

                else -> {
                    MockResponse().setResponseCode(404)
                }
            }
        }
    }
}