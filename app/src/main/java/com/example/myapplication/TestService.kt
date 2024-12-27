package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat

class TestService: Service() {
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
        // Builds a specific notification (the only notification)
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
        Log.d("help","endhere")
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}