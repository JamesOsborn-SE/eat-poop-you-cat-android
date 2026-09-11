package dev.develsinthedetails.eatpoopyoucat.app

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (intent.action) {
            "ACTION_YES" -> {
                val destUrl = intent.getStringExtra("DEST_URL")
                if (destUrl != null) {
                    val deepLinkIntent = Intent(Intent.ACTION_VIEW, destUrl.toUri()).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(deepLinkIntent)
                }
                notificationManager.cancel(1)
            }
            "ACTION_NO" -> {
                notificationManager.cancel(1)
            }
        }
    }
}