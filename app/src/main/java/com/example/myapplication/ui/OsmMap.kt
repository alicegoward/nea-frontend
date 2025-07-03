package com.example.myapplication.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.myapplication.RouteViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Displays a map with overlays for current location and data about
 * the active route (if relevant)
 * @param viewModel route view model
 * @param context application context
 * @param navController navigation
 */
@Composable
fun OsmdroidMapView(
    viewModel: RouteViewModel,
    context: Context,
    navController: NavController
) {
    val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    viewModel.getLocation(fusedLocationProviderClient)
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
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
                if (viewModel.position !=
                    GeoPoint(0.0, 0.0)) {
                    val location = Marker(mapView).apply {
                        position = viewModel.position
                        setInfoWindow(null)
                        icon = ContextCompat.getDrawable(
                            context,
                            com.example
                                .myapplication.R.drawable.current_location
                        )
                        setAnchor(
                            Marker.ANCHOR_CENTER,
                            Marker.ANCHOR_CENTER
                        )
                    }
                    if (mapView.overlays.isEmpty()) {
                        mapView.controller.setCenter(viewModel.position)
                    }
                    mapView.overlays.clear()
                    mapView.overlays.add(location)
                    viewModel.otherRoutes.forEach {
                        mapView.overlays.add(Polyline().apply {
                            setPoints(it.value)
                            color = android.graphics.Color.argb(
                                255,
                                50,
                                50,
                                200
                            )
                        })
                    }
                    mapView.overlays.add(Polyline().apply {
                        setPoints(viewModel.routePoints)
                    })
                    viewModel.waypoints.forEach {
                        mapView.overlays.add(Marker(mapView).apply {
                            position = it
                            setInfoWindow(null)
                            icon = ContextCompat.getDrawable(
                                context,
                                com.example
                                    .myapplication.R.drawable.waypoint
                            )
                            setAnchor(
                                Marker.ANCHOR_CENTER,
                                Marker.ANCHOR_CENTER
                            )
                        })
                    }
                    if (viewModel.routePoints.isNotEmpty()) {
                        mapView.overlays.add(Marker(mapView).apply {
                            position = viewModel.routePoints.last()
                            setInfoWindow(null)
                            icon = ContextCompat.getDrawable(
                                context,
                                com.example.myapplication.R.drawable.end
                            )
                            setAnchor(
                                Marker.ANCHOR_CENTER,
                                Marker.ANCHOR_BOTTOM
                            )
                        })
                    }

                    mapView.invalidate()
                }
            }
        }
    )
    outerScaffold(navController, viewModel) {
        if (viewModel.itineraries != null) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Spacer(Modifier.height(60.dp))
                for (itinerary in viewModel.itineraries!!) {
                    Row(
                        Modifier.padding(
                            horizontal = 30.dp,
                            vertical = 10.dp
                        )
                    ) {
                        Text(itinerary.username)
                        Spacer(Modifier.weight(1F))
                        Text(
                            SimpleDateFormat("HH:mm", Locale.UK)
                                .format(Date(
                                    itinerary.eta.toLong() * 1000)
                            )
                        )
                    }
                    for (waypoint in itinerary.waypoints.values) {
                        Row(Modifier.padding(horizontal = 30.dp)) {
                            Spacer(Modifier.width(30.dp))
                            Text(
                                viewModel.convertToParticipants(
                                    waypoint.participants
                                        .values.toMutableList(),
                                    itinerary.username
                                )
                            )
                            Spacer(Modifier.weight(1F))
                            Text(
                                SimpleDateFormat(
                                    "HH:mm",
                                    Locale.UK
                                ).format(Date(
                                    waypoint.eta.toLong() * 1000))
                            )
                        }
                    }
                }
                Row {
                    TextButton(
                        content = { Text("Notify late") },
                        onClick = { viewModel
                            .sendOthersNotification("late") },
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        content = { Text("Notify lost") },
                        onClick = { viewModel
                            .sendOthersNotification("lost") },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}