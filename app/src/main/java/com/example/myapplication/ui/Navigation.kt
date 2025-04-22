package com.example.myapplication.ui

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.RouteInfo
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
                        gotoLogin = {navController.navigate(("loginScreen"))},
                    )
                }
                composable("displayLocation") {
                    DisplayLocation(viewModel, context)
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
                        gotoFriends = {navController.navigate(("friendScreen"))},
                        gotoRoute = { navController.navigate(("routeManagerScreen")) })
                }
                composable("friendScreen") {
                    FriendScreen(
                        viewModel = viewModel,
                        context = context,
                        gotoMap = {navController.navigate(("mapScreen")) },
                        gotoRoute = {navController.navigate(("routeManagerScreen"))}
                    )
                }
                composable("routeManagerScreen") {
                    RouteManager(
                        viewModel = viewModel,
                        context = context,
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
                        viewModel = viewModel,
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
