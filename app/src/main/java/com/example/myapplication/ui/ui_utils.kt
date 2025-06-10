package com.example.myapplication.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.MainViewModel
import com.example.myapplication.R

@Composable
fun outerScaffold(navController: NavController, viewModel: MainViewModel, mainUI: @Composable () -> Unit) {
    displaySnackbar(viewModel)
    Column {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            mainUI()
        }
        Box(
            Modifier.fillMaxWidth()) {
            Row(Modifier.height(75.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    content = { Text("Friends") },
                    onClick = { navController.navigate("friendScreen") },
                    modifier = Modifier.weight(1F).fillMaxSize(),
                    shape = RoundedCornerShape(0.dp)
                )
                Button (
                    content = { Text("Map") },
                    onClick = { navController.navigate("mapScreen") },
                    modifier = Modifier.weight(1F).fillMaxSize(),
                    shape = RoundedCornerShape(0.dp)
                )
                Button(
                    content = { Text("Routes") },
                    onClick = { navController.navigate("routeManagerScreen") },
                    modifier = Modifier.weight(1F).fillMaxSize(),
                    shape = RoundedCornerShape(0.dp)
                )
            }
        }
    }

}

@Composable
fun displaySnackbar(viewModel: MainViewModel) {
    if (viewModel.toastText != "") {
        Box(Modifier.fillMaxSize()) {
            Log.d("displaying snackbar", viewModel.toastText)
            Snackbar(Modifier.align(Alignment.BottomCenter)) {
                Text(viewModel.toastText, Modifier.padding(12.dp))
            }
        }
    }
}