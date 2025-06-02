package com.example.myapplication.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.myapplication.MainViewModel
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


@Composable
fun OsmdroidMapView(viewModel: MainViewModel, context: Context, navController: NavController) {
    val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    viewModel.getLocation(fusedLocationProviderClient)
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setCenter(viewModel.position)
                controller.setZoom(17.5)
            }
        },
        update = {
                mapView ->
            run {
                if (viewModel.position != GeoPoint(0.0,0.0)) {
                    val location = Marker(mapView).apply {
                        position = viewModel.position
                        setInfoWindow(null)
                        icon = ContextCompat.getDrawable(context, com.example.myapplication.R.drawable.ic_launcher_foreground)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

                    }
                    if (mapView.overlays.isEmpty()) {
                        mapView.controller.setCenter(viewModel.position)
                    }
                    mapView.overlays.clear()
                    mapView.overlays.add(location)
                    Log.d("route geometry size", viewModel.other_routes.size.toString())
                    viewModel.other_routes.forEach {
                        Log.d("route geometry maybe", it.value.toString())
                        mapView.overlays.add(Polyline().apply {
                            setPoints(it.value)
                            setColor(android.graphics.Color.argb(255,255,0,0))
                        })
                    }
                    val bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
                    val paint = Paint().apply {
                        setColor(android.graphics.Color.argb(255, 0, 0, 255))
                    }
                    Canvas(bitmap).drawRect(0F, 0F, 50F, 50F, paint)
                    viewModel.waypoints.forEach {
                        mapView.overlays.add(Marker(mapView).apply {
                            position = it
                            setInfoWindow(null)
                            icon = BitmapDrawable(context.resources, bitmap)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        })
                    }
                    mapView.overlays.add(Polyline().apply {
                        setPoints(viewModel.sampleRoute)
                    })
                    mapView.invalidate()
                }
            }
        }
    )
    outerScaffold (navController, viewModel) {
        if (viewModel.itineraries != null) {
            Column(Modifier.fillMaxWidth().background(Color.Gray).padding(60.dp)) {
                for (itinerary in viewModel.itineraries!!) {
                    Row {
                        Text(itinerary.username)
                        Spacer(Modifier.weight(1F))
                        Text(
                            SimpleDateFormat("HH:mm",Locale.UK).format(Date(itinerary.eta.toLong() * 1000))
                        )
                    }
                    for (waypoint in itinerary.waypoints.values) {
                        Row(Modifier.padding(horizontal = 30.dp)) {
                            Text(waypoint.participants.values.toString())
                            Spacer(Modifier.weight(1F))
                            Text(SimpleDateFormat("HH:mm", Locale.UK).format(Date(waypoint.eta.toLong() * 1000))
                            )
                        }
                    }
                }
                Row {
                    Button(
                        content = { Text("Notify late") },
                        onClick = { viewModel.sendOthersNotification("late") }
                    )
                    Spacer(Modifier.weight(1F))
                    Button(
                        content = { Text("Notify lost") },
                        onClick = { viewModel.sendOthersNotification("lost") }
                    )
                }
            }
        }
    }
}