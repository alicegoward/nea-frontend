package com.example.myapplication.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.TestViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun OsmdroidMapView(viewModel: TestViewModel, context: Context, gotoFriends: ()->Unit) {
    val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    lateinit var mapView: MapView
    viewModel.getLocation(fusedLocationProviderClient)
    Column(
        modifier = Modifier.fillMaxSize().padding(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75F),
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
                    if (viewModel.position != GeoPoint(0.0,0.0)) {
                        val location = Marker(mapView).apply {
                            position = viewModel.position
                            setInfoWindow(null)
                            icon = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

                        }
                        if (mapView.overlays.isEmpty()) {
                            mapView.overlays.add(location)
                            mapView.controller.setCenter(viewModel.position)
                        } else {
                            mapView.overlays[0] = location

                        }
                        mapView.invalidate()
                    }
                }
            }
        )
        Spacer(modifier = Modifier.weight(1F))
        Button(
            onClick = { gotoFriends() },
            content = { Text("Friends") }
        )
    }
}