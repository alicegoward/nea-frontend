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
    var outgoingRequests: MutableCollection<String>? by mutableStateOf(null)
    var pendingRequests: MutableCollection<String>? by mutableStateOf(null)
    var createAccount by mutableStateOf(false)
    var errorAddFriend: String? by mutableStateOf(null)

    /**
     * Gets the user's list of incoming, outgoing and full friends
     */
    fun getFriends() {
        getData {
            if (user != null) {
                val data = mainAPI.getFriends().body()
                Log.d("maybe some data", data.toString())
                friends = data?.names?.values
                pendingRequests = data?.incoming?.values
                outgoingRequests = data?.outgoing?.values
            }
        }
    }

    /**
     * @param request user's request to update the status for
     * @param state whether the request has been accepted or declined
     */
    fun updateRequest(request: String, state: String) {
        getData {
            mainAPI.updateRequest(UpdateRequest(user!!, request, state))
            this@UserViewModel.getFriends()
        }
    }

    /**
     * @param toAdd friend to send request to
     */
    fun addFriend(toAdd: String) {
        getData {
            val data = mainAPI.addFriend(
                AddFriend(
                    userRequesting = user!!,
                    userRequested = toAdd
                )
            )
            Log.d("friend data", data.body().toString() + data.code())
            errorAddFriend = data.body()?.reason
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