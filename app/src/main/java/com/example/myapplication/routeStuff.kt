package com.example.myapplication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.osmdroid.util.GeoPoint

/**
 * Different states that the route creation can currently be
 */
enum class RouteState {
    START, CHOOSE_LOCATION, CHOOSE_END_TIME, END;

    /**
     * @return next route state
     */
    fun next(): RouteState {
        return when (this) {
            START -> CHOOSE_LOCATION
            CHOOSE_LOCATION -> CHOOSE_END_TIME
            CHOOSE_END_TIME -> END
            END -> START
        }
    }

    /**
     * @return previous route state
     */
    fun prev(): RouteState {
        return when (this) {
            START -> START
            CHOOSE_LOCATION -> START
            CHOOSE_END_TIME -> CHOOSE_LOCATION
            END -> CHOOSE_END_TIME
        }
    }
}

/**
 * route during creation
 */
class Route(endLocation: GeoPoint) {
    var state by mutableStateOf(RouteState.START)
    var friends = mutableStateListOf<String>()
    var endLocation by mutableStateOf(endLocation)
    var endDate: Long = java.time.Instant.now().epochSecond
    var endTime: Int = 0
    var name by mutableStateOf("")

    /**
     * @param username name of user creating the route
     * @return data about the route to send to the server
     */
    fun toRouteModel(username: String): RouteData {
        val friendDict = HashMap<Int, String>()
        friends.forEachIndexed { idx, item -> friendDict[idx] = item }
        return RouteData(
            endTime = endDate + endTime,
            lat = endLocation.latitude,
            long = endLocation.longitude,
            friends = friendDict,
            name = name,
            junction_name = "",
            username = username
        )
    }
}

/**
 * Various info about a route including individual configuration data
 */
data class RouteInfo(val routeData: RouteData) {
    val name = routeData.name
    val endLat = routeData.lat
    val endLong = routeData.long
    val admin = routeData.username
    val participants = routeData.friends
    val endTime = routeData.endTime
    val junctionName = routeData.junction_name
    var createMOR by mutableStateOf("No")
    var morToleranceS by mutableStateOf("10 minutes")
    var rerouteToleranceM by mutableStateOf("50 m")
    var autoNotifyLate by mutableStateOf("10 minutes")
}