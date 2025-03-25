package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.myapplication.ui.Navigation
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig


class MainActivity : ComponentActivity() {
    private lateinit var multiplePermission: ActivityResultLauncher<Array<String>>
    private lateinit var singlePermission: ActivityResultLauncher<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        val viewModel : TestViewModel by viewModels()
        Configuration.getInstance().userAgentValue = BuildConfig.LIBRARY_PACKAGE_NAME
        multiplePermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            checkPermissions(true, singlePermission, multiplePermission, this, viewModel)
        }
        singlePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            checkPermissions(true, singlePermission, multiplePermission, this, viewModel)
        }
        super.onCreate(savedInstanceState)
        checkPermissions(false, singlePermission, multiplePermission, this, viewModel)
        enableEdgeToEdge()
        startService(Intent(this, NotificationService::class.java))
        setContent{
            Navigation(this, viewModel, singlePermission, multiplePermission)
        }
    }
}