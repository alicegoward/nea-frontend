package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Service that polls for location updates at regular intervals
 */
class NotificationService : Service() {
    /**
     * Checks for any new notifications every 30s, and notifies the user if so.
     * This is a background service, so runs continuously
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("stuff","started service")
        val channel = NotificationChannel("api_notifications", "api", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "api service testing"
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        val builder = NotificationCompat.Builder(this, "api_notifications")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("NEA frontend")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val userDao = AppDatabase.getInstance(this).UserDao()
        var user: String? = null
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    if (user == null) {
                        val usernames = userDao.getUserPass()
                        if (usernames.size == 1) {
                            user = usernames[0].username
                        }
                    }
                    Log.d("maybe notification", user.toString())
                    if (user != null) {
                        val mainAPI: MainAPI =
                            RetrofitHelper.getInstance().create(MainAPI::class.java)
                        val data = mainAPI.getNotificationData().body()?.notifications
                        data?.forEach { entry ->
                            notificationManager.notify(
                                Random.nextInt(0, 1000),
                                builder.setContentText(entry.value).build()
                            )
                        }
                    }
                } catch(e: Exception) {
                    Log.d("fail", "notification")
                }
                Thread.sleep(30000)
            }
        }
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}