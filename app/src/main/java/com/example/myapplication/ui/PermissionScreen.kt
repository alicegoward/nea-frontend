package com.example.myapplication.ui

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.MainViewModel
import com.example.myapplication.RouteViewModel
import com.example.myapplication.UserViewModel
import com.example.myapplication.checkPermissions

/**
 * Screen that launches various permissions (should be shown briefly, if at all)
 * @param singlePermission launcher for one permission
 * @param multiplePermission launcher for multiple permissions
 * @param viewModel route view model
 * @param context application context
 * @param navController navigation
 */
@Composable
fun PermissionScreen(singlePermission: ActivityResultLauncher<String>,
                     multiplePermission: ActivityResultLauncher<Array<String>>,
                     viewModel: RouteViewModel,
                     context: Context,
                     navController: NavController) {
    checkPermissions(true, singlePermission, multiplePermission, context, viewModel)
    Button(content = { Text(text=viewModel.buttonText) },
        onClick = {
            checkPermissions(true, singlePermission, multiplePermission, context, viewModel)
        },
        modifier = Modifier.padding(60.dp))
    if (viewModel.permissionsDone) {
        LaunchedEffect(true) { navController.navigate("loginScreen") }
    }
}