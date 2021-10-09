package com.diskin.alon.pagoda.weatherinfo.presentation

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.CombinedLoadStates
import androidx.paging.PagingDataAdapter
import androidx.paging.PagingDataDiffer
import com.diskin.alon.pagoda.weatherinfo.appservices.model.LocationDto
import com.diskin.alon.pagoda.weatherinfo.presentation.model.*
import java.util.concurrent.CopyOnWriteArrayList

fun createTestWeather(): UiWeather {
    return UiWeather(
        "city",
        "America/Chicago",
        "E, dd MMM yyyy HH:mm",
        null,
        "24°",
        "condition",
        10,
        "25°",
        "min max temp",
        "34%",
        "15km/h",
        "06:34",
        "19:34",
        "low",
        listOf(
            UiHourForecast("12:00","condition1",34,"23°"),
            UiHourForecast("13:00","condition2",32,"28°"),
            UiHourForecast("14:00","condition3",35,"32°")
        ),
        listOf(
            UiDayForecast("Sunday","condition4",45,"-10°","32°"),
            UiDayForecast("Monday","condition7",47,"10°","36°")
        ),
        "3 Mar, 12:45",
        "C"
    )
}

fun createFavoriteLocations() = listOf(
    UiLocation(
        10.0,
        20.7,
        "Location1",
        "Country1",
        true
    ),
    UiLocation(
        10.0,
        20.7,
        "Location2",
        "Country2, State2",
        true
    ),
    UiLocation(
        10.0,
        20.7,
        "Location3",
        "Country3",
        true
    ),
    UiLocation(
        10.0,
        20.7,
        "Location4",
        "Country4, State 4",
        true
    )
)

fun createSearchedLocations() = listOf(
    UiLocation(
        10.0,
        20.7,
        "Location1",
        "Country1",
        false
    ),
    UiLocation(
        10.0,
        20.7,
        "Location2",
        "Country2, State2",
        false
    ),
    UiLocation(
        10.0,
        20.7,
        "Location3",
        "Country3, State3",
        false
    ),
    UiLocation(
        10.0,
        20.7,
        "Location4",
        "Country4",
        true
    )
)

fun createUnFavoriteLocation() =
    UiLocation(
        10.0,
        20.7,
        "Location3",
        "Country3, State3",
        false
    )

fun createFavoritedLocation() =
    UiLocation(
        10.0,
        20.7,
        "Location3",
        "Country3, State3",
        true
    )

fun getSearchResultsAdapterLoadStatesListener(adapter: PagingDataAdapter<*, *>): (CombinedLoadStates) -> Unit {
    val field = PagingDataAdapter::class.java.getDeclaredField("differ")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    val differ = field.get(adapter) as AsyncPagingDataDiffer<LocationDto>

    val field2 = AsyncPagingDataDiffer::class.java.getDeclaredField("differBase")
    field2.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    val differBase = field2.get(differ) as PagingDataDiffer<LocationDto>

    val field3 = PagingDataDiffer::class.java.getDeclaredField("loadStateListeners")
    field3.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    val listeners = field3.get(differBase) as CopyOnWriteArrayList<(CombinedLoadStates) -> Unit>

    return listeners[1]
}