package com.example.myapplication.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.TestViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.navigation.NavController
import com.example.myapplication.RouteInfo


@Composable
fun RouteInfo(route: RouteInfo, viewModel: TestViewModel, navController: NavController) {
    Column(modifier = Modifier.padding(60.dp)) {
        Text(route.name)
        if (viewModel.user != route.admin) {
            Text(route.admin + " (owner)")
        }
        for (member: String in route.participants.values) {
            Text(member)
        }
        Text("end position "+route.end_lat+" - "+route.end_long)
        Text("end time "+route.end_time)
        Spacer(Modifier.weight(1f))
        Button(
            content={ Text("go back") },
            onClick = { navController.navigate("routeManagerScreen") }
        )
    }
}