package com.diskin.alon.pagoda.locations.domain

import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [Coordinates] unit test class.
 */
@RunWith(JUnitParamsRunner::class)
class CoordinatesTest {

    @Test(expected = IllegalArgumentException::class)
    @Parameters(method = "latLonParams")
    fun throwExceptionWhenCreatedWithOutOfRangeValues(lat: Double,lon: Double) {
        // Given an out of range lat and lon values

        // When coordinates instance is initialized with values
        Coordinates(lat, lon)

        // The coordinates should throw an exception
    }

    fun latLonParams() = arrayOf(
        arrayOf(45,180.1),
        arrayOf(45,-180.1),
        arrayOf(90.1,10.00),
        arrayOf(-90.1,10.00)
    )
}