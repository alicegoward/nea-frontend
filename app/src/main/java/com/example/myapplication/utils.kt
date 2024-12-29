package com.example.myapplication

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService


fun checkPermission(context: Context, permission: String): Boolean {
    return (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
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