package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


class MainActivity : ComponentActivity() {
    private var buttonText = "help"
    private lateinit var multiplePermission: ActivityResultLauncher<Array<String>>
    private lateinit var singlePermission: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        singlePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted ->
            run {
                Log.d("permission", isGranted.toString())
                checkPermissions(false)
            }
        }
        multiplePermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            isGranted ->
            run {
                Log.d("multi permission", isGranted.toString())
                checkPermissions(false)
            }
        }
        checkPermissions(false)
        enableEdgeToEdge()
        updateScreen();
    }
    private fun checkPermissions(clicked : Boolean) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            buttonText = "Notification permission"
            if( clicked ) {
                singlePermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            buttonText = "Location permission"
            if( clicked ) {
                multiplePermission.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                )
            }
        } else {
            buttonText="We have all our permissions :)"
            startForegroundService(Intent(this, TestService::class.java))
        }
        updateScreen();
    }

    private fun updateScreen() {
        setContent{
            PermissionScreen()
        }
    }
    @Composable
    fun PermissionScreen() {
        Button(content = {Text(text=buttonText)},
            onClick = {checkPermissions(true)},
            modifier = Modifier.padding(60.dp))
        val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        lateinit var locationCallback: LocationCallback
        var locationText by remember { mutableStateOf("unknown location") }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations){
                    locationText = location.latitude.toString()+" "+location.longitude.toString()
                }
            }
        }
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            LaunchedEffect(true) {
                val locationRequest = LocationRequest.Builder(5000).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
                fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())
            }
        }
        Text(text = locationText, modifier=Modifier.padding(150.dp))
    }
}