package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random


class MainActivity : ComponentActivity() {
    private var buttonText by mutableStateOf("help")
    private var data by mutableStateOf("Data")
    private lateinit var multiplePermission: ActivityResultLauncher<Array<String>>
    private lateinit var singlePermission: ActivityResultLauncher<String>
    private lateinit var navController: NavHostController
    private var permissionsDone by mutableStateOf(false)
    private val sampleAPI: SampleAPI = RetrofitHelper.getInstance().create(SampleAPI::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        singlePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            checkPermissions(true)
        }
        multiplePermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            checkPermissions(true)
        }
        checkPermissions(false)
        enableEdgeToEdge()
        setContent{
            Navigation()
        }
    }
    private fun checkPermissions(clicked : Boolean) {
        if (!checkPermission(this,Manifest.permission.POST_NOTIFICATIONS)) {
            buttonText = "Notification permission"
            if( clicked ) {
                singlePermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else if (!checkPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)) {
            buttonText = "Location permission"
            if( clicked ) {
                multiplePermission.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                )
            }
        } else if (!checkPermission(this,Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            buttonText = "Also background permission"
            if (clicked) {
                singlePermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        } else {
            buttonText="Permissions granted"
            permissionsDone = true
        }
    }
    @Composable
    fun Navigation() {
        MyApplicationTheme {
            Surface {
                navController = rememberNavController()
                Log.d("h","navigation initialised")
                var startScreen = "permissionScreen"
                if (permissionsDone) {
                    startScreen = "displayLocation"
                }
                NavHost(navController = navController, startDestination = startScreen) {
                    composable("permissionScreen") {
                        PermissionScreen(
                            gotoMain = {navController.navigate(("displayLocation"))},
                        )
                    }
                    composable("displayLocation") {
                        DisplayLocation()
                    }
                }
            }
        }
    }

    @Composable
    fun PermissionScreen(gotoMain: () -> Unit) {
        Button(content = {Text(text=buttonText)},
            onClick = {checkPermissions(true)},
            modifier = Modifier.padding(60.dp))
        if (permissionsDone) {
            LaunchedEffect(true) { gotoMain() }
        }
    }

    @Composable
    fun DisplayLocation() {
        val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        var running by remember { mutableStateOf(isServiceRunning(this@MainActivity)) }
        lateinit var locationCallback: LocationCallback
        var locationText by remember { mutableStateOf("unknown location") }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations){
                    locationText = location.latitude.toString()+" "+location.longitude.toString()
                }
            }
        }
        val locationRequest = LocationRequest.Builder(5000).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())
        Column(modifier = Modifier.padding(60.dp)) {
            Text(text = locationText, modifier = Modifier.padding(bottom = 16.dp))
            Button(
                onClick = {
                    if (!running) {
                        startForegroundService(Intent(this@MainActivity, TestService::class.java))
                    } else {
                        stopService(Intent(this@MainActivity, TestService::class.java))
                    }
                    running = !running
                },
                content = {
                    if (running) {
                        Text("Service running")
                    } else {
                        Text("Service not running")
                    }
                }
            )
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        data = sampleAPI.getData(Random.nextInt(200).toString()).body().toString()
                        Log.d("aa", data)
                    }
                },
                content = {
                    Text("Get data")
                }
            )
            Text(text = data, modifier = Modifier.padding(bottom = 16.dp))
        }
    }
}