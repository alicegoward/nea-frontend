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
    @GET("get_route/{id}")
    suspend fun getRoutes(@Path("id") id: String): Response<RouteDataList>

    @POST("add_location")
    suspend fun postData(@Body locationDataModel: LocationData): Response<Track>

    @POST("route/create")
    suspend fun postRoute(@Body routeModel: RouteData): Response<Success>

    @POST("route/sample")
    suspend fun getSampleData(@Body startEnd: Routing): Response<Track>

    @POST("route/deactivate/{id}")
    suspend fun deactivateRoute(@Path("id") id: String): Response<Success>
}

data class Track(
    val geometry: HashMap<Int, Coordinate>,
    val dist: Double
)

data class Coordinate(
    val lat: Double,
    val long: Double
)

data class Routing (
    val start: Coordinate,
    val user: String,
    val datetime: Long,
    val route: String
)

data class RouteDataList(
    val data: HashMap<Int, RouteData>,
    val success: Boolean
)

data class RouteData(
    val endTime: Long,
    val name: String,
    val lat: Double,
    val long: Double,
    val friends: HashMap<Int, String>,
    val username: String
)

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
    val names: HashMap<Int, String>,
    val incoming: HashMap<Int, String>,
    val outgoing: HashMap<Int, String>
)

data class UpdateRequest(
    val userAccepting: String,
    val userAccepted: String,
    val result: String
)