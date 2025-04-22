package com.example.myapplication.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.TestViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.Route
import com.example.myapplication.RouteInfo
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RouteInfo(route: RouteInfo, viewModel: TestViewModel, navController: NavController) {
    Column(modifier = Modifier.padding(60.dp)) {
        Text(route.name, fontSize = 30.sp)
        Text("Route participants:", fontSize = 20.sp)
        Text(route.admin + " (route admin)")
        for (member: String in route.participants.values) {
            Text(member)
        }
        if (route.admin == viewModel.user) {
            Button(
                content = { Text("Edit participants") },
                onClick = { navController.navigate("edit_participants/${route.name}") }
            )
        }
        Text("Meeting details:", fontSize = 20.sp)
        Row {
            Text("Where: "+route.junction_name)
            if (route.admin == viewModel.user) {
                Spacer(Modifier.weight(1f))
                Button(
                    content = { Text("Edit") },
                    onClick = { navController.navigate("edit_position/${route.name}") }
                )
            }
        }
        Row {
            Text("When: "+SimpleDateFormat("EEEE MMMM d HH:mm" ,Locale.UK).format(Date(route.end_time * 1000)))
            if (route.admin == viewModel.user) {
                Spacer(Modifier.weight(1f))
                Button(
                    content = { Text("Edit") },
                    onClick = { navController.navigate("edit_time/${route.name}") }
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            content= { Text("go back") },
            onClick = { navController.navigate("routeManagerScreen") }
        )
    }
}

@Composable
fun editParticipants(route_name: String, viewModel: TestViewModel, navController: NavController) {
    var route: RouteInfo? = null
    for (possible_route in viewModel.routes) {
        if (possible_route.name == route_name) route = possible_route
    }
    viewModel.routeCreation = Route(GeoPoint(route!!.end_lat, route.end_long))
    viewModel.routeCreation.friends = mutableStateListOf<String>().apply { addAll(route.participants.values) }
    Column(modifier = Modifier.padding(60.dp)) {
        chooseFriends(viewModel)
        Spacer(Modifier.weight(1F))
        Row {
            Button(
                content = { Text("Save") },
                onClick = {
                    val out = hashMapOf("route" to route_name)
                    viewModel.routeCreation.friends.forEachIndexed {idx, item -> out[idx.toString()] = item}
                    viewModel.updateRouteInfo(out)
                    navController.navigate("route_info/${route_name}")
                }
            )
            Spacer(Modifier.weight(1F))
            Button(
                content = { Text("Cancel") },
                onClick = {
                    navController.navigate("route_info/${route_name}")
                }
            )
        }
    }
}

@Composable
fun editTime(route_name: String, viewModel: TestViewModel, navController: NavController) {
    var route: RouteInfo? = null
    for (possible_route in viewModel.routes) {
        if (possible_route.name == route_name) route = possible_route
    }
    viewModel.routeCreation = Route(viewModel.position)
    viewModel.routeCreation.endTime = route!!.end_time.toInt()
    Column(modifier = Modifier.padding(60.dp)) {
        chooseEndTime(viewModel)
        Spacer(Modifier.weight(1F))
        Row {
            Button(
                content = { Text("Save") },
                onClick = {
                    Log.d(viewModel.routeCreation.endDate.toString(),
                        viewModel.routeCreation.endTime.toString()
                    )
                    viewModel.updateRouteInfo(
                        mapOf(
                            "time" to (viewModel.routeCreation.endDate!! + viewModel.routeCreation.endTime!!).toString(),
                            "route" to route_name)
                    )
                    navController.navigate("route_info/${route_name}")
                }
            )
            Spacer(Modifier.weight(1F))
            Button(
                content = { Text("Cancel") },
                onClick = {
                    navController.navigate("route_info/${route_name}")
                }
            )
        }
    }
}

@Composable
fun editPosition(route_name: String, context: Context, viewModel: TestViewModel, navController: NavController) {
    var route: RouteInfo? = null
    for (possible_route in viewModel.routes) {
        if (possible_route.name == route_name) route = possible_route
    }
    viewModel.routeCreation = Route(GeoPoint(route!!.end_lat, route.end_long))
    Column(modifier = Modifier.padding(60.dp)) {
        chooseLocation(viewModel, context)
        Spacer(Modifier.weight(1F))
        Row {
            Button(
                content = { Text("Save") },
                onClick = {
                    viewModel.updateRouteInfo(
                        mapOf(
                            "lat" to viewModel.routeCreation.endLocation.latitude.toString(),
                            "long" to viewModel.routeCreation.endLocation.longitude.toString(),
                            "route" to route_name
                        )
                    )
                    navController.navigate("route_info/${route_name}")
                }
            )
            Spacer(Modifier.weight(1F))
            Button(
                content = { Text("Cancel") },
                onClick = {
                    navController.navigate("route_info/${route_name}")
                }
            )
        }
    }
}