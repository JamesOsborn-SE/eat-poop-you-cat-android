package dev.develsinthedetails.eatpoopyoucat.feature.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationCompat
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.app.MainActivity
import dev.develsinthedetails.eatpoopyoucat.app.NotificationActionReceiver

@Composable
@Preview
fun NotificationTester() {
    val context = LocalContext.current
    val channelId = "net_play_games"
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            return@rememberLauncherForActivityResult
        }
    }

}


private fun showNotification(context: Context, channelId: String) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val channel = NotificationChannel(
        channelId,
        "Test Notifications",
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = "Channel for testing notifications"
    }
    notificationManager.createNotificationChannel(channel)

    val intentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    val yesIntent = Intent(context, NotificationActionReceiver::class.java).apply {
        action = "ACTION_YES"
    }
    val yesPendingIntent = PendingIntent.getBroadcast(context, 0, yesIntent, intentFlags)

    val noIntent = Intent(context, NotificationActionReceiver::class.java).apply {
        action = "ACTION_NO"
    }
    val mainIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val mainPendingIntent = PendingIntent.getActivity(
        context,
        2,
        mainIntent,
        intentFlags
    )
    val noPendingIntent = PendingIntent.getBroadcast(context, 1, noIntent, intentFlags)

    val builder = NotificationCompat.Builder(context, channelId)
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