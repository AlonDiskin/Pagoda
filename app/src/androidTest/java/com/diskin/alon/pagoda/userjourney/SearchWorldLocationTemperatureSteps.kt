package com.diskin.alon.pagoda.userjourney

import androidx.appcompat.widget.SearchView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import com.diskin.alon.pagoda.R
import com.diskin.alon.pagoda.common.uitesting.isRecyclerViewItemsCount
import com.diskin.alon.pagoda.common.uitesting.typeSearchViewText
import com.diskin.alon.pagoda.locations.presentation.controller.LocationSearchResultsAdapter.LocationSearchResultViewHolder
import com.diskin.alon.pagoda.util.DeviceUtil
import com.diskin.alon.pagoda.util.FileUtil
import com.diskin.alon.pagoda.util.TestDatabase
import com.diskin.alon.pagoda.weatherinfo.data.BuildConfig
import com.mauriciotogneri.greencoffee.GreenCoffeeSteps
import com.mauriciotogneri.greencoffee.annotations.And
import com.mauriciotogneri.greencoffee.annotations.Given
import com.mauriciotogneri.greencoffee.annotations.Then
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONObject

/**
 * Step definitions for 'User browse world location temperature' scenario.
 */
class SearchWorldLocationTemperatureSteps(db: TestDatabase, server: MockWebServer) : GreenCoffeeSteps() {
    private val selectedLat = 18.213001
    private val selectedLon = 59.195999
    private val query = "los angeles"
    private val locations = createDbLocations()
    private val dispatcher = TestDispatcher(selectedLat, selectedLon)

    init {
        // Prepare test server
        server.setDispatcher(dispatcher)

        // Prepare test database
        locations.forEach {
            val insertSql = "INSERT INTO locations (lat,lon,name,country,state,bookmarked)" +
                    "VALUES(${it.lat},${it.lon},'${it.name}','${it.country}','${it.state}',0);"

            db.compileStatement(insertSql).executeInsert()
        }
    }

    @Given("^User launch app from device home$")
    fun user_launch_app_from_device_home() {
        DeviceUtil.launchAppFromHome()
        Thread.sleep(1000)
    }

    @And("^User search for location other then his current one$")
    fun user_search_for_location_other_then_his_current_one() {
        // Open search screen
        onView(withId(R.id.drawerLayout))
            .perform(DrawerActions.open())

        onView(withId(R.id.nav_view))
            .perform(NavigationViewActions.navigateTo(R.id.nav_search))

        // Perform search
        onView(isAssignableFrom(SearchView::class.java))
            .perform(typeSearchViewText(query))
        Thread.sleep(2000)
    }

    @Then("^All matching locations by name should be shown$")
    fun all_matching_locations_by_name_should_be_shown() {
        val expectedUiResultsSize = locations.filter { it.name.startsWith(query, ignoreCase = true) }.size

        // Verify expected search results are shown
        onView(withId(R.id.search_location_results))
            .check(matches(isRecyclerViewItemsCount(expectedUiResultsSize)))
    }

    @And("^Select the first location result$")
    fun select_the_first_location_result() {
        // Click on first search result
        onView(withId(R.id.search_location_results))
            .perform(actionOnItemAtPosition<LocationSearchResultViewHolder>(0, click()))
        Thread.sleep(2000)
    }

    @Then("^Selected location temperature should be shown$")
    fun selected_location_temperature_should_be_shown() {
        val weatherJson = FileUtil.readStringFromFile(dispatcher.locationWeatherRes)
        val currentTemp = JSONObject(weatherJson).getJSONObject("current")
            .getDouble("temp")

        // Verify current temp show
        onView(withId(R.id.currentTemp))
            .check(matches(withText(currentTemp.toInt().toString().plus("°"))))
    }

    private data class DbLocation(
        val lat: Double,
        val lon: Double,
        val name: String,
        val country: String,
        val state: String
    )

    private fun createDbLocations(): List<DbLocation> {
        return listOf(
            DbLocation(
                36.213001,
                49.195999,
                "Losevo",
                "Russia",
                ""
            ),
            DbLocation(
                26.213001,
                19.195999,
                "Losal",
                "India",
                ""
            ),
            DbLocation(
                28.213001,
                39.195999,
                "Los Banos",
                "Brazil",
                ""
            ),
            DbLocation(
                selectedLat,
                selectedLon,
                "Los Angeles",
                "Use",
                "CA"
            ),
            DbLocation(
                10.213001,
                51.195999,
                "Los Angeles",
                "Philippines",
                ""
            ),
            DbLocation(
                13.213001,
                50.195999,
                "Los Angeles County",
                "Use",
                "CA"
            ),
            DbLocation(
                43.213001,
                40.195999,
                "Moscow",
                "Russia",
                ""
            ),
            DbLocation(
                29.213001,
                11.195999,
                "New York",
                "Use",
                "NY"
            ),

            DbLocation(
                13.213001,
                4.195999,
                "Paris",
                "France",
                ""
            )
        )
    }

    private class TestDispatcher(val selectedLat: Double, val selectedLon: Double): Dispatcher() {
        val locationWeatherRes = "assets/json/location_weather.json"
        val locationGeoRes = "assets/json/location_geocoding.json"
        private val weatherPath = "/data/2.5/onecall"
        private val geocodingPath = "/geo/1.0/reverse"

        override fun dispatch(request: RecordedRequest): MockResponse {
            return when(request.requestUrl.uri().path){
                weatherPath -> {
                    if (request.requestUrl.queryParameter("lat") == selectedLat.toString()  &&
                        request.requestUrl.queryParameter("lon") == selectedLon.toString()  &&
                        request.requestUrl.queryParameterNames().contains("lon") &&
                        request.requestUrl.queryParameter("exclude") == "minutely,alerts" &&
                        request.requestUrl.queryParameter("units") == "metric" &&
                        request.requestUrl.queryParameter("appid") == BuildConfig.OPEN_WEATHER_MAP_API_KEY
                    ) {

                        MockResponse()
                            .setBody(FileUtil.readStringFromFile(locationWeatherRes))
                            .setResponseCode(200)

                    } else {
                        MockResponse().setResponseCode(404)
                    }
                }

                geocodingPath -> {
                    if (request.requestUrl.queryParameter("lat") == selectedLat.toString()  &&
                        request.requestUrl.queryParameter("lon") == selectedLon.toString()  &&
                        request.requestUrl.queryParameter("limit") == "1" &&
                        request.requestUrl.queryParameter("appid") == BuildConfig.OPEN_WEATHER_MAP_API_KEY
                    ) {

                        MockResponse()
                            .setBody(FileUtil.readStringFromFile(locationGeoRes))
                            .setResponseCode(200)

                    } else {
                        MockResponse().setResponseCode(404)
                    }
                }

                else -> MockResponse().setResponseCode(404)
            }
        }
    }
}