package com.example.myapplication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.osmdroid.util.GeoPoint

enum class RouteState {
    START, CHOOSE_FRIENDS, CHOOSE_LOCATION, CHOOSE_END_TIME, END;
    fun next(): RouteState {
        return when (this) {
            START -> CHOOSE_FRIENDS
            CHOOSE_FRIENDS -> CHOOSE_LOCATION
            CHOOSE_LOCATION -> CHOOSE_END_TIME
            CHOOSE_END_TIME -> END
            END -> START
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
            name = name,
            junction_name = "",
            username = username
        )
    }
}

data class RouteInfo(val routeData: RouteData) {
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