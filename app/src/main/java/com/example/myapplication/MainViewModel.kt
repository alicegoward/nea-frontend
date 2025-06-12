package com.example.myapplication

import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

open class MainViewModel : ViewModel() {
    var user: String? by mutableStateOf(null)
    var password: String? by mutableStateOf(null)
    var mainAPI: MainAPI = RetrofitHelper.getInstance().create(MainAPI::class.java)
    var toastText by mutableStateOf("")
    var error_text: String? by mutableStateOf(null)

    fun createUser(to_check: String, password: String) {
        getData {
            val status = mainAPI.postUserData(UsernamePassword(to_check, password))
            Log.d("user create status", status.toString())
            if (status.code() == 200) {
                user = to_check
                this.password = password
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
                TokenManager.token = user_data.body()!!.token
                mainAPI = RetrofitHelper.getInstance().create(MainAPI::class.java)
                user = to_check
                this.password = password
                Log.d("user", user.toString())
            } else {
                error_text = user_data.body()?.reason
                Log.e("login", error_text.toString())
            }
        }
    }

    fun getData(doStuff: suspend () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            doStuff()
        }
    }
}