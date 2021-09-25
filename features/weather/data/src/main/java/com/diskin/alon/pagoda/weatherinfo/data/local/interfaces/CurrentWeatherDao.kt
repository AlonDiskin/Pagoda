package com.diskin.alon.pagoda.weatherinfo.data.local.interfaces

import androidx.room.*
import com.diskin.alon.pagoda.weatherinfo.data.local.model.CurrentWeatherEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface CurrentWeatherDao {

    @Query("SELECT * FROM current_weather WHERE id = :id")
    fun getWeather(id: Int): Observable<CurrentWeatherEntity>

    @Query("SELECT * FROM current_weather WHERE id = :id")
    fun getWeatherSingle(id: Int): Single<CurrentWeatherEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM current_weather WHERE id = :id)")
    fun isWeatherExist(id: Int): Single<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(weather: CurrentWeatherEntity): Completable
}