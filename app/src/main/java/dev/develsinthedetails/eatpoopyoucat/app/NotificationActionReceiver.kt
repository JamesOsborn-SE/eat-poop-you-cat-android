package dev.develsinthedetails.eatpoopyoucat.app

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "ACTION_YES" -> {
                Toast.makeText(context, "You clicked YES!", Toast.LENGTH_SHORT).show()
            }
            "ACTION_NO" -> {
                Toast.makeText(context, "You clicked NO!", Toast.LENGTH_SHORT).show()
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
    }
}