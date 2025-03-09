package com.example.myapplication

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface SampleAPI {
    @GET("/todos/{id}")
    suspend fun getData(@Path("id") id: String): Response<SampleData>
}

data class SampleData(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean,
)
