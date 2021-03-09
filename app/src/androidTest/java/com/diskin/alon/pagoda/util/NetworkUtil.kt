package com.diskin.alon.pagoda.util

import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer

object NetworkUtil {
    private var _server: MockWebServer? = null
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
    }
}