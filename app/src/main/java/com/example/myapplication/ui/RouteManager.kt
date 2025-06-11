package com.example.myapplication.ui

import RouteInfo
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.LogLocation
import com.example.myapplication.RouteViewModel
import com.example.myapplication.isServiceRunning
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

@Composable
fun RouteManager(viewModel: RouteViewModel, context: Context, navController: NavController) {
    if (viewModel.routes == emptyList<RouteInfo>()) {
        viewModel.getRoutes()
    }
    if ((viewModel.activeRoute != null) and !isServiceRunning(context)) {
        context.startService(Intent(context, LogLocation::class.java))
    }
    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    viewModel.getLocation(fusedLocationClient)
    outerScaffold(navController, viewModel) {
        Column(
            modifier = Modifier.padding(top=40.dp).verticalScroll(rememberScrollState())
        ) {
            Text("My routes", fontSize = 20.sp, modifier = Modifier.padding(vertical = 15.dp, horizontal=30.dp))
            for (route in viewModel.routes) {
                Row(modifier = Modifier.padding(horizontal = 30.dp, vertical = 15.dp)) {
                    Text(route.name)
                    Spacer(Modifier.weight(1F))
                    Column(horizontalAlignment = Alignment.End) {
                        Button(
                            onClick = {
                                if (viewModel.activeRoute == route) {
                                    context.stopService(Intent(context, LogLocation::class.java))
                                }
                                viewModel.activateRoute(route)
                            },
                            content = {
                                if (viewModel.activeRoute == route) Text("Deactivate")
                                else Text("Activate")
                            }
                        )
                        TextButton (
                            onClick = { navController.navigate("route_info/${route.name}") },
                            content = { Text("More info") }
                        )
                    }
                }
            }
            Button(modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { navController.navigate("create_route") },
                content = { Text("create route") })
        }
    }
}