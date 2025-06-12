package com.example.myapplication

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Client side equivalent of urls.py, posts and sends data to the server
 */
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

    @POST("route/activate")
    suspend fun activateRoute(@Body startEnd: Routing): Response<Track>

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

/**
 * @param geometry Enumerated dictionary of lat/long points
 * @param dist Total length of the path
 * @param other_paths Dictionary of usernames, each of which contain an enumerated dictionary of coordinates
 * @param waypoints Dictionary of coordinates of waypoints
 * @param itineraries Enumerated dictionary of itinerary
 */
data class Track(
    val geometry: HashMap<Int, Coordinate>,
    val dist: Double,
    val other_paths: HashMap<String, HashMap<Int, Coordinate>>,
    val waypoints: HashMap<Int, Coordinate>,
    val itineraries: HashMap<Int, Itinerary>
)

/**
 * @param data String that other users should be notified with
 * @param route Route that the users on should be notified
 * @param user User sending the notification
 */
data class DataToNotify(
    val data: String,
    val route: String,
    val user: String
)

/**
 * @param eta Total length of route
 * @param waypoints Enumerated dictionary of waypoints
 * @param username Name of the participant
 */
data class Itinerary(
    val eta: Float,
    val waypoints: HashMap<Int, ItineraryWaypoint>,
    val username: String
)

/**
 * @param participants Enumerated dictionary of people at the waypoint
 * @param eta Distance until waypoint
 */
data class ItineraryWaypoint(
    val participants: HashMap<Int, String>,
    val eta: Float
)

/**
 * @param route name of route
 */
data class UserOnRoute (
    val route: String
)

/**
 * @param route name of route
 * @param create_mor whether the user is configured to meet on route
 * @param mor_tolerance_s how many extra seconds the route can take to include a meet on route
 * @param reroute_tolerance_m how many meters of course the user will go before rerouting
 * @param auto_notify_late how many seconds late the person can be before an automatic notification is sent
 */
data class ConfigData(
    val route: String,
    val create_mor: Boolean,
    val mor_tolerance_s: Double,
    val reroute_tolerance_m: Double,
    val auto_notify_late: Double
)

/**
 * @param lat latitude
 * @param long longitude
 */
data class Coordinate(
    val lat: Double,
    val long: Double
)

/**
 * @param position current location
 * @param datetime current time
 * @param route route name
 */
data class Routing (
    val position: Coordinate,
    val datetime: Long,
    val route: String
)

/**
 * @param data Enumerated dictionary of route data
 */
data class RouteDataList(
    val data: HashMap<Int, RouteData>,
)

/**
 * @param endTime meeting time (epoch seconds)
 * @param name route name
 * @param username admin of route
 * @param lat latitude of end coordinate
 * @param long longitude of end coordinate
 * @param friends list of usernames also on the route
 * @param junction_name name of meeting junction
 */
data class RouteData(
    val endTime: Long,
    val name: String,
    val username: String,
    val lat: Double,
    val long: Double,
    val friends: HashMap<Int, String>,
    val junction_name: String,
)

/**
 * @param notifications dictionary of notifications directed at user
 */
data class NotificationData (
    val notifications: HashMap<Int, String>,
)

/**
 * @param token authentication token
 * @param reason reason for login failure
 */
data class UserInfo (
    val token: String,
    val reason: String
)

/**
 * @param datetime time at which location data was recorded
 * @param position coordinate of person
 */
data class LocationData (
    val datetime: Long,
    val position: Coordinate
)

/**
 * @param username Person's username
 * @param password Person's password
 */
data class UsernamePassword(
    val username: String,
    val password: String
)

/**
 * @param userRequesting user initiating friend request
 * @param userRequested user who has been sent a friend request
 */
data class AddFriend(
    val userRequesting: String,
    val userRequested: String
)

/**
 * @param reason cause of failure / success
 */
data class Success(
    val reason: String
)

/**
 * @param names enumerated dictionary of current friends
 * @param incoming enumerated dictionary incoming friend requests
 * @param outgoing enumerated dictionary of friend requests that have been sent but not acted upon
 */
data class Friends(
    val names: HashMap<Int, String>,
    val incoming: HashMap<Int, String>,
    val outgoing: HashMap<Int, String>
)

/**
 * @param userAccepting name of user accepting request
 * @param userAccepted name of user whose request should be accepted
 * @param result whether the user accepted or declined the request
 */
data class UpdateRequest(
    val userAccepting: String,
    val userAccepted: String,
    val result: String
)