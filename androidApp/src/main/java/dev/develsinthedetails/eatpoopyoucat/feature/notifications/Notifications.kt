package dev.develsinthedetails.eatpoopyoucat.feature.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.app.MainActivity

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

fun showTurnNotification(context: Context, destUrl: String) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val intentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    val yesIntent = Intent(
        Intent.ACTION_VIEW,
        destUrl.toUri(),
        context,
        MainActivity::class.java
    ).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val yesPendingIntent = PendingIntent.getActivity(context, 0, yesIntent, intentFlags)

    val noIntent = Intent()
    val noPendingIntent = PendingIntent.getBroadcast(context, 1, noIntent, intentFlags)

    val mainPendingIntent = PendingIntent.getActivity(
        context,
        2,
        yesIntent,
        intentFlags
    )

    val builder = NotificationCompat.Builder(context, AppNotificationChannel.GAME_ALERTS.id)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle("Want to take your turn?")
        .setContentText("Join us!!!")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(mainPendingIntent)
        .addAction(0, "Yes", yesPendingIntent)
        .addAction(0, "No", noPendingIntent)

    try {
        notificationManager.notify(1, builder.build())
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}