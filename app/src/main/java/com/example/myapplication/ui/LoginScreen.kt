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
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.TestViewModel

@Composable
fun LoginScreen(viewModel: TestViewModel, context: Context, gotoMain: ()->Unit) {

    if (viewModel.user != null) {
        Log.d("user", viewModel.user!!)
        LaunchedEffect(true) { gotoMain() }
    }
    Button(
        onClick = { viewModel.create_account = false }
    ) {
        Text("Login")
    }
    Button(
        onClick = { viewModel.create_account = true },
        modifier = Modifier.padding(50.dp)
    ) {
        Text("Create account")
    }
    if (viewModel.create_account) {
        createAccount(viewModel)
    } else {
        login(viewModel)
    }
}

@Composable
fun createAccount(viewModel: TestViewModel) {
    var username_attempt by remember { mutableStateOf("")}
    var password_attempt by remember { mutableStateOf("")}
    TextField(
        value = username_attempt,
        onValueChange = { username_attempt = it },
        label = { Text("Username") },
        modifier = Modifier.padding(60.dp)
    )
    TextField(
        value = password_attempt,
        onValueChange = { password_attempt = it },
        label = { Text("Password") },
        modifier = Modifier.padding(100.dp)
    )
    Button(
        onClick = { viewModel.createUser(username_attempt, password_attempt) },
        modifier = Modifier.padding(120.dp)
    ) {
        Text("Create account")
    }
}

@Composable
fun login(viewModel: TestViewModel) {
    var username_attempt by remember { mutableStateOf("")}
    var password_attempt by remember { mutableStateOf("")}
    TextField(
        value = username_attempt,
        onValueChange = { username_attempt = it },
        label = { Text("Username") },
        modifier = Modifier.padding(60.dp)
    )
    TextField(
        value = password_attempt,
        onValueChange = { password_attempt = it },
        label = { Text("Password") },
        modifier = Modifier.padding(90.dp)
    )
    Button(
        onClick = { viewModel.loginUser(username_attempt, password_attempt) },
        modifier = Modifier.padding(120.dp)
    ) {
        Text("Login")
    }
}