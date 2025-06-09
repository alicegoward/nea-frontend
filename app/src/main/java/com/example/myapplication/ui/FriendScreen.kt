package com.example.myapplication.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.AppDatabase
import com.example.myapplication.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun FriendScreen(viewModel: UserViewModel, navController: NavController, context: Context) {
    var current_friend by remember {mutableStateOf("")}
    val coroutineScope = rememberCoroutineScope()
    outerScaffold(navController, viewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                coroutineScope.launch {
                    viewModel.logout()
                    val userDao = AppDatabase.getInstance(context).UserDao()
                    userDao.deleteUser()
                    navController.navigate("loginScreen")
                }
            }, content = { Text("Logout") })
            Text("Friends")
            if (viewModel.friends != null) {
                for (friend: String in viewModel.friends!!) {
                    Row { Text(friend) }
                }
            } else {
                viewModel.getFriends()
            }
            Text("Friend requests:")
            if (viewModel.pending_requests != null) {
                for (request: String in viewModel.pending_requests!!) {
                    Row {
                        Text(request)
                        Spacer(modifier = Modifier.weight(1F))
                        Button(
                            onClick = { viewModel.update_request(request, "accepted") },
                            content = { Text("Accept") }
                        )
                        Button(
                            onClick = { viewModel.update_request(request, "declined") },
                            content = { Text("Decline") }
                        )
                    }
                }
            }
            Text("Outgoing requests:")
            if (viewModel.outgoing_requests != null) {
                for (request: String in viewModel.outgoing_requests!!) {
                    Row { Text(request) }
                }
            }
            Spacer(modifier = Modifier.weight(1F))
            TextField(
                value = current_friend,
                onValueChange = { current_friend = it },
                label = { Text("Username") },
            )
            Button(
                onClick = {
                    viewModel.addFriend(current_friend)
                    current_friend = ""
                },
                content = { Text("Add friend") }
            )
            if (viewModel.error_add_friend != null) {
                Text(viewModel.error_add_friend!!)
            } else {
                Text("")
            }
        }
    }
}