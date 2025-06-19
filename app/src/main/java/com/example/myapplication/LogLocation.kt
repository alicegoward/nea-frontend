package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * Service that runs in the background while a route is activated.
 */
class LogLocation: Service() {
    private lateinit var locationCallback: LocationCallback
    val mainAPI: MainAPI = RetrofitHelper.getInstance().create(MainAPI::class.java)

    /**
     * Creates the location callback and sends the results to the server.
     * Creates a notification to display to the user that location is being recorded
     */
    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val name = "location"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel("location", name, importance)
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        val builder = NotificationCompat.Builder(this, "location")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Active route")
            .setContentText("Currently recording location")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            })
        val notification = builder.build()
        notification.flags = Notification.FLAG_ONGOING_EVENT
        ServiceCompat.startForeground(this,1,notification,ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        locationCallback = object : LocationCallback() {
            /**
             * On receiving each location, sends the result to the server
             */
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations){
                    Log.d("location",location.latitude.toString())
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            mainAPI.postData(
                                LocationData(
                                    Instant.now().epochSecond,
                                    Coordinate(location.latitude, location.longitude)
                                )
                            )
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        }
        val locationRequest = LocationRequest.Builder(5000).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,Looper.getMainLooper())
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    /**
     * Stop location updates
     */
    override fun onDestroy() {
        super.onDestroy()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}