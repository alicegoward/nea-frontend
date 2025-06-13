package com.example.myapplication.ui

import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.RouteState
import com.example.myapplication.RouteViewModel
import com.example.myapplication.UserViewModel
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Calendar


/**
 * Wrapper function for each route creation screen
 * @param viewModel route view model
 * @param userViewModel user view model
 * @param context application context
 * @param navController navigation
 */
@Composable
fun CreateRoute(viewModel: RouteViewModel, userViewModel: UserViewModel, context: Context, navController: NavController) {
    displaySnackbar(viewModel)
    Column(Modifier.padding(60.dp)) {
        when (viewModel.routeCreation.state) {
            RouteState.START -> start(viewModel, userViewModel, true)
            RouteState.CHOOSE_LOCATION -> chooseLocation(viewModel, context)
            RouteState.CHOOSE_END_TIME -> chooseEndTime(viewModel)
            RouteState.END -> end(viewModel, navController)
        }
        Spacer(Modifier.weight(1F))
        Row {
            TextButton(
                onClick = { viewModel.routeCreation.state = viewModel.routeCreation.state.prev() },
                content = { Text("Back") }
            )
            Spacer(Modifier.weight(1F))
            TextButton(
                onClick = { viewModel.routeCreation.state = viewModel.routeCreation.state.next() },
                content = { Text("Next") }
            )
        }
    }
}

/**
 * first route creation screen - inputs the route name and participants
 * @param routeViewModel route view model
 * @param userViewModel user view model
 * @param name whether the route name should also be inputted (false in edit mode)
 */
@Composable
fun start(routeViewModel: RouteViewModel, userViewModel: UserViewModel, name: Boolean) {
    if (name) {
        Text("Route name", fontSize = 20.sp)
        OutlinedTextField(
            value = routeViewModel.routeCreation.name,
            onValueChange = { routeViewModel.routeCreation.name = it },
        )
    }
    Spacer(Modifier.height(30.dp))
    if (userViewModel.friends != null) {
        Text("Select friends")
        for (friend: String in userViewModel.friends!!) {
            Row {
                Text(friend)
                Spacer(modifier = Modifier.weight(1F))
                if (friend in routeViewModel.routeCreation.friends) {
                    Button(
                        onClick = { routeViewModel.routeCreation.friends.remove(friend) },
                        content = { Text("Remove") }
                    )
                } else {
                    Button(
                        onClick = { routeViewModel.routeCreation.friends.add(friend) },
                        content = { Text("Add") }
                    )
                }
            }
        }
    } else {
        userViewModel.getFriends()
    }
}

/**
 * saves the route and goes back to the route manager
 * @param viewModel route view model
 * @param navController navigation
 */
fun end(viewModel: RouteViewModel, navController: NavController) {
    viewModel.routeCreation.state = RouteState.START
    viewModel.saveRoute()
    navController.navigate("routeManagerScreen")
}

/**
 * Choosing the end day and time for the meeting
 * @param viewModel route view model
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun chooseEndTime(viewModel: RouteViewModel) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().get(Calendar.MINUTE),
        is24Hour = true,
    )
    Column {
        Text("Select meeting time", Modifier.align(Alignment.CenterHorizontally), fontSize = 20.sp)
        Spacer(Modifier.height(20.dp))
        Row {
            Spacer(Modifier.weight(0.5f))
            FilledTonalButton(
                content = { Text("Day") },
                onClick = { showDatePicker = true }
            )
            Spacer(Modifier.weight(1f))
            FilledTonalButton(
                content = { Text("Time") },
                onClick = { showTimePicker = true }
            )
            Spacer(Modifier.weight(0.5f))
        }
    }


    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.routeCreation.endDate = datePickerState.selectedDateMillis?.div(1000)
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = {
                            viewModel.routeCreation.endTime =
                                (timePickerState.hour * 60 + timePickerState.minute) * 60
                            showTimePicker = false
                        }) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}

/**
 * choose location screen - move the map until the pin is over the desired location
 * @param viewModel route view model
 * @param context application context
 */
@Composable
fun chooseLocation(viewModel: RouteViewModel, context: Context) {
    Column(Modifier.fillMaxSize(0.75f)) {
        Text("Select end destination", Modifier.padding(20.dp), fontSize = 20.sp)
        Spacer(Modifier.weight(1f))
        AndroidView(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth(),
            factory = {
                MapView(it).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setCenter(viewModel.position)
                    controller.setZoom(17.5)
                }
            },
            update = { mapView ->
                run {
                    mapView.addMapListener(object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            event?.let {
                                viewModel.routeCreation.endLocation = mapView.mapCenter as GeoPoint
                            }
                            return true
                        }

                        override fun onZoom(event: ZoomEvent?): Boolean {
                            event?.let {
                                viewModel.routeCreation.endLocation = mapView.mapCenter as GeoPoint
                            }
                            return true
                        }
                    })
                    if (viewModel.position != GeoPoint(0.0, 0.0)) {
                        val location = Marker(mapView).apply {
                            position = viewModel.position
                            setInfoWindow(null)
                            icon = ContextCompat.getDrawable(
                                context,
                                R.drawable.current_location
                            )
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

                        }
                        val endPoint = Marker(mapView).apply {
                            position = viewModel.routeCreation.endLocation
                            setInfoWindow(null)
                            icon = ContextCompat.getDrawable(
                                context,
                                R.drawable.end
                            )
                            setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_CENTER)
                        }
                        Log.d("location central", viewModel.routeCreation.endLocation.toString())
                        if (mapView.overlays.isEmpty()) {
                            mapView.overlays.add(location)
                            mapView.overlays.add(endPoint)
                            mapView.controller.setCenter(viewModel.position)
                        } else {
                            mapView.overlays[0] = location
                            mapView.overlays[1] = endPoint
                        }
                        Log.d("end location", viewModel.routeCreation.endLocation.toString())
                        mapView.invalidate()
                    }
                }

            }
        )
    }

}