package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random


class MainActivity : ComponentActivity() {
    lateinit var multiplePermission: ActivityResultLauncher<Array<String>>
    lateinit var singlePermission: ActivityResultLauncher<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        multiplePermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            checkPermissions(true, singlePermission, multiplePermission, this)
        }
        singlePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            checkPermissions(true, singlePermission, multiplePermission, this)
        }
        super.onCreate(savedInstanceState)
        val viewModel : TestViewModel by viewModels()
        checkPermissions(false, singlePermission, multiplePermission, this)
        enableEdgeToEdge()
        setContent{
            Navigation(this, viewModel, singlePermission, multiplePermission)
        }
    }
}

@Composable
fun Navigation(context: Context,
               viewModel: TestViewModel,
               singlePermission: ActivityResultLauncher<String>,
               multiplePermission: ActivityResultLauncher<Array<String>>) {
    MyApplicationTheme {
        Surface {
            val navController = rememberNavController()
            Log.d("h","navigation initialised")
            val startScreen = "permissionScreen"
            NavHost(navController = navController, startDestination = startScreen) {
                composable("permissionScreen") {
                    PermissionScreen(
                        singlePermission,
                        multiplePermission,
                        viewModel,
                        context = context,
                        gotoMain = {navController.navigate(("displayLocation"))},
                    )
                }
                composable("displayLocation") {
                    DisplayLocation(viewModel, context)
                }
            }
        }
    }
}


@Composable
fun PermissionScreen(singlePermission: ActivityResultLauncher<String>,
                     multiplePermission: ActivityResultLauncher<Array<String>>,
                     viewModel: TestViewModel,
                     context: Context,
                     gotoMain: () -> Unit) {
    var buttonText by mutableStateOf("help")
    var permissionsDone by mutableStateOf(false)
    val (buttonTextMessage, permissionsDoneCheck) = checkPermissions(true,
        singlePermission, multiplePermission, context)
    buttonText = buttonTextMessage;
    permissionsDone = permissionsDoneCheck;
    Button(content = {Text(text=buttonText)},
        onClick = {
            val (buttonTextMessage, permissionsDoneCheck) = checkPermissions(true,
                singlePermission, multiplePermission, context)
            buttonText = buttonTextMessage;
            permissionsDone = permissionsDoneCheck;
        },
        modifier = Modifier.padding(60.dp))
    if (permissionsDone) {
        LaunchedEffect(true) { gotoMain() }
    }
}

private fun checkPermissions(clicked : Boolean,
                             singlePermission: ActivityResultLauncher<String>,
                             multiplePermission: ActivityResultLauncher<Array<String>>,
                             context: Context) : Pair<String, Boolean> {
    var permissionsDone = false
    var buttonText = ""
    if (!checkPermission(context,Manifest.permission.POST_NOTIFICATIONS)) {
        buttonText = "Notification permission"
        if( clicked ) {
            singlePermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    } else if (!checkPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)) {
        buttonText = "Location permission"
        if( clicked ) {
            multiplePermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        }
    } else if (!checkPermission(context,Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
        buttonText = "Also background permission"
        if (clicked) {
            singlePermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    } else {
        buttonText="Permissions granted"
        permissionsDone = true
    }
    return Pair(buttonText, permissionsDone)
}

@Composable
fun DisplayLocation(viewModel: TestViewModel, context: Context) {
    val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    var running by remember { mutableStateOf(isServiceRunning(context)) }
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
                    context.startForegroundService(Intent(context, TestService::class.java))
                } else {
                    context.stopService(Intent(context, TestService::class.java))
                }
                running=!running
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
                viewModel.getData()
            },
            content = {
                Text("Get data")
            }
        )
        Text(text = viewModel.data, modifier = Modifier.padding(bottom = 16.dp))
    }
}