package com.example.myapplication

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun checkPermission(context: Context, permission: String): Boolean {
    return (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
}