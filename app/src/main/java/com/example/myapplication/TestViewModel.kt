package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Looper
import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

enum class RouteState {
    START, CHOOSE_FRIENDS, CHOOSE_LOCATION, CHOOSE_END_TIME, END;
    fun next(): RouteState {
        return when (this) {
            START -> CHOOSE_FRIENDS
            CHOOSE_FRIENDS -> CHOOSE_LOCATION
            CHOOSE_LOCATION -> CHOOSE_END_TIME
            CHOOSE_END_TIME -> END
            END -> END
        }
    }
    fun prev(): RouteState {
        return when (this) {
            START -> START
            CHOOSE_FRIENDS -> START
            CHOOSE_LOCATION -> CHOOSE_FRIENDS
            CHOOSE_END_TIME -> CHOOSE_LOCATION
            END -> CHOOSE_END_TIME
        }
    }
}

class Route(EndLocation: GeoPoint) {
    var state by mutableStateOf(RouteState.START)
    var friends = mutableStateListOf<String>()
    var endLocation by mutableStateOf(EndLocation)
    var endDate: Long? = null
    var endTime: Int? = null
    var name by mutableStateOf("")

    fun toRouteModel(username: String): RouteData {
        val friendDict = HashMap<Int, String>()
        friends.forEachIndexed {idx, item -> friendDict[idx] = item}
        return RouteData(
            endTime = endDate!! + endTime!!,
            lat = endLocation.latitude,
            long = endLocation.longitude,
            friends = friendDict,
            username = username,
            name = name,
            junction_name = "",
        )
    }
}

class RouteInfo(routeData: RouteData) {
    val name = routeData.name
    val end_lat = routeData.lat
    val end_long = routeData.long
    val admin = routeData.username
    val participants = routeData.friends
    val end_time = routeData.endTime
    val junction_name = routeData.junction_name
    var create_mor by mutableStateOf("No")
    var mor_tolerance_s by mutableStateOf("10 minutes")
    var reroute_tolerance_m by mutableStateOf("50 m")
    var auto_notify_late by mutableStateOf("10 minutes")
}

class TestViewModel : ViewModel() {
    var other_routes by mutableStateOf(HashMap<String, List<GeoPoint>>())
    var waypoints: List<GeoPoint> by mutableStateOf(emptyList())
    var activeRoute: RouteInfo? by mutableStateOf(null)
    var friends: MutableCollection<String>? by mutableStateOf(null)
    var outgoing_requests: MutableCollection<String>? by mutableStateOf(null)
    var pending_requests: MutableCollection<String>? by mutableStateOf(null)
    var permissionsDone by mutableStateOf(false)
    var buttonText by mutableStateOf("")
    var running: Boolean by mutableStateOf(false)
    val sampleAPI: SampleAPI = RetrofitHelper.getInstance().create(SampleAPI::class.java)
    var data by mutableStateOf("help")
    var create_account by mutableStateOf(false)
    var user: String? by mutableStateOf(null)
    var error_text: String? by mutableStateOf(null)
    var error_add_friend: String? by mutableStateOf(null)
    var position by mutableStateOf(GeoPoint(0.0, 0.0))
    private lateinit var locationCallback: LocationCallback
    var locationText by mutableStateOf("unknown location")
    var routeCreation = Route(this.position)
    var routes: List<RouteInfo> by mutableStateOf(emptyList())
    var sampleRoute by mutableStateOf(emptyList<GeoPoint>())
    var route_dist: Double? by mutableStateOf(null)
    var itineraries: MutableCollection<Itinerary>? by mutableStateOf(null)

    @SuppressLint("MissingPermission")
    fun getLocation(fusedLocationProviderClient: FusedLocationProviderClient) {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                val location = p0.locations[p0.locations.size-1]
                if (activeRoute != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val route_data = sampleAPI.postData(
                            LocationData(
                                Instant.now().epochSecond,
                                user!!,
                                Coordinate(location.latitude, location.longitude)
                            )
                        ).body()
                        if (route_data != null) {
                            sampleRoute = route_data.geometry.toSortedMap().values.map { GeoPoint(it.lat, it.long) }
                            route_data.other_paths.forEach { (user, path) ->
                                other_routes[user] = path.toSortedMap().values.map { GeoPoint(it.lat, it.long) }
                            }
                            Log.d("stuff", route_data.toString())
                            waypoints = route_data.waypoints.values.toList().map { GeoPoint(it.lat, it.long) }
                            route_dist = route_data.dist
                            itineraries = route_data.itineraries.values.toMutableList()
                        }
                    }
                }
                position = GeoPoint(location.latitude, location.longitude)

            }
        }
        val locationRequest = LocationRequest.Builder(5000).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun createUser(to_check: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val status = sampleAPI.postUserData(UsernamePassword(to_check, password)).body()
            Log.d("user create status", status.toString())
            if (status?.success == true) {
                user = to_check
            } else {
                error_text = status?.reason
                Log.e("create account", error_text.toString())
            }
        }
    }

    fun saveRoute() {
        CoroutineScope(Dispatchers.IO).launch {
            sampleAPI.postRoute(routeCreation.toRouteModel(user!!))
            Log.d("route","saved")
            routeCreation = Route(this@TestViewModel.position)
            getRoutes()
        }
    }

    fun getIndividualConfig(route: RouteInfo) {
        CoroutineScope(Dispatchers.IO).launch {
            val config = sampleAPI.getIndividualConfig(UserOnRoute(user!!, route.name)).body()
            route.create_mor = if (config!!.create_mor) "Yes" else "No"
            route.mor_tolerance_s = "${(config.mor_tolerance_s/60).toInt()} minutes"
            route.reroute_tolerance_m = "${config.reroute_tolerance_m.toInt()} m"
            route.auto_notify_late = "${(config.auto_notify_late/60).toInt()} minutes"
        }
    }

    fun getRoutes() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("route?", sampleAPI.getRoutes(user!!).body().toString())
            routes = sampleAPI.getRoutes(user!!).body()!!.data.map {i -> RouteInfo(i.value) }
        }
    }

    fun loginUser(to_check: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val user_data = sampleAPI.loginUser(UsernamePassword(to_check, password)).body()
            Log.d("data", user_data.toString())
            if (user_data?.success == true) {
                user = to_check
            } else {
                error_text = user_data?.reason
                Log.e("login", error_text.toString())
            }
        }
    }

    fun getFriends() {
        CoroutineScope(Dispatchers.IO).launch {
            if (user != null) {
                val data = sampleAPI.getFriends(user!!).body()
                Log.d("maybe some data", data.toString())
                friends = data?.names?.values
                pending_requests = data?.incoming?.values
                outgoing_requests = data?.outgoing?.values
            }
        }
    }


    fun update_request(request: String, state: String) {
        CoroutineScope(Dispatchers.IO).launch {
            sampleAPI.updateRequest(UpdateRequest(user!!, request, state))
            this@TestViewModel.getFriends()
        }
    }


    fun addFriend(to_add: String) {
        CoroutineScope(Dispatchers.IO).launch {
            error_add_friend = sampleAPI.addFriend(
                AddFriend(
                    userRequesting = user!!,
                    userRequested = to_add
                )
            ).body()?.reason
            getFriends()
        }
    }

    fun getSampleData() {
        CoroutineScope(Dispatchers.IO).launch {
            val route_data = sampleAPI.getSampleData(Routing(
                position = Coordinate(position.latitude, position.longitude),
                user = user!!,
                datetime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                route = activeRoute!!.name
            )).body()
            sampleRoute = route_data!!.geometry.toSortedMap().values.map {GeoPoint(it.lat, it.long)}
            route_data.other_paths.forEach { (user, path) ->
                other_routes[user] = path.toSortedMap().values.map { GeoPoint(it.lat, it.long) }
            }
            waypoints = route_data.waypoints.toSortedMap().values.map { GeoPoint(it.lat, it.long) }
            Log.d("other routes", other_routes.toString())
            route_dist = route_data.dist
            itineraries = route_data.itineraries.values.toMutableList()
        }
    }

    fun activateRoute(route: RouteInfo) {
        if (route == activeRoute) {
            activeRoute = null
            itineraries = null
            sampleRoute = emptyList()
            CoroutineScope(Dispatchers.IO).launch {
                sampleAPI.deactivateRoute(user!!)
            }
        } else {
            activeRoute = route
            getSampleData()
        }
    }
    fun updateRouteInfo(data: Map<String, String>) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("have data", data.toString())
            sampleAPI.updateRoute(data)
            this@TestViewModel.getRoutes()
        }
    }

    fun setIndividualConfig(route: RouteInfo) {
        CoroutineScope(Dispatchers.IO).launch {
            sampleAPI.setIndividualConfig(ConfigData(
                user!!,
                route.name,
                route.create_mor == "Yes",
                route.mor_tolerance_s.takeWhile { it.isDigit() }.toDouble() * 60,
                route.reroute_tolerance_m.takeWhile { it.isDigit() }.toDouble(),
                        route.auto_notify_late.takeWhile { it.isDigit() }.toDouble() * 60,

                ))
            getIndividualConfig(route)
        }
    }

    fun sendOthersNotification(meaning: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (meaning == "late") {
                sampleAPI.notifyOthers(Data_to_notify("$user is running late", activeRoute!!.name, user!!))
            } else {
                sampleAPI.notifyOthers(Data_to_notify("$user is lost", activeRoute!!.name, user!!))
            }
        }
    }
}