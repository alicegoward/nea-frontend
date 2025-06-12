package com.example.myapplication

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Preconfigured Retrofit instance using OkHttpClient
 */
object RetrofitHelper {

    private const val BASE_URL = "http://192.168.137.1:8000/api/"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                TokenManager.token?.let { token ->
                    requestBuilder.addHeader("auth", token)
                }
                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    /**
     * @return Retrofit instance
     */
    fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

/**
 * Singleton to manage the token
 */
object TokenManager {
    var token: String? = null
}
