package com.example.myapplication

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

/**
 * view model that handles route-related aspects of the application
 */
class RouteViewModel: MainViewModel() {
    var position by mutableStateOf(GeoPoint(0.0, 0.0))
    private lateinit var locationCallback: LocationCallback
    var routeCreation = Route(this.position)
    var routes: List<RouteInfo> by mutableStateOf(emptyList())
    var routePoints by mutableStateOf(emptyList<GeoPoint>())
    private var routeDist: Double? by mutableStateOf(null)
    var itineraries: MutableCollection<Itinerary>? by mutableStateOf(null)
    var otherRoutes by mutableStateOf(HashMap<String, List<GeoPoint>>())
    var waypoints: List<GeoPoint> by mutableStateOf(emptyList())
    var activeRoute: RouteInfo? by mutableStateOf(null)
    var permissionsDone by mutableStateOf(false)
    var buttonText by mutableStateOf("")

    /**
     * Saves the route currently being created to the server
     */
    fun saveRoute() {
        getData {
            Log.d("user", user.toString())
            mainAPI.postRoute(routeCreation.toRouteModel(user!!))
            Log.d("route","saved")
            routeCreation = Route(this@RouteViewModel.position)
            getRoutes()
        }
    }

    /**
     * Gets routes the user is a part of
     */
    fun getRoutes() {
        getData {
            routes = mainAPI.getRoutes().body()!!.data.map { i -> RouteInfo(i.value) }
        }
    }

    /**
     * Activates or deactivates the route, updating what is displayed on the map in the process
     * @param route route that is (de)activated
     */
    fun activateRoute(route: RouteInfo) {
        if (route == activeRoute) {
            activeRoute = null
            itineraries = null
            routePoints = emptyList()
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
                    otherRoutes[user] = path.toSortedMap().values.map { GeoPoint(it.lat, it.long) }
                }
                waypoints = route_data.waypoints.toSortedMap().values.map { GeoPoint(it.lat, it.long) }
                Log.d("other routes", otherRoutes.toString())
                routeDist = route_data.dist
                itineraries = route_data.itineraries.values.toMutableList()
                activeRoute = route
            }
        }
    }

    /**
     * Saves any changes to the route to the server
     * @param data route data to save
     */
    fun updateRouteInfo(data: Map<String, String>) {
        getData {
            Log.d("have data", data.toString())
            mainAPI.updateRoute(data)
            this@RouteViewModel.getRoutes()
        }
    }

    /**
     * sets the individual meeting and notification tolerances in the server
     * @param route route to get tolerances to save from
     */
    fun setIndividualConfig(route: RouteInfo) {
        getData {
            mainAPI.setIndividualConfig(ConfigData(
                route.name,
                route.createMOR == "Yes",
                route.morToleranceS.takeWhile { it.isDigit() }.toDouble() * 60,
                route.rerouteToleranceM.takeWhile { it.isDigit() }.toDouble(),
                route.autoNotifyLate.takeWhile { it.isDigit() }.toDouble() * 60,

                ))
            getIndividualConfig(route)
        }
    }

    /**
     * gets current meeting and notification tolerances from the server
     * @param route route to get information about
     */
    fun getIndividualConfig(route: RouteInfo) {
        getData {
            val config = mainAPI.getIndividualConfig(UserOnRoute(route.name)).body()
            route.createMOR = if (config!!.create_mor) "Yes" else "No"
            route.morToleranceS = "${(config.mor_tolerance_s/60).toInt()} minutes"
            route.rerouteToleranceM = "${config.reroute_tolerance_m.toInt()} m"
            route.autoNotifyLate = "${(config.auto_notify_late/60).toInt()} minutes"
        }
    }

    /**
     * Starts location callbacks, which update the user interface and send the new datapoint to the server
     * @param fusedLocationProviderClient link to Android's location API
     */
    @SuppressLint("MissingPermission")
    fun getLocation(fusedLocationProviderClient: FusedLocationProviderClient) {
        locationCallback = object : LocationCallback() {
            private var lastPostTime = 0L
            override fun onLocationResult(p0: LocationResult) {
                val location = p0.lastLocation ?: return
                val now = System.currentTimeMillis()
                position = GeoPoint(location.latitude, location.longitude)
                if (activeRoute != null && now - lastPostTime >= 5000) {
                    lastPostTime = now
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
                                otherRoutes[user] = path.toSortedMap().values.map { GeoPoint(it.lat, it.long) }
                            }
                            Log.d("stuff", route_data.toString())
                            waypoints = route_data.waypoints.values.toList().map { GeoPoint(it.lat, it.long) }
                            routeDist = route_data.dist
                            itineraries = route_data.itineraries.values.toMutableList()
                        }
                    }
                }
                position = GeoPoint(location.latitude, location.longitude)

            }
        }
        val locationRequest = LocationRequest.Builder(10000).setMinUpdateIntervalMillis(5000).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    /**
     * Manual notification to other users that the user may not make it on time
     * @param meaning whether the user is running late or is lost
     */
    fun sendOthersNotification(meaning: String) {
        getData {
            if (meaning == "late") {
                mainAPI.notifyOthers(DataToNotify("$user is running late", activeRoute!!.name, user!!))
            } else {
                mainAPI.notifyOthers(DataToNotify("$user is lost", activeRoute!!.name, user!!))
            }
        }
    }

    /**
     * Converts a list of people at a waypoint to text to display
     * @param people waypoint participants
     * @param person person who's itinerary it is
     */
    fun convertToParticipants(people: MutableList<String>, person: String): String {
        people.remove(person)
        var text = ""
        if (people.size > 1) {
            for (i in 0..<people.size - 1) {
                text += "${people[i]}, "
            }
            text = text.dropLast(2)
            text += " and ${people[people.size - 1]}"
        } else {
            text = people[0]
        }
        return text
    }
}