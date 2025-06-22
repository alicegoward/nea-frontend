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

/**
 * overall view model that the route and user view models inherit from
 * contains login and error handling functions, as well as information that should be shared
 * (although this must be manually shared between the two)
 */
open class MainViewModel : ViewModel() {
    var user: String? by mutableStateOf(null)
    var password: String? by mutableStateOf(null)
    var mainAPI: MainAPI = RetrofitHelper.getInstance().create(MainAPI::class.java)
    var toastText by mutableStateOf("")
    var errorText: String? by mutableStateOf(null)

    /**
     * attempts to register a user, displaying an error message if unsuccessful
     * @param toCheck user to attempt registration for
     * @param password user's password
     */
    fun createUser(toCheck: String, password: String) {
        getData {
            val status = mainAPI.postUserData(UsernamePassword(toCheck, password))
            Log.d("user create status", status.toString())
            if (status.code() == 200) {
                user = toCheck
                this.password = password
            } else {
                errorText = status.body()?.reason
                Log.e("create account", errorText.toString())
            }
        }
    }

    /**
     * Attempt to login user, saving a token if successful and outputting an error if not
     * @param toCheck username to attempt to login with
     * @param password password to attempt to login with
     */
    fun loginUser(toCheck: String, password: String) {
        getData {
            val userData = mainAPI.loginUser(UsernamePassword(toCheck, password))
            Log.d("data", userData.body().toString())
            if (userData.code() == 200) {
                TokenManager.token = userData.body()!!.token
                mainAPI = RetrofitHelper.getInstance().create(MainAPI::class.java)
                user = toCheck
                this.password = password
                Log.d("user", user.toString())
            } else {
                errorText = userData.body()?.reason
                Log.e("login", errorText.toString())
            }
        }
    }

    /**
     * Surrounds any API call(s) in a try/except block, outputting the error
     * @param doStuff code to run in the try/except block
     */
    fun getData(doStuff: suspend () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                doStuff()
                toastText = ""
            } catch (e: NullPointerException) {
                toastText = "Server Error"
            } catch (e: SocketTimeoutException) {
                toastText = "Network Error"
            } catch (e: Exception) {
                Log.d("error", e.toString())
                toastText = "Unknown Error"
            }
            Log.d("toast", toastText)
        }
    }
}