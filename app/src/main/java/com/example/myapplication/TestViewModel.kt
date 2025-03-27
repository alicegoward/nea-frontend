package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Looper
import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class TestViewModel : ViewModel() {
    var friends: MutableCollection<String>? by mutableStateOf(null)
    var pending_requests: MutableCollection<String>? by mutableStateOf(null)
    var permissionsDone by mutableStateOf(false)
    var buttonText by mutableStateOf("")
    var running: Boolean by mutableStateOf(false)
    val sampleAPI: SampleAPI = RetrofitHelper.getInstance().create(SampleAPI::class.java)
    var data by mutableStateOf("help")
    var create_account by mutableStateOf(false)
    var user: String? by mutableStateOf(null)
    var error_text: String? by mutableStateOf(null)
    var error_add_friend: String? by mutableStateOf(null)
    var position by mutableStateOf(GeoPoint(0.0,0.0))
    lateinit var locationCallback: LocationCallback
    var locationText by  mutableStateOf("unknown location")
    @SuppressLint("MissingPermission")
    fun getLocation(fusedLocationProviderClient: FusedLocationProviderClient) {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations){
                    position = GeoPoint(location.latitude, location.longitude)
                    Log.d("currentlocation", position.toString())
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
            var status = sampleAPI.postUserData(UsernamePassword(to_check, password)).body()
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
            val user_data = sampleAPI.loginUser(UsernamePassword(to_check, password)).body()
            Log.d("data", user_data.toString())
            if (user_data?.success == true) {
                user = to_check
            } else {
                error_text = user_data?.reason
                Log.e("login", error_text.toString())
            }
        }
    }

    fun getFriends() {
        CoroutineScope(Dispatchers.IO).launch {
            if (user != null) {
                friends = sampleAPI.getFriends(user!!).body()?.names?.values
            }
        }
    }

    fun getPendingRequests() {
        CoroutineScope(Dispatchers.IO).launch {
            pending_requests = sampleAPI.getPendingRequests(user!!).body()?.names?.values
        }
    }

    fun update_request(request: String, state: String) {
        CoroutineScope(Dispatchers.IO).launch {
            sampleAPI.updateRequest(UpdateRequest(user!!, request, state))
            this@TestViewModel.getFriends()
            this@TestViewModel.getPendingRequests()
        }
    }


    fun addFriend(to_add: String) {
        CoroutineScope(Dispatchers.IO).launch {
            error_add_friend = sampleAPI.addFriend(AddFriend(
                userRequesting = user!!,
                userRequested = to_add
            )).body()?.reason
            getFriends()
        }
    }
}