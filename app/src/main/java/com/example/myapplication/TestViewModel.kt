package com.example.myapplication

import android.os.Looper
import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class TestViewModel : ViewModel() {
    var permissionsDone by mutableStateOf(false)
    var buttonText by mutableStateOf("")
    var running: Boolean by mutableStateOf(false)
    val sampleAPI: SampleAPI = RetrofitHelper.getInstance().create(SampleAPI::class.java)
    var data by mutableStateOf("help")
    var create_account by mutableStateOf(false)
    var user: String? by mutableStateOf(null)
    var error_text: String? by mutableStateOf(null)
    init {
        Log.d("aaaa", "view model")
    }

    fun getData() {
        CoroutineScope(Dispatchers.IO).launch {
            sampleAPI.getData(Random.nextInt(1,5).toString()).body().toString()
            Log.d("aa", data)
        }
    }
    lateinit var locationCallback: LocationCallback
    var locationText by  mutableStateOf("unknown location")
    fun getLocation(fusedLocationProviderClient: FusedLocationProviderClient) {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations){
                    locationText =
                        location.latitude.toString() + " " + location.longitude.toString()
                }
            }
        }
        val locationRequest = LocationRequest.Builder(5000).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())
    }

    fun createUser(to_check: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            var status = sampleAPI.postUserData(user_data_model(to_check, password)).body()
            Log.d("user create status", status.toString())
            if (status?.success == true) {
                user = to_check
            } else {
                error_text = status?.reason
                Log.e("create account", error_text.toString())
            }
        }
    }

    fun loginUser(to_check: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            var user_data = sampleAPI.getUserData(to_check, password).body()
            Log.d("data", user_data.toString())
            if (user_data?.success == true) {
                user = to_check
            } else {
                error_text = user_data?.reason
                Log.e("login", error_text.toString())
            }
        }
    }
}