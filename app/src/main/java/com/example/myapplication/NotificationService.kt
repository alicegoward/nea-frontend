package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.RetrofitHelper.getData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random


class NotificationService : Service() {
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
            .setContentTitle("aa")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val userDao = AppDatabase.getInstance(this).UserDao()
        var user: String? = null
        getData {
            while (true) {
                if (user == null) {
                    val usernames = userDao.getUsername()
                    if (usernames.size == 1) {
                        user = usernames[0]
                    }
                }
                Log.d("maybe notification",user.toString())
                if (user != null) {
                    val mainAPI: MainAPI =
                        RetrofitHelper.getInstance().create(MainAPI::class.java)
                    val data = mainAPI.getNotificationData(user!!).body()?.notifications
                    data?.forEach { entry ->
                        Log.d("notification", entry.value)
                        notificationManager.notify(
                            Random.nextInt(0, 1000),
                            builder.setContentText(entry.value).build()
                        )
                    }
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