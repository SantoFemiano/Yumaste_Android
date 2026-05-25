package com.example.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Standard Base URL of Yumaste REST API
    private const val BASE_URL = "https://yumaste.duckdns.org/"

    @Volatile
    var token: String? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
            
            token?.let { currentToken ->
                requestBuilder.addHeader("Authorization", "Bearer $currentToken")
            }
            
            chain.proceed(requestBuilder.build())
        }
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: YumasteApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(YumasteApiService::class.java)
    }
}
