package com.diskin.alon.pagoda.weatherinfo.data

import com.diskin.alon.pagoda.weatherinfo.data.remote.util.RemoteWeatherMapper
import org.junit.Before

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