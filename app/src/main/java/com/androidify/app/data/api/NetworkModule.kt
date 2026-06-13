package com.androidify.app.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt DI module providing networking dependencies.
 * Configures Retrofit with OkHttp, Gson, and appropriate timeouts
 * for AI-powered image processing endpoints.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Base URL for the backend API.
     * Uses 10.0.2.2 which maps to host machine's localhost from Android emulator.
     * Change this to the actual server IP for physical device testing.
     */
    private const val BASE_URL = "https://androidify-backend.onrender.com/"

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)    // AI generation can be slow
            .writeTimeout(60, TimeUnit.SECONDS)     // Large base64 uploads
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAndroidifyApi(retrofit: Retrofit): AndroidifyApi {
        return retrofit.create(AndroidifyApi::class.java)
    }
}
