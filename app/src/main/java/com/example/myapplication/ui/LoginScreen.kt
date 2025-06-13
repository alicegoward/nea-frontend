package com.example.myapplication.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.AppDatabase
import com.example.myapplication.RouteViewModel
import com.example.myapplication.User
import com.example.myapplication.UserViewModel

/**
 * options to login or create an account
 * on successful login navigates to the main app and adds the received authentication token to the API
 * @param viewModel user view model
 * @param context application context
 * @param navController navigation
 * @param routeViewModel route view model
 */
@Composable
fun LoginScreen(viewModel: UserViewModel, context: Context, navController: NavController, routeViewModel: RouteViewModel) {
    var usernameAttempt by remember { mutableStateOf("")}
    var passwordAttempt by remember { mutableStateOf("")}
    val db = AppDatabase.getInstance(context)
    val userDao = db.UserDao()

    LaunchedEffect(Unit) {
        val credentials = userDao.getUserPass()
        if (credentials.isNotEmpty()) {
            viewModel.loginUser(credentials[0].username, credentials[0].password)
        }
    }

    if (viewModel.user != null) {
        LaunchedEffect(viewModel.user, viewModel.password) {
            routeViewModel.mainAPI = viewModel.mainAPI
            routeViewModel.user = viewModel.user
            userDao.deleteUser()
            userDao.insertUser(User(username = viewModel.user!!, password = viewModel.password!!))
            navController.navigate("mapScreen")
        }
    }
    displaySnackbar(viewModel)
    Column {
        Row(modifier = Modifier.fillMaxWidth().height(100.dp)) {
            FilledTonalButton (
                onClick = { viewModel.create_account = false },
                Modifier.weight(1F).fillMaxHeight(),
                shape = RoundedCornerShape(0.dp)
            ) {
                Text("Login", Modifier.align(Alignment.Bottom).padding(10.dp), fontSize = 16.sp)
            }
            FilledTonalButton (
                onClick = { viewModel.create_account = true },
                Modifier.weight(1F).fillMaxHeight(),
                shape = RoundedCornerShape(0.dp)
            ) {
                Text("Create account",Modifier.align(Alignment.Bottom).padding(10.dp), fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(30.dp))
        Text("Username", Modifier.padding(horizontal = 50.dp))
        Spacer(Modifier.height(5.dp))
        TextField(
            modifier = Modifier.padding(horizontal = 50.dp).fillMaxWidth(),
            value = usernameAttempt,
            onValueChange = { usernameAttempt = it },
            label = { Text("Username") },
        )
        Spacer(Modifier.height(30.dp))
        Text("Password", Modifier.padding(horizontal = 50.dp))
        Spacer(Modifier.height(5.dp))
        TextField(
            modifier = Modifier.padding(horizontal = 50.dp).fillMaxWidth(),
            value = passwordAttempt,
            onValueChange = { passwordAttempt = it },
            label = { Text("Password") },
        )
        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth().padding(50.dp),
            onClick = {
                if (viewModel.create_account) {
                    viewModel.createUser(usernameAttempt, passwordAttempt)
                } else {
                    viewModel.loginUser(usernameAttempt, passwordAttempt)
                }
            },
        ) {
            if (viewModel.create_account) {
                Text("Create account", fontSize = 16.sp)
            } else {
                Text("Login", fontSize = 16.sp)
            }
        }
        if (viewModel.error_text != null) {
            Text(viewModel.error_text!!, Modifier.align(Alignment.CenterHorizontally))
        }
    }
}