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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            modifier = Modifier.padding(60.dp).verticalScroll(rememberScrollState())
        ) {
            Text("My routes")
            for (route in viewModel.routes) {
                Row(modifier = Modifier.padding(vertical = 30.dp)) {
                    Text(route.name)
                    Spacer(Modifier.weight(1F))
                    Column {
                        Button(
                            onClick = { navController.navigate("route_info/${route.name}") },
                            content = { Text("more info") }
                        )
                        Button(
                            onClick = {
                                if (viewModel.activeRoute == route) {
                                    context.stopService(Intent(context, LogLocation::class.java))
                                }
                                viewModel.activateRoute(route)
                            },
                            content = {
                                if (viewModel.activeRoute == route) Text("deactivate")
                                else Text("activate")
                            }
                        )
                    }
                }
            }
            Button(
                onClick = { navController.navigate("create_route") },
                content = { Text("create route") })
        }
    }
}