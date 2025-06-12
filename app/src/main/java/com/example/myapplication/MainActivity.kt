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

    /**
     * Creates the relevant view models, and creates permissions dialogues.
     * Runs navigation, which displays the relevant screen.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        val userViewModel : UserViewModel by viewModels()
        val routeViewModel : RouteViewModel by viewModels()
        Configuration.getInstance().userAgentValue = BuildConfig.LIBRARY_PACKAGE_NAME
        multiplePermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            checkPermissions(true, singlePermission, multiplePermission, this, routeViewModel)
        }
        singlePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            checkPermissions(true, singlePermission, multiplePermission, this, routeViewModel)
        }
        super.onCreate(savedInstanceState)
        checkPermissions(false, singlePermission, multiplePermission, this, routeViewModel)
        enableEdgeToEdge()
        startService(Intent(this, NotificationService::class.java))
        setContent{
            Navigation(this, userViewModel, routeViewModel, singlePermission, multiplePermission)
        }
    }
}