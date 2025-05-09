package com.example.myapplication

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat


fun checkPermission(context: Context, permission: String): Boolean {
    return (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
}

fun checkPermissions(clicked : Boolean,
                     singlePermission: ActivityResultLauncher<String>,
                     multiplePermission: ActivityResultLauncher<Array<String>>,
                     context: Context,
                     viewModel: MainViewModel) {
    if (!checkPermission(context, Manifest.permission.POST_NOTIFICATIONS)) {
        viewModel.buttonText = "Notification permission"
        if( clicked ) {
            singlePermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    } else if (!checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
        viewModel.buttonText = "Location permission"
        if( clicked ) {
            multiplePermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        }
    } else if (!checkPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
        viewModel.buttonText = "Also background permission"
        if (clicked) {
            singlePermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    } else {
        viewModel.buttonText="Permissions granted"
        viewModel.permissionsDone = true
    }
}

fun isServiceRunning(context: Context): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
        if (TestService::class.java.name == service.service.className) {
            return true
        }
    }
    return false
}