package com.example.myapplication

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

class TestService: Service() {
    private lateinit var locationCallback: LocationCallback
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("help","here")
        val name = "testName"
        val descriptionText = "testDescription"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("testChannel", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        val notifintent = Intent(this, MainActivity::class.java)
        notifintent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent =
            PendingIntent.getActivity(this, 0, notifintent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(this, "testChannel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("some stuff")
            .setContentText("more stuff")
            .setPriority(NotificationCompat.PRIORITY_MAX).setContentIntent(pendingIntent)
        ServiceCompat.startForeground(this,100,builder.build(),ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE)
        val db = AppDatabase.getInstance(this)
        val locationDao = db.locationDao()
        Log.d("help","endhere")
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations){
                    Log.d("location",location.latitude.toString())
                    CoroutineScope(Dispatchers.IO).launch {
                        locationDao.insertLocation(Location(0,Instant.now().epochSecond,location.latitude,location.longitude))
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


}