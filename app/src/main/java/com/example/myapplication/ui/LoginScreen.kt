package com.example.myapplication.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.example.myapplication.AppDatabase
import com.example.myapplication.MainViewModel
import com.example.myapplication.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(viewModel: MainViewModel, context: Context, gotoMain: ()->Unit) {
    var username_attempt by remember { mutableStateOf("")}
    var password_attempt by remember { mutableStateOf("")}
    val db = AppDatabase.getInstance(context)
    val userDao = db.UserDao()

    CoroutineScope(Dispatchers.IO).launch {
        val credentials = userDao.getUserPass()
        if (credentials.size == 1) {
            viewModel.loginUser(credentials[0].username, credentials[0].password)
        }
    }

    if (viewModel.user != null) {
        CoroutineScope(Dispatchers.IO).launch {
            userDao.deleteUser()
            Log.d("dao", userDao.getUserPass().toString())
            userDao.insertUser(User(username = viewModel.user!!, password = viewModel.password!!))
        }
        Log.d("user", viewModel.user!!)
        LaunchedEffect(true) { gotoMain() }
    }
    Row(modifier = Modifier.padding(top=60.dp, start=30.dp)) {
        Button(
            onClick = { viewModel.create_account = false }
        ) {
            Text("Login")
        }
        Button(
            onClick = { viewModel.create_account = true },
        ) {
            Text("Create account")
        }
    }
    Column(modifier = Modifier.padding(top=120.dp, start=30.dp)) {
        TextField(
            value = username_attempt,
            onValueChange = { username_attempt = it },
            label = { Text("Username") },
        )
        TextField(
            value = password_attempt,
            onValueChange = { password_attempt = it },
            label = { Text("Password") },
        )
        Button(
            onClick = {
                if (viewModel.create_account) {
                    viewModel.createUser(username_attempt, password_attempt)
                } else {
                    viewModel.loginUser(username_attempt, password_attempt)
                }
            },
        ) {
            if (viewModel.create_account) {
                Text("Create account")
            } else {
                Text("Login")
            }
        }
        if (viewModel.error_text != null) {
            Text(viewModel.error_text!!)
        }
    }
}