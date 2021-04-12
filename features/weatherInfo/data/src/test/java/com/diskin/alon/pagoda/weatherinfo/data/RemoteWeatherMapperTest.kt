package com.diskin.alon.pagoda.weatherinfo.data

import com.diskin.alon.pagoda.weatherinfo.data.remote.RemoteWeatherMapper
import org.junit.Before
import org.junit.Test

/**
 * [RemoteWeatherMapper] unit test class.
 */
class RemoteWeatherMapperTest {

    // Test subject
    private lateinit var mapper: RemoteWeatherMapper

    @Before
    fun setUp() {
        mapper = RemoteWeatherMapper()
    }

}