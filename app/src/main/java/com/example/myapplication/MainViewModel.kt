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
    var mainAPI: MainAPI = RetrofitHelper.getInstance().create(MainAPI::class.java)
    var toastText by mutableStateOf("")

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
                toastText = "Unknown Error"
            }
            Log.d("toast", toastText)
        }
    }
}