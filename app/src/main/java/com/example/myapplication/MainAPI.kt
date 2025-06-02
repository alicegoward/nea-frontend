package com.example.myapplication

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MainAPI {
    @POST("user/login")
    suspend fun loginUser(@Body userDataModel: UsernamePassword): Response<UserInfo>
    @POST("user/register")
    suspend fun postUserData(@Body userDataModel: UsernamePassword): Response<Success>
    @POST("user/add_friend")
    suspend fun addFriend(@Body addFriendModel: AddFriend): Response<Success>
    @POST("user/update_friend_state")
    suspend fun updateRequest(@Body updateRequestModel: UpdateRequest): Response<Success>
    @GET("user/notifications")
    suspend fun getNotificationData(): Response<NotificationData>
    @GET("user/friends")
    suspend fun getFriends(): Response<Friends>
    @GET("get_route")
    suspend fun getRoutes(): Response<RouteDataList>

    @POST("add_location")
    suspend fun postData(@Body locationDataModel: LocationData): Response<Track>

    @POST("route/create")
    suspend fun postRoute(@Body routeModel: RouteData): Response<Success>

    @POST("route/sample")
    suspend fun getSampleData(@Body startEnd: Routing): Response<Track>

    @POST("route/deactivate")
    suspend fun deactivateRoute(): Response<Success>

    @POST("route/update")
    suspend fun updateRoute(@Body updateRoute: Map<String, String>): Response<Success>

    @POST("route/get_individual_config")
    suspend fun getIndividualConfig(@Body userOnRoute: UserOnRoute): Response<ConfigData>

    @POST("route/set_individual_config")
    suspend fun setIndividualConfig(@Body configData: ConfigData): Response<Success>

    @POST("route/notify_others")
    suspend fun notifyOthers(@Body dataToNotify: DataToNotify): Response<Success>
}

data class Track(
    val geometry: HashMap<Int, Coordinate>,
    val dist: Double,
    val other_paths: HashMap<String, HashMap<Int, Coordinate>>,
    val waypoints: HashMap<Int, Coordinate>,
    val itineraries: HashMap<Int, Itinerary>
)

data class DataToNotify(
    val data: String,
    val route: String,
    val user: String
)

data class Itinerary(
    val eta: Float,
    val waypoints: HashMap<Int, ItineraryWaypoint>,
    val username: String
)

data class ItineraryWaypoint(
    val participants: HashMap<Int, String>,
    val eta: Float
)

data class UserOnRoute (
    val route: String
)

data class ConfigData(
    val route: String,
    val create_mor: Boolean,
    val mor_tolerance_s: Double,
    val reroute_tolerance_m: Double,
    val auto_notify_late: Double
)

data class Coordinate(
    val lat: Double,
    val long: Double
)

data class Routing (
    val position: Coordinate,
    val datetime: Long,
    val route: String
)

data class RouteDataList(
    val data: HashMap<Int, RouteData>,
)

data class RouteData(
    val endTime: Long,
    val name: String,
    val username: String,
    val lat: Double,
    val long: Double,
    val friends: HashMap<Int, String>,
    val junction_name: String,
)

data class NotificationData (
    val notifications: HashMap<Int, String>,
)

data class UserInfo (
    val token: String,
    val reason: String
)

data class LocationData (
    val datetime: Long,
    val position: Coordinate
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
    val reason: String
)

data class Friends(
    val names: HashMap<Int, String>,
    val incoming: HashMap<Int, String>,
    val outgoing: HashMap<Int, String>
)

data class UpdateRequest(
    val userAccepting: String,
    val userAccepted: String,
    val result: String
)