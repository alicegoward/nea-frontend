package com.example.myapplication

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.myapplication.RetrofitHelper.createClient
import com.example.myapplication.RetrofitHelper.okHttpClient

class UserViewModel: MainViewModel() {
    var friends: MutableCollection<String>? by mutableStateOf(null)
    var outgoing_requests: MutableCollection<String>? by mutableStateOf(null)
    var pending_requests: MutableCollection<String>? by mutableStateOf(null)
    var create_account by mutableStateOf(false)
    var user: String? by mutableStateOf(null)
    var password: String? by mutableStateOf(null)
    var error_text: String? by mutableStateOf(null)
    var error_add_friend: String? by mutableStateOf(null)
    fun createUser(to_check: String, password: String) {
        getData {
            val status = mainAPI.postUserData(UsernamePassword(to_check, password))
            Log.d("user create status", status.toString())
            if (status.code() == 200) {
                user = to_check
            } else {
                error_text = status.body()?.reason
                Log.e("create account", error_text.toString())
            }
        }
    }

    fun loginUser(to_check: String, password: String) {
        getData {
            val user_data = mainAPI.loginUser(UsernamePassword(to_check, password))
            Log.d("data", user_data.body().toString())
            if (user_data.code() == 200) {
                okHttpClient = createClient(user_data.body()!!.token)
                mainAPI = RetrofitHelper.getInstance().create(MainAPI::class.java)
                user = to_check
                this.password = password
            } else {
                error_text = user_data.body()?.reason
                Log.e("login", error_text.toString())
            }
        }
    }

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


    fun update_request(request: String, state: String) {
        getData {
            mainAPI.updateRequest(UpdateRequest(user!!, request, state))
            this@UserViewModel.getFriends()
        }
    }


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
}