package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import dev.develsinthedetails.eatpoopyoucat.core.utilities.NetworkUtils
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun ManageServerLifecycle(
    onUpdateAddress: (String?) -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val ipAddress = NetworkUtils.getLocalIpAddress()
        val isWifiOn = NetworkUtils.isWifiConnected(context)

        onUpdateAddress(ipAddress?.let { "http://$it:3947" })

        if (isWifiOn || ipAddress != null) {
            val serviceIntent = Intent(context, Server::class.java)
            context.startService(serviceIntent)
        } else {
            val panelIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent(Settings.Panel.ACTION_WIFI)
            } else {
                Intent(Settings.ACTION_WIFI_SETTINGS)
            }
            context.startActivity(panelIntent)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val ipAddress = NetworkUtils.getLocalIpAddress()
            onUpdateAddress(ipAddress?.let { "http://$it:3947" })
            delay(1.seconds)
        }
    }
}