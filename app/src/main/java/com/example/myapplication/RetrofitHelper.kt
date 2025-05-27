package com.example.myapplication

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitHelper {

    private const val BASE_URL = "http://192.168.137.1:8000/db/"

    var okHttpClient: OkHttpClient = createClient("aaaa")

    fun createClient(newToken: String): OkHttpClient {
        Log.i("TOKEN", newToken)
        return OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("auth", newToken)
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    fun getData(doStuff: suspend () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                doStuff()
            } catch (e: Exception) {
                Log.e("internet", e.toString())
            }
        }
    }
}