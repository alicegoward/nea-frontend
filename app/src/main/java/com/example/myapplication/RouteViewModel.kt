package com.example.myapplication

import Route
import RouteInfo
import android.annotation.SuppressLint
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import org.osmdroid.util.GeoPoint
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class RouteViewModel: MainViewModel() {
    var position by mutableStateOf(GeoPoint(0.0, 0.0))
    private lateinit var locationCallback: LocationCallback
    var routeCreation = Route(this.position)
    var routes: List<RouteInfo> by mutableStateOf(emptyList())
    var routePoints by mutableStateOf(emptyList<GeoPoint>())
    private var route_dist: Double? by mutableStateOf(null)
    var itineraries: MutableCollection<Itinerary>? by mutableStateOf(null)
    var other_routes by mutableStateOf(HashMap<String, List<GeoPoint>>())
    var waypoints: List<GeoPoint> by mutableStateOf(emptyList())
    var activeRoute: RouteInfo? by mutableStateOf(null)
    var permissionsDone by mutableStateOf(false)
    var buttonText by mutableStateOf("")

    fun saveRoute() {
        getData {
            mainAPI.postRoute(routeCreation.toRouteModel(user!!))
            Log.d("route","saved")
            routeCreation = Route(this@RouteViewModel.position)
            getRoutes()
        }
    }

    fun getRoutes() {
        getData {
            routes = mainAPI.getRoutes().body()!!.data.map { i -> RouteInfo(i.value) }
        }
    }

    fun activateRoute(route: RouteInfo) {
        if (route == activeRoute) {
            activeRoute = null
            itineraries = null
            getData {
                mainAPI.deactivateRoute()
            }
        } else {
            Log.d("activating route","here")
            getData {
                val route_data = mainAPI.activateRoute(Routing(
                    position = Coordinate(position.latitude, position.longitude),
                    datetime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                    route = route.name
                )).body()
                routePoints = route_data!!.geometry.toSortedMap().values.map { GeoPoint(it.lat, it.long) }
                route_data.other_paths.forEach { (user, path) ->
                    other_routes[user] = path.toSortedMap().values.map { GeoPoint(it.lat, it.long) }
                }
                waypoints = route_data.waypoints.toSortedMap().values.map { GeoPoint(it.lat, it.long) }
                Log.d("other routes", other_routes.toString())
                route_dist = route_data.dist
                itineraries = route_data.itineraries.values.toMutableList()
                activeRoute = route
            }
        }
    }
    fun updateRouteInfo(data: Map<String, String>) {
        getData {
            Log.d("have data", data.toString())
            mainAPI.updateRoute(data)
            this@RouteViewModel.getRoutes()
        }
    }

    fun setIndividualConfig(route: RouteInfo) {
        getData {
            mainAPI.setIndividualConfig(ConfigData(
                route.name,
                route.create_mor == "Yes",
                route.mor_tolerance_s.takeWhile { it.isDigit() }.toDouble() * 60,
                route.reroute_tolerance_m.takeWhile { it.isDigit() }.toDouble(),
                route.auto_notify_late.takeWhile { it.isDigit() }.toDouble() * 60,

                ))
            getIndividualConfig(route)
        }
    }

    fun getIndividualConfig(route: RouteInfo) {
        getData {
            val config = mainAPI.getIndividualConfig(UserOnRoute(route.name)).body()
            route.create_mor = if (config!!.create_mor) "Yes" else "No"
            route.mor_tolerance_s = "${(config.mor_tolerance_s/60).toInt()} minutes"
            route.reroute_tolerance_m = "${config.reroute_tolerance_m.toInt()} m"
            route.auto_notify_late = "${(config.auto_notify_late/60).toInt()} minutes"
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocation(fusedLocationProviderClient: FusedLocationProviderClient) {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                val location = p0.locations[p0.locations.size-1]
                if (activeRoute != null) {
                    getData {
                        val route_data = mainAPI.postData(
                            LocationData(
                                Instant.now().epochSecond,
                                Coordinate(location.latitude, location.longitude),
                            )
                        ).body()
                        if (route_data != null) {
                            routePoints = route_data.geometry.toSortedMap().values.map { GeoPoint(it.lat, it.long) }
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


    fun sendOthersNotification(meaning: String) {
        getData {
            if (meaning == "late") {
                mainAPI.notifyOthers(DataToNotify("$user is running late", activeRoute!!.name, user!!))
            } else {
                mainAPI.notifyOthers(DataToNotify("$user is lost", activeRoute!!.name, user!!))
            }
        }
    }
}