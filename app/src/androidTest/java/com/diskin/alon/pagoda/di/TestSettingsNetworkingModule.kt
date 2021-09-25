package com.diskin.alon.pagoda.di

import com.diskin.alon.pagoda.settings.data.remote.OpenWeatherMapApi
import com.diskin.alon.pagoda.util.NetworkUtil
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
object TestSettingsNetworkingModule {

    @Singleton
    @Provides
    fun provideFeedlyApi(httpClient: OkHttpClient): OpenWeatherMapApi {
        return Retrofit.Builder()
            .baseUrl(NetworkUtil.url)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(httpClient)
            .build()
            .create(OpenWeatherMapApi::class.java)
    }
}