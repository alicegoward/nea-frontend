package com.example.myapplication.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.TestViewModel

@Composable
fun FriendScreen(viewModel: TestViewModel, context: Context, gotoMap: () -> Unit) {
    viewModel.getFriends()
    var current_friend by remember {mutableStateOf("")}
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (viewModel.friends != null) {
            for (i in viewModel.friends!!) {
                Row(horizontalArrangement = Arrangement.Start) { Text(i) }
            }
        }
        Spacer(modifier = Modifier.weight(1F))
        TextField(
            value = current_friend,
            onValueChange = { current_friend = it },
            label = { Text("Username") },
        )
        Button(
            onClick = { viewModel.addFriend(current_friend) },
            content = { Text("Add friend") }
        )
        if (viewModel.error_add_friend != null) {
            Text(viewModel.error_add_friend!!)
        } else { Text("") }
        Button(
            onClick = { gotoMap() },
            content = { Text("Map") }
        )
    }
}