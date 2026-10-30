package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import dev.develsinthedetails.eatpoopyoucat.R
import org.koin.android.ext.android.inject

enum class AppNotificationChannel(
    val id: String,
    val channelName: String,
    val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
) {
    WEB_SERVER(
        id = "webserver",
        channelName = "Web Server Service",
        importance = NotificationManager.IMPORTANCE_LOW
    ),
    GAME_ALERTS(
        id = "game_alerts",
        channelName = "Game Alerts",
        importance = NotificationManager.IMPORTANCE_HIGH
    )
}

class AndroidForegroundServerService : Service() {
    private val gameServer by inject<GameServer>()
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
        gameServer.start()
    }

    override fun onDestroy() {
        gameServer.stop()
        super.onDestroy()
    }

    private fun startForegroundServiceNotification() {
        val notification = Notification.Builder(this, AppNotificationChannel.WEB_SERVER.id)
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
}