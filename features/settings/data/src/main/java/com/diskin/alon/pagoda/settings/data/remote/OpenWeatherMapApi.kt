package com.diskin.alon.pagoda.settings.data.remote

import com.diskin.alon.pagoda.settings.data.model.ApiWeatherAlertResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * OpenWeatherMap rest api client contract.
 */
interface OpenWeatherMapApi {

    @GET("$ALERT_DATA_PATH?$ALERT_DATA_QUERY")
    fun getWeatherAlert(
        @Query(LAT_PARAM) lat: Double,
        @Query(LON_PARAM) lon: Double,
    ): Single<ApiWeatherAlertResponse>
}