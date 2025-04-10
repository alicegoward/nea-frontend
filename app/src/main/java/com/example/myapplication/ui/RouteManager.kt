package com.example.myapplication.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.RouteInfo
import com.example.myapplication.TestViewModel

@Composable
fun RouteManager(viewModel: TestViewModel, context: Context, navController: NavController) {
    if (viewModel.routes == emptyList<RouteInfo>()) {
        viewModel.getRoutes()
    }
    Column (modifier = Modifier
        .padding(60.dp)
        .verticalScroll(rememberScrollState())) {
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
                        onClick = { viewModel.activateRoute(route) },
                        content = {
                            if (viewModel.activeRoute == route) Text("deactivate")
                            else Text("activate")
                        }
                    )
                }
            }
        }
        Spacer(Modifier.weight(1F))
        Button(onClick = { navController.navigate("create_route") }, content = { Text("create route") })
    }
}