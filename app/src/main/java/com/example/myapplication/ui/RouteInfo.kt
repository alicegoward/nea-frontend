package com.example.myapplication.ui

import com.example.myapplication.Route
import com.example.myapplication.RouteInfo
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.RouteViewModel
import com.example.myapplication.UserViewModel
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RouteInfo(route: RouteInfo, viewModel: UserViewModel, navController: NavController) {
    outerScaffold(navController, viewModel) {
        Column(modifier = Modifier.padding(60.dp)) {
            Text(route.name, fontSize = 30.sp)
            Text("com.example.myapplication.Route participants:", fontSize = 20.sp)
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
                Text("Where: " + route.junction_name)
                if (route.admin == viewModel.user) {
                    Spacer(Modifier.weight(1f))
                    Button(
                        content = { Text("Edit") },
                        onClick = { navController.navigate("edit_position/${route.name}") }
                    )
                }
            }
            Row {
                Text(
                    "When: " + SimpleDateFormat(
                        "EEEE MMMM d HH:mm",
                        Locale.UK
                    ).format(Date(route.end_time * 1000))
                )
                if (route.admin == viewModel.user) {
                    Spacer(Modifier.weight(1f))
                    Button(
                        content = { Text("Edit") },
                        onClick = { navController.navigate("edit_time/${route.name}") }
                    )
                }
            }
            Button(
                content = { Text("Configure individual notification / meet on route behaviour") },
                onClick = { navController.navigate("notification_config/${route.name}") }
            )
            Spacer(Modifier.weight(1f))
            Button(
                content = { Text("go back") },
                onClick = { navController.navigate("routeManagerScreen") }
            )
        }
    }
}

class Choice(private val question: String, private val options: List<String>, private val initialState: String) {
    lateinit var selected: MutableState<String>
    @Composable
    fun init() { selected = remember { mutableStateOf(initialState) } }

    @Composable
    fun display() {
        Text(question)
        options.forEach { text ->
            Row(
                Modifier.fillMaxWidth().selectable(selected = (text == selected.value), onClick = { selected.value = text }, role = Role.RadioButton).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = (text == selected.value), onClick = null)
                Text(text)
            }
        }
    }
}

@Composable
fun IndividualConfig(viewModel: RouteViewModel, route_name: String, navController: NavController) {
    val route = viewModel.routes.find {it.name == route_name}!!
    outerScaffold(navController, viewModel) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(60.dp)) {
            val create_meet_on_route = Choice(
                "Auto create meet on route?",
                listOf("Yes", "No"),
                route.create_mor
            ).apply { init() }
            val meet_on_route_tolerance = Choice(
                "Maximum detour + wait time for meet on route:",
                listOf(1, 2, 5, 10).map { i -> "$i minutes" },
                route.mor_tolerance_s
            ).apply { init() }
            val reroute_tolerance = Choice(
                "How many metres before rerouting if you go off course?",
                listOf(20, 50, 100, 200).map { i -> "$i m" },
                route.reroute_tolerance_m
            ).apply { init() }
            val auto_notify_late = Choice(
                "After you are late by how much should everyone else be notified?",
                listOf(2, 5, 10, 20).map { i -> "$i minutes" },
                route.auto_notify_late
            ).apply { init() }

            create_meet_on_route.display()
            if (create_meet_on_route.selected.value == "Yes") {
                meet_on_route_tolerance.display()
            }
            reroute_tolerance.display()
            auto_notify_late.display()
            Spacer(Modifier.weight(1F))
            Row {
                Button(
                    content = { Text("save and go back") },
                    onClick = {
                        route.create_mor = create_meet_on_route.selected.value
                        route.mor_tolerance_s = meet_on_route_tolerance.selected.value
                        route.reroute_tolerance_m = reroute_tolerance.selected.value
                        viewModel.setIndividualConfig(route)
                        navController.navigate("route_info/$route_name")
                    }
                )
                Button(
                    content = { Text("cancel") },
                    onClick = { navController.navigate("route_info/$route_name") }
                )
            }
        }
    }
}

@Composable
fun editParticipants(route_name: String, viewModel: RouteViewModel, navController: NavController, userViewModel: UserViewModel) {
    var route: RouteInfo? = null
    for (possible_route in viewModel.routes) {
        if (possible_route.name == route_name) route = possible_route
    }
    viewModel.routeCreation = Route(GeoPoint(route!!.end_lat, route.end_long))
    viewModel.routeCreation.friends = mutableStateListOf<String>().apply { addAll(route.participants.values) }
    outerScaffold(navController, viewModel) {
        Column(modifier = Modifier.padding(60.dp)) {
            chooseFriends(viewModel, userViewModel)
            Spacer(Modifier.weight(1F))
            Row {
                Button(
                    content = { Text("Save") },
                    onClick = {
                        val out = hashMapOf("route" to route_name)
                        viewModel.routeCreation.friends.forEachIndexed { idx, item ->
                            out[idx.toString()] = item
                        }
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
}

@Composable
fun editTime(route_name: String, viewModel: RouteViewModel, navController: NavController) {
    var route: RouteInfo? = null
    for (possible_route in viewModel.routes) {
        if (possible_route.name == route_name) route = possible_route
    }
    viewModel.routeCreation = Route(viewModel.position)
    viewModel.routeCreation.endTime = route!!.end_time.toInt()
    outerScaffold(navController, viewModel) {
        Column(modifier = Modifier.padding(60.dp)) {
            chooseEndTime(viewModel)
            Spacer(Modifier.weight(1F))
            Row {
                Button(
                    content = { Text("Save") },
                    onClick = {
                        Log.d(
                            viewModel.routeCreation.endDate.toString(),
                            viewModel.routeCreation.endTime.toString()
                        )
                        viewModel.updateRouteInfo(
                            mapOf(
                                "time" to (viewModel.routeCreation.endDate!! + viewModel.routeCreation.endTime!!).toString(),
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
}

@Composable
fun editPosition(route_name: String, context: Context, viewModel: RouteViewModel, navController: NavController) {
    var route: RouteInfo? = null
    for (possible_route in viewModel.routes) {
        if (possible_route.name == route_name) route = possible_route
    }
    viewModel.routeCreation = Route(GeoPoint(route!!.end_lat, route.end_long))
    outerScaffold(navController, viewModel) {
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
}