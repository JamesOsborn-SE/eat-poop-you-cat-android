package dev.develsinthedetails.eatpoopyoucat.feature.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.app.MainActivity
import dev.develsinthedetails.eatpoopyoucat.app.NotificationActionReceiver
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme

@Composable
@Preview
fun NotificationTester() {
    val context = LocalContext.current
    val channelId = "test_channel_id"

    // 1. Setup the permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showNotification(context, channelId)
        }
    }
    AppTheme() {


        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = {
                // Check if we need to ask for permission (Android 13+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        showNotification(context, channelId)
                    } else {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    // Pre-Android 13, manifest permission is enough
                    showNotification(context, channelId)
                }
            }) {
                Text("Send Test Notification")
            }
        }
    }
}


private fun showNotification(context: Context, channelId: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // 2. Create the Notification Channel (safe to call multiple times)
    val channel = NotificationChannel(
        channelId,
        "Test Notifications",
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = "Channel for testing notifications"
    }
    notificationManager.createNotificationChannel(channel)

    // Flag required for Android 12+ (API 31)
    val intentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    // "Yes" Intent
    val yesIntent = Intent(context, NotificationActionReceiver::class.java).apply {
        action = "ACTION_YES"
    }
    val yesPendingIntent = PendingIntent.getBroadcast(context, 0, yesIntent, intentFlags)

    // "No" Intent
    val noIntent = Intent(context, NotificationActionReceiver::class.java).apply {
        action = "ACTION_NO"
    }
    val mainIntent = Intent(context, MainActivity::class.java).apply {
        // These flags ensure tapping the notification doesn't open a duplicate app instance
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val mainPendingIntent = PendingIntent.getActivity(
        context,
        2, // Use a different request code than your action buttons
        mainIntent,
        intentFlags
    )
    val noPendingIntent = PendingIntent.getBroadcast(context, 1, noIntent, intentFlags)

    // 3. Build the notification
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle("Want to take your turn?")
        .setContentText("Join us!!!")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(mainPendingIntent)
        .addAction(0, "Yes", yesPendingIntent)
        .addAction(0, "No", noPendingIntent)

    // 4. Show the notification
    try {
        notificationManager.notify(1, builder.build())
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}