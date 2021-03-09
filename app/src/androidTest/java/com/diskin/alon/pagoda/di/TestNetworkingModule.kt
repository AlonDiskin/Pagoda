package com.diskin.alon.pagoda.di

import com.diskin.alon.pagoda.util.NetworkUtil
import com.diskin.alon.pagoda.weatherinfo.data.remote.NetworkErrorHandler
import com.diskin.alon.pagoda.weatherinfo.data.remote.NetworkErrorHandlerImpl
import com.diskin.alon.pagoda.weatherinfo.data.remote.OpenWeatherMapApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TestNetworkingModule {

    companion object {
        
        @Singleton
        @Provides
        fun provideHttpClient(): OkHttpClient {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BASIC

            return OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
        }

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

    @Singleton
    @Binds
    abstract fun bindNetworkErrorHandler(handler: NetworkErrorHandlerImpl): NetworkErrorHandler
}