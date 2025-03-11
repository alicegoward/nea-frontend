package com.example.myapplication.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.TestService
import com.example.myapplication.TestViewModel
import com.example.myapplication.isServiceRunning
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

@Composable
fun DisplayLocation(viewModel: TestViewModel, context: Context) {
    val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    var running by remember { mutableStateOf(isServiceRunning(context)) }
    viewModel.getLocation(fusedLocationProviderClient)
    Column(modifier = Modifier.padding(60.dp)) {
        Text(text = viewModel.locationText, modifier = Modifier.padding(bottom = 16.dp))
        Button(
            onClick = {
                if (viewModel.running) {
                    context.startForegroundService(Intent(context, TestService::class.java))
                } else {
                    context.stopService(Intent(context, TestService::class.java))
                }
                viewModel.running=!viewModel.running
            },
            content = {
                if (running) { Text("Service running") }
                else { Text("Service not running") }
            }
        )
        Button(
            onClick = { viewModel.getData() },
            content = { Text("Get data") }
        )
        Text(text = viewModel.data, modifier = Modifier.padding(bottom = 16.dp))
    }
}