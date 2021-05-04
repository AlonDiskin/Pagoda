package com.diskin.alon.pagoda.weatherinfo.featuretesting.di

import com.diskin.alon.pagoda.weatherinfo.data.remote.OpenWeatherMapApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestNetworkingModule {

    @Singleton
    @Provides
    fun provideMockWebServer(): MockWebServer {
        // Fix MockWebServer 'No password supplied for PKCS#12 KeyStore' bug on CI machine
        System.setProperty("javax.net.ssl.trustStore", "NONE")
        val server =  MockWebServer()

        server.start()
        return server
    }

    @Singleton
    @Provides
    fun provideOpenWeatherMapApi(server: MockWebServer): OpenWeatherMapApi {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
            .create(OpenWeatherMapApi::class.java)
    }
}