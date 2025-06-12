package com.example.myapplication

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * view model that aspects user-related aspects of the application
 */
class UserViewModel: MainViewModel() {
    var friends: MutableCollection<String>? by mutableStateOf(null)
    var outgoing_requests: MutableCollection<String>? by mutableStateOf(null)
    var pending_requests: MutableCollection<String>? by mutableStateOf(null)
    var create_account by mutableStateOf(false)
    var error_add_friend: String? by mutableStateOf(null)

    /**
     * Gets the user's list of incoming, outgoing and full friends
     */
    fun getFriends() {
        getData {
            if (user != null) {
                val data = mainAPI.getFriends().body()
                Log.d("maybe some data", data.toString())
                friends = data?.names?.values
                pending_requests = data?.incoming?.values
                outgoing_requests = data?.outgoing?.values
            }
        }
    }

    /**
     * @param request user's request to update the status for
     * @param state whether the request has been accepted or declined
     */
    fun update_request(request: String, state: String) {
        getData {
            mainAPI.updateRequest(UpdateRequest(user!!, request, state))
            this@UserViewModel.getFriends()
        }
    }

    /**
     * @param to_add friend to send request to
     */
    fun addFriend(to_add: String) {
        getData {
            val data = mainAPI.addFriend(
                AddFriend(
                    userRequesting = user!!,
                    userRequested = to_add
                )
            )
            Log.d("friend data", data.body().toString() + data.code())
            error_add_friend = data.body()?.reason
            getFriends()
        }
    }

    /**
     * logs user out of view model
     */
    fun logout() {
        user=null
        password = null
        friends = null
    }
}