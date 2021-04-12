package com.diskin.alon.pagoda.weatherinfo.di

import com.diskin.alon.pagoda.weatherinfo.data.remote.NetworkErrorHandler
import com.diskin.alon.pagoda.weatherinfo.data.remote.NetworkErrorHandlerImpl
import com.diskin.alon.pagoda.weatherinfo.data.remote.OPEN_WEATHER_API
import com.diskin.alon.pagoda.weatherinfo.data.remote.OpenWeatherMapApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WeatherInfoNetworkingModule {

    companion object {

        @Singleton
        @Provides
        fun provideOpenWeatherMapApi(httpClient: OkHttpClient): OpenWeatherMapApi {
            return Retrofit.Builder()
                .baseUrl(OPEN_WEATHER_API)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient)
                .build()
                .create(OpenWeatherMapApi::class.java)
        }
    }

    @Singleton
    @Binds
    abstract fun bindNetworkErrorHandler(handler: NetworkErrorHandlerImpl): NetworkErrorHandler
}