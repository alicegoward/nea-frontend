package com.example.myapplication

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SampleAPI {
    @POST("user/login")
    suspend fun loginUser(@Body userDataModel: UsernamePassword): Response<UserInfo>
    @POST("user/add")
    suspend fun postUserData(@Body userDataModel: UsernamePassword): Response<Success>
    @POST("user/add_friend")
    suspend fun addFriend(@Body addFriendModel: AddFriend): Response<Success>
    @POST("user/update_friend_state")
    suspend fun updateRequest(@Body updateRequestModel: UpdateRequest): Response<Success>
    @GET("user/notifications/{id}")
    suspend fun getNotificationData(@Path("id") id: String): Response<NotificationData>
    @GET("user/friends/{id}")
    suspend fun getFriends(@Path("id") id: String): Response<Friends>
    @GET("user/friend_requests/{id}")
    suspend fun getPendingRequests(@Path("id") id: String): Response<Friends>

    @POST("add_location")
    suspend fun postData(@Body locationDataModel: LocationData): Response<Success>

}

data class NotificationData (
    val notifications: HashMap<Int, String>,
    val success: Boolean,
)

data class UserInfo (
    val username: String,
    val password: String,
    val success: Boolean,
    val reason: String
)

data class LocationData (
    val datetime: Long,
    val user: String,
    val lat: Double,
    val long: Double
)

data class UsernamePassword(
    val username: String,
    val password: String
)

data class AddFriend(
    val userRequesting: String,
    val userRequested: String
)

data class Success(
    val success: Boolean,
    val reason: String
)

data class Friends(
    val success: Boolean,
    val names: HashMap<Int, String>
)

data class UpdateRequest(
    val userAccepting: String,
    val userAccepted: String,
    val result: String
)