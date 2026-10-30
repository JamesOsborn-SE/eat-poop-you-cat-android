package dev.develsinthedetails.eatpoopyoucat.feature.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.app.AppContextProvider
import dev.develsinthedetails.eatpoopyoucat.app.MainActivity
import dev.develsinthedetails.eatpoopyoucat.app.Notifier
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.AppNotificationChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class SystemNotifierListener {
    private val notifier by inject<Notifier>(Notifier::class.java)
    private val notificationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    fun start() {
        notificationScope.launch {
            notifier.systemNotificationEvents.collect { event ->
                try {
                    showNotification(
                        title = event.title,
                        text = event.text,
                        yes = event.yes,
                        no = event.no,
                        destUri = event.destUri
                    )
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
    }

    init {
        start()
    }

    private fun showNotification(
        title: String,
        text: String,
        yes: String,
        no: String,
        destUri: String
    ) {
        val context = AppContextProvider.context
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val yesIntent = Intent(
            Intent.ACTION_VIEW,
            destUri.toUri(),
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
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent)
            .addAction(0, yes, yesPendingIntent)
            .addAction(0, no, noPendingIntent)

        try {
            notificationManager.notify(1, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}