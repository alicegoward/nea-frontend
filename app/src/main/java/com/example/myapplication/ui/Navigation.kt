package com.example.myapplication.ui

import RouteInfo
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.RouteViewModel
import com.example.myapplication.UserViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme


@Composable
fun Navigation(context: Context,
               userViewModel: UserViewModel,
               routeViewModel: RouteViewModel,
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
                        routeViewModel,
                        context,
                        navController,
                    )
                }
                composable("loginScreen") {
                    LoginScreen(
                        userViewModel,
                        context,
                        navController,
                        routeViewModel)
                }
                composable("mapScreen") {
                    OsmdroidMapView(
                        routeViewModel,
                        context,
                        navController
                    )
                }
                composable("friendScreen") {
                    FriendScreen(
                        userViewModel,
                        navController
                    )
                }
                composable("routeManagerScreen") {
                    routeViewModel.getRoutes()
                    RouteManager(
                        routeViewModel,
                        context,
                        navController
                    )
                }
                composable("notification_config/{route_name}") { backStackEntry ->
                    val routeName = backStackEntry.arguments?.getString("route_name")!!
                    routeViewModel.getIndividualConfig(routeViewModel.routes.find {it.name == routeName}!!)
                    IndividualConfig(
                        routeViewModel,
                        routeName,
                        navController,
                    )
                }
                composable("edit_time/{route_name}") { backStackEntry ->
                    editTime(
                        backStackEntry.arguments?.getString("route_name")!!,
                        routeViewModel,
                        navController,
                    )
                }
                composable("edit_position/{route_name}") { backStackEntry ->
                    editPosition(
                        backStackEntry.arguments?.getString("route_name")!!,
                        context,
                        routeViewModel,
                        navController,
                    )
                }
                composable("edit_participants/{route_name}") { backStackEntry ->
                    editParticipants(
                        backStackEntry.arguments?.getString("route_name")!!,
                        routeViewModel,
                        navController,
                        userViewModel
                    )
                }
                composable("route_info/{route_name}") { backStackEntry ->
                    val routeName = backStackEntry.arguments?.getString("route_name")
                    var routeInfo: RouteInfo? = null
                    for (i in routeViewModel.routes) {
                        if (i.name == routeName) {
                            routeInfo = i
                        }
                    }
                    RouteInfo(
                        routeInfo!!,
                        userViewModel,
                        navController,
                    )
                }
                composable("create_route") {
                    CreateRoute(
                        routeViewModel,
                        userViewModel,
                        context,
                        navController
                    )
                }
            }
        }
    }
}
