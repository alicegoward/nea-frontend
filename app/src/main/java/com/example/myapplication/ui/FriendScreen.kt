package com.example.myapplication.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.AppDatabase
import com.example.myapplication.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun FriendScreen(viewModel: UserViewModel, navController: NavController, context: Context) {
    var currentFriend by remember {mutableStateOf("")}
    val coroutineScope = rememberCoroutineScope()
    outerScaffold(navController, viewModel) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                modifier = Modifier.padding(30.dp),
                onClick = {
                    coroutineScope.launch {
                        viewModel.logout()
                        val userDao = AppDatabase.getInstance(context).UserDao()
                        userDao.deleteUser()
                        navController.navigate("loginScreen")
                    }
                }
            ) {
                Text("Logout")
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 60.dp, horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text("Friend requests", fontSize = 20.sp)
            if (viewModel.pending_requests != null) {
                for (request: String in viewModel.pending_requests!!) {
                    Row(Modifier.fillMaxWidth(),  verticalAlignment = Alignment.CenterVertically) {
                        Text(request)
                        Spacer(modifier = Modifier.weight(1F))
                        TextButton (
                            onClick = { viewModel.update_request(request, "accepted") },
                            content = { Text("Accept") }
                        )
                        TextButton (
                            onClick = { viewModel.update_request(request, "declined") },
                            content = { Text("Decline") }
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("Outgoing requests", fontSize = 20.sp)
            if (viewModel.outgoing_requests != null) {
                for (request: String in viewModel.outgoing_requests!!) {
                    Row { Text(request) }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    modifier = Modifier.width(200.dp),
                    value = currentFriend,
                    onValueChange = { currentFriend = it },
                    label = { Text("Username") },
                )
                Spacer(Modifier.weight(1F))
                Button(
                    onClick = {
                        viewModel.addFriend(currentFriend)
                        currentFriend = ""
                    },
                    content = { Text("Send request") }
                )
            }
            if (viewModel.error_add_friend != null) {
                Text(viewModel.error_add_friend!!)
            } else {
                Text("")
            }
            Text("Friends", fontSize = 20.sp)
            if (viewModel.friends != null) {
                for (friend: String in viewModel.friends!!) {
                    Row { Text(friend) }
                }
            } else {
                viewModel.getFriends()
            }
        }
    }
}