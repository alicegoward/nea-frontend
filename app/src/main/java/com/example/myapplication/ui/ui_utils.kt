package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun outerScaffold(navController: NavController, mainUI: @Composable () -> Unit) {
    Column {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            mainUI()
        }
        Box(Modifier.fillMaxWidth().background(Color.LightGray).padding(20.dp)) {
            Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    content = { Text("Friends") },
                    onClick = { navController.navigate("friendScreen") }
                )
                Spacer(Modifier.weight(1f))
                Button(
                    content = { Text("Map") },
                    onClick = { navController.navigate("mapScreen") }
                )
                Spacer(Modifier.weight(1f))
                Button(
                    content = { Text("Routes") },
                    onClick = { navController.navigate("routeManagerScreen") }
                )
            }
        }
    }
}