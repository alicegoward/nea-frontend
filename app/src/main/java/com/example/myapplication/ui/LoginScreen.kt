package com.example.myapplication.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.TestViewModel

@Composable
fun LoginScreen(viewModel: TestViewModel, context: Context, gotoMain: ()->Unit) {

    var text by remember { mutableStateOf("test")}
    if (viewModel.user != null) {
        Log.d("user", viewModel.user!!)
        LaunchedEffect(true) { gotoMain() }
    }
    TextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("Label") },
        modifier = Modifier.padding(60.dp)
    )
    Button(
        onClick = { viewModel.checkUser(text) },
        modifier = Modifier.padding(120.dp)
    ) {
        Text("Login")
    }
}