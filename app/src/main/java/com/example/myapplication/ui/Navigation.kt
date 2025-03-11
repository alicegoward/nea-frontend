package com.example.myapplication.ui

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.TestViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme


@Composable
fun Navigation(context: Context,
               viewModel: TestViewModel,
               singlePermission: ActivityResultLauncher<String>,
               multiplePermission: ActivityResultLauncher<Array<String>>
) {
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