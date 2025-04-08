package com.example.myapplication.ui

import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.Route
import com.example.myapplication.RouteState
import com.example.myapplication.TestViewModel
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Calendar


@Composable
fun RouteManager(viewModel: TestViewModel, context: Context) {
    when (viewModel.routeCreation.state) {
        RouteState.START -> start(viewModel)
        RouteState.CHOOSE_FRIENDS -> chooseFriends(viewModel)
        RouteState.CHOOSE_LOCATION -> chooseLocation(viewModel, context)
        RouteState.CHOOSE_END_TIME -> chooseEndTime(viewModel)
        RouteState.END -> end(viewModel)
    }
}

fun start(viewModel: TestViewModel) {
    viewModel.routeCreation.state = RouteState.CHOOSE_FRIENDS
}

fun end(viewModel: TestViewModel) {
    viewModel.saveRoute()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun chooseEndTime(viewModel: TestViewModel) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().get(Calendar.MINUTE),
        is24Hour = true,
    )
    Column(modifier = Modifier.padding(60.dp)) {
        Button(
            content = { Text("select day") },
            onClick = { showDatePicker = true }
        )
        Button(
            content = { Text("select time") },
            onClick = { showTimePicker = true }
        )
        Spacer(modifier=Modifier.weight(1F))
        Row {
            Button(
                onClick = { viewModel.routeCreation.state = RouteState.CHOOSE_LOCATION },
                content = { Text("Back") }
            )
            Button(
                onClick = { viewModel.routeCreation.state = RouteState.END },
                content = { Text("Next") }
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.routeCreation.endDate = datePickerState.selectedDateMillis?.toInt()
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
        Column(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            TimeInput(
                state = timePickerState,
            )
            Button(onClick = { showTimePicker = false }) {
                Text("Dismiss picker")
            }
            Button(onClick = {
                viewModel.routeCreation.endTime = (timePickerState.hour*60+timePickerState.minute)*60*1000
                showTimePicker = false
            }) {
                Text("Confirm selection")
            }
        }
    }
}

@Composable
fun chooseLocation(viewModel: TestViewModel, context: Context) {
    lateinit var mapView: MapView
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75F),
            factory = { context ->
                mapView = MapView(context)
                mapView.setTileSource(TileSourceFactory.MAPNIK)
                mapView.setMultiTouchControls(true)
                mapView.controller.setCenter(viewModel.position)
                mapView.controller.setZoom(17.5)
                mapView
            },
            update = {
                    mapView ->
                run {
                    mapView.addMapListener(object: MapListener {
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
                                R.drawable.ic_launcher_foreground
                            )
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

                        }
                        val endPoint = Marker(mapView).apply {
                            position = viewModel.routeCreation.endLocation
                            setInfoWindow(null)
                            icon = ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_launcher_background
                            )
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
        Spacer(modifier=Modifier.weight(1F))
        Row {
            Button(
                onClick = { viewModel.routeCreation.state = RouteState.CHOOSE_FRIENDS },
                content = { Text("Back") }
            )
            Button(
                onClick = { viewModel.routeCreation.state = RouteState.CHOOSE_END_TIME },
                content = { Text("Next") }
            )
        }
    }

}

@Composable
fun chooseFriends(viewModel: TestViewModel) {
    Column(modifier = Modifier.padding(60.dp)) {
        if (viewModel.friends != null) {
            Text("Select friends")
            for (friend: String in viewModel.friends!!) {
                Row {
                    Text(friend)
                    Spacer(modifier = Modifier.weight(1F))
                    Log.d("friend stuff", (friend in viewModel.routeCreation.friends).toString())
                    if (friend in viewModel.routeCreation.friends) {
                        Button(
                            onClick = { viewModel.routeCreation.friends.remove(friend) },
                            content = { Text("Remove") }
                        )
                    } else {
                        Button(
                            onClick = { viewModel.routeCreation.friends.add(friend) },
                            content = { Text("Add") }
                        )
                    }
                }
            }
            Spacer(modifier=Modifier.weight(1F))
            Row {
                Button(
                    onClick = {},
                    content = { Text("Back") }
                )
                Button(
                    onClick = { viewModel.routeCreation.state = RouteState.CHOOSE_LOCATION },
                    content = { Text("Next") }
                )
            }
        } else {
            viewModel.getFriends()
        }

    }
}