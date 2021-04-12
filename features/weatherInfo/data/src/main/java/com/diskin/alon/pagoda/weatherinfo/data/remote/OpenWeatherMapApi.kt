package com.diskin.alon.pagoda.weatherinfo.data.remote

import com.diskin.alon.pagoda.weatherinfo.data.model.ApiLocationResponse
import com.diskin.alon.pagoda.weatherinfo.data.model.ApiWeatherResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * OpenWeatherMap rest client api contract.
 */
interface OpenWeatherMapApi {

    @GET("${WEATHER_PATH}?${WEATHER_QUERY}")
    fun getCurrentWeather(
        @Query(LAT_PARAM) lat: Double,
        @Query(LON_PARAM) lon: Double,
    ): Single<ApiWeatherResponse>

    @GET("${LOCATION_PATH}?${LOCATION_QUERY}")
    fun getLocationDetail(
        @Query(LAT_PARAM) lat: Double,
        @Query(LON_PARAM) lon: Double,
    ): Single<List<ApiLocationResponse>>
}