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
import com.example.myapplication.MainViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme


@Composable
fun Navigation(context: Context,
               viewModel: MainViewModel,
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
                        gotoLogin = {navController.navigate(("loginScreen"))},
                    )
                }
                composable("loginScreen") {
                    LoginScreen(viewModel,
                        context,
                        gotoMain = {navController.navigate(("mapScreen"))})
                }
                composable("mapScreen") {
                    OsmdroidMapView(
                        viewModel = viewModel,
                        context = context,
                        navController = navController
                    )
                }
                composable("friendScreen") {
                    FriendScreen(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
                composable("routeManagerScreen") {
                    viewModel.getRoutes()
                    RouteManager(
                        viewModel = viewModel,
                        context = context,
                        navController = navController
                    )
                }
                composable("notification_config/{route_name}") { backStackEntry ->
                    val route_name = backStackEntry.arguments?.getString("route_name")!!
                    viewModel.getIndividualConfig(viewModel.routes.find {it.name == route_name}!!)
                    IndividualConfig(
                        route_name =  route_name,
                        viewModel = viewModel,
                        navController = navController
                    )
                }
                composable("edit_time/{route_name}") { backStackEntry ->
                    editTime(
                        route_name =  backStackEntry.arguments?.getString("route_name")!!,
                        viewModel = viewModel,
                        navController = navController
                    )
                }
                composable("edit_position/{route_name}") { backStackEntry ->
                    editPosition(
                        route_name =  backStackEntry.arguments?.getString("route_name")!!,
                        MainViewModel = viewModel,
                        navController = navController,
                        context = context
                    )
                }
                composable("edit_participants/{route_name}") { backStackEntry ->
                    editParticipants(
                        route_name =  backStackEntry.arguments?.getString("route_name")!!,
                        viewModel = viewModel,
                        navController = navController
                    )
                }
                composable("route_info/{route_name}") { backStackEntry ->
                    val route_name = backStackEntry.arguments?.getString("route_name")
                    var routeInfo: RouteInfo? = null
                    for (i in viewModel.routes) {
                        if (i.name == route_name) {
                            routeInfo = i
                        }
                    }
                    RouteInfo(
                        route = routeInfo!!,
                        viewModel = viewModel,
                        navController = navController
                    )
                }
                composable("create_route") {
                    CreateRoute(
                        viewModel = viewModel,
                        context = context,
                        navController = navController
                    )
                }
            }
        }
    }
}
