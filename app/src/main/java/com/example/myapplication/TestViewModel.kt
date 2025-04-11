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

enum class RouteState {
    START, CHOOSE_FRIENDS, CHOOSE_LOCATION, CHOOSE_END_TIME, END
}

class Route {
    var state by mutableStateOf(RouteState.START)
    var friends = mutableStateListOf<String>()
    var endLocation by mutableStateOf(GeoPoint(0.0,0.0))
    var endDate: Int? = null
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
            name = name
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
}

class TestViewModel : ViewModel() {
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
    var routeCreation = Route()
    var routes: List<RouteInfo> by mutableStateOf(emptyList())
    var sampleRoute by mutableStateOf(emptyList<GeoPoint>())

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
                                location.latitude,
                                location.longitude
                            )
                        ).body()
                        if (route_data != null) {
                            if (!route_data.values.isEmpty()) {
                                sampleRoute = route_data.toSortedMap().values.map { GeoPoint(it.lat, it.long) }
                            }
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
            var status = sampleAPI.postUserData(UsernamePassword(to_check, password)).body()
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
            routeCreation = Route()
            getRoutes()
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
                start = Coordinate(position.latitude, position.longitude),
                end = Coordinate(activeRoute!!.end_lat, activeRoute!!.end_long),
                user = user!!
            )).body()
            sampleRoute = route_data!!.toSortedMap().values.map {GeoPoint(it.lat, it.long)}
        }
    }

    fun activateRoute(route: RouteInfo) {
        if (route == activeRoute) {
            activeRoute = null
            sampleRoute = emptyList()
            CoroutineScope(Dispatchers.IO).launch {
                sampleAPI.deactivateRoute(user!!)
            }
        } else {
            activeRoute = route
            getSampleData()
        }
    }
}