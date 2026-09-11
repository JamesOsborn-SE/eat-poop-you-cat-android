package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import dev.develsinthedetails.eatpoopyoucat.R
import org.koin.android.ext.android.inject

class AndroidForegroundServerService : Service() {
    private val sharedKtorServer by inject<SharedKtorServer>()
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        startForegroundServiceNotification()
        sharedKtorServer.start()
    }

    override fun onDestroy() {
        sharedKtorServer.stop()
        super.onDestroy()
    }

    private fun startForegroundServiceNotification() {
        val notification = Notification.Builder(this, "webserver")
            .setContentTitle("Net Play Game")
            .setContentText("Hosting server...")
            .setSmallIcon(R.drawable.ic_notification)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        } else {
            startForeground(1, notification)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "webserver",
            "Web Server Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }
}