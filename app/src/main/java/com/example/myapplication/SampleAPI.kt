package com.example.myapplication

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SampleAPI {
    @GET("detail/{id}")
    suspend fun getData(@Path("id") id: String): Response<SampleData>
    @GET("user/view/{username}/{password}")
    suspend fun getUserData(@Path("username") username: String, @Path("password") password: String): Response<UserInfo>
    @GET("notifications/{id}")
    suspend fun getNotificationData(@Path("id") id: String): Response<NotificationData>
    @GET("friends/{id}")
    suspend fun getFriends(@Path("id") id: String): Response<Friends>
    @GET("friend_requests/{id}")
    suspend fun getPendingRequests(@Path("id") id: String): Response<Friends>

    @POST("add_location")
    suspend fun postData(@Body locationDataModel: location_data_model): Response<success>
    @POST("user/add")
    suspend fun postUserData(@Body userDataModel: user_data_model): Response<success>
    @POST("add_friend")
    suspend fun addFriend(@Body addFriendModel: add_friend_model): Response<success>
    @POST("update_friend_state")
    suspend fun updateRequest(@Body updateRequestModel: update_request_model): Response<success>
}

data class SampleData(
    val id: Int,
    val question_text: String,
    val pub_date: String,
    val success: Boolean
)

data class NotificationData(
    val notifications: HashMap<Int, String>,
    val success: Boolean,
)

data class UserInfo(
    val username: String,
    val password: String,
    val success: Boolean,
    val reason: String
)

data class location_data_model(
    var datetime: Long,
    var user: String,
    var lat: Double,
    var long: Double
)

data class user_data_model(
    var username: String,
    val password: String
)

data class add_friend_model(
    val user_requesting: String,
    val user_requested: String
)

data class success(
    var success: Boolean,
    val reason: String
)

data class Friends(
    var success: Boolean,
    val names: HashMap<Int, String>
)

data class update_request_model(
    var user_accepting: String,
    val user_accepted: String,
    val result: String
)