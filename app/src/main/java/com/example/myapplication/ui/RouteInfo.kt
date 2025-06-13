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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

/**
 * displays information about the route, with options to modify for the admin
 * @param route information about the route
 * @param viewModel user view model
 * @param navController navigation
 */
@Composable
fun RouteInfo(route: RouteInfo, viewModel: UserViewModel, navController: NavController) {
    outerScaffold(navController, viewModel) {
        Column(modifier = Modifier.padding(60.dp)) {
            Text(route.name, fontSize = 30.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Route participants:", fontSize = 20.sp)
                Spacer(Modifier.weight(1f))
                if (route.admin == viewModel.user) {
                    TextButton(
                        content = { Text("Edit") },
                        onClick = { navController.navigate("edit_participants/${route.name}") }
                    )
                }
            }
            Text(route.admin + " (route admin)")
            for (member: String in route.participants.values) {
                if (member != route.admin) Text(member)
            }
            Text("Meeting details:", fontSize = 20.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(route.junctionName)
                if (route.admin == viewModel.user) {
                    Spacer(Modifier.weight(1f))
                    TextButton(
                        content = { Text("Edit") },
                        onClick = { navController.navigate("edit_position/${route.name}") }
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "When: " + SimpleDateFormat(
                        "EEEE MMMM d\n HH:mm",
                        Locale.UK
                    ).format(Date(route.endTime * 1000))
                )
                if (route.admin == viewModel.user) {
                    Spacer(Modifier.weight(1f))
                    TextButton (
                        content = { Text("Edit") },
                        onClick = { navController.navigate("edit_time/${route.name}") }
                    )
                }
            }
            FilledTonalButton (
                content = { Text("Configure route behaviour") },
                onClick = { navController.navigate("notification_config/${route.name}") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

/**
 * stores data for each individual configuration choice the user can make
 * @param question question for the user to answer
 * @param options possible options
 * @param initialState default (or previously selected) option
 */
class Choice(private val question: String, private val options: List<String>, private val initialState: String) {
    lateinit var selected: MutableState<String>

    /**
     * required as constructor cannot be composable
     */
    @Composable
    fun init() { selected = remember { mutableStateOf(initialState) } }

    /**
     * displays the choice and stores the value when it changes
     */
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

/**
 * displays options for the user's meet on route / notification settings
 * @param viewModel route view model
 * @param routeName name of route
 * @param navController navigation
 */
@Composable
fun IndividualConfig(viewModel: RouteViewModel, routeName: String, navController: NavController) {
    val route = viewModel.routes.find {it.name == routeName}!!
    outerScaffold(navController, viewModel) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(60.dp)) {
            val createMeetOnRoute = Choice(
                "Auto create meet on route?",
                listOf("Yes", "No"),
                route.createMOR
            ).apply { init() }
            val meetOnRouteTolerance = Choice(
                "Maximum detour + wait time for meet on route:",
                listOf(1, 2, 5, 10).map { i -> "$i minutes" },
                route.morToleranceS
            ).apply { init() }
            val rerouteTolerance = Choice(
                "How many metres before rerouting if you go off course?",
                listOf(20, 50, 100, 200).map { i -> "$i m" },
                route.rerouteToleranceM
            ).apply { init() }
            val autoNotifyLate = Choice(
                "After you are late by how much should everyone else be notified?",
                listOf(2, 5, 10, 20).map { i -> "$i minutes" },
                route.autoNotifyLate
            ).apply { init() }

            createMeetOnRoute.display()
            if (createMeetOnRoute.selected.value == "Yes") {
                meetOnRouteTolerance.display()
            }
            rerouteTolerance.display()
            autoNotifyLate.display()
            Spacer(Modifier.weight(1F))
            Row {
                Button(
                    content = { Text("save and go back") },
                    onClick = {
                        route.createMOR = createMeetOnRoute.selected.value
                        route.morToleranceS = meetOnRouteTolerance.selected.value
                        route.rerouteToleranceM = rerouteTolerance.selected.value
                        viewModel.setIndividualConfig(route)
                        navController.navigate("route_info/$routeName")
                    }
                )
                Button(
                    content = { Text("cancel") },
                    onClick = { navController.navigate("route_info/$routeName") }
                )
            }
        }
    }
}

/**
 * wrapper for the edit participants function in create route, this time in the context of editing the route
 * @param routeName name of route
 * @param viewModel route view model
 * @param navController navigation
 * @param userViewModel user view model
 */
@Composable
fun editParticipants(routeName: String, viewModel: RouteViewModel, navController: NavController, userViewModel: UserViewModel) {
    var route: RouteInfo? = null
    for (possibleRoute in viewModel.routes) {
        if (possibleRoute.name == routeName) route = possibleRoute
    }
    viewModel.routeCreation = Route(GeoPoint(route!!.endLat, route.endLong))
    viewModel.routeCreation.friends = mutableStateListOf<String>().apply { addAll(route.participants.values) }
    outerScaffold(navController, viewModel) {
        Column(modifier = Modifier.padding(60.dp)) {
            start(viewModel, userViewModel, name = false)
            Spacer(Modifier.weight(1F))
            Row {
                Button(
                    content = { Text("Save") },
                    onClick = {
                        val out = hashMapOf("route" to routeName)
                        viewModel.routeCreation.friends.forEachIndexed { idx, item ->
                            out[idx.toString()] = item
                        }
                        viewModel.updateRouteInfo(out)
                        navController.navigate("route_info/${routeName}")
                    }
                )
                Spacer(Modifier.weight(1F))
                Button(
                    content = { Text("Cancel") },
                    onClick = {
                        navController.navigate("route_info/${routeName}")
                    }
                )
            }
        }
    }
}

/**
 * wrapper for the edit time function in create route, this time in the context of editing the route
 * @param routeName name of route
 * @param viewModel route view model
 * @param navController navigation
 */
@Composable
fun editTime(routeName: String, viewModel: RouteViewModel, navController: NavController) {
    var route: RouteInfo? = null
    for (possibleRoute in viewModel.routes) {
        if (possibleRoute.name == routeName) route = possibleRoute
    }
    viewModel.routeCreation = Route(viewModel.position)
    viewModel.routeCreation.endTime = route!!.endTime.toInt()
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
                                "route" to routeName
                            )
                        )
                        navController.navigate("route_info/${routeName}")
                    }
                )
                Spacer(Modifier.weight(1F))
                Button(
                    content = { Text("Cancel") },
                    onClick = {
                        navController.navigate("route_info/${routeName}")
                    }
                )
            }
        }
    }
}

/**
 * wrapper for the edit position function in create route, this time in the context of editing the route
 * @param routeName name of route
 * @param context application context
 * @param viewModel route view model
 * @param navController navigation
 */
@Composable
fun editPosition(routeName: String, context: Context, viewModel: RouteViewModel, navController: NavController) {
    var route: RouteInfo? = null
    for (possibleRoute in viewModel.routes) {
        if (possibleRoute.name == routeName) route = possibleRoute
    }
    viewModel.routeCreation = Route(GeoPoint(route!!.endLat, route.endLong))
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
                                "route" to routeName
                            )
                        )
                        navController.navigate("route_info/${routeName}")
                    }
                )
                Spacer(Modifier.weight(1F))
                Button(
                    content = { Text("Cancel") },
                    onClick = {
                        navController.navigate("route_info/${routeName}")
                    }
                )
            }
        }
    }
}