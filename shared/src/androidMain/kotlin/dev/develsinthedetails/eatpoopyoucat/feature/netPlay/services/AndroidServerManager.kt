package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import android.content.Intent
import android.os.Build
import android.provider.Settings
import dev.develsinthedetails.eatpoopyoucat.app.AppContextProvider
import dev.develsinthedetails.eatpoopyoucat.core.utilities.NetworkUtils

class AndroidServerManager : ServerManager {
    val context = AppContextProvider.context
    override val currentAddress: String?
        get() = NetworkUtils.getLocalIpAddress()?.let { "http://$it:3947" }

    override fun startServer() {
        val serviceIntent = Intent(context, AndroidForegroundServerService::class.java)
        context.startService(serviceIntent)
    }

    override fun stopServer() {
        val serviceIntent = Intent(context, AndroidForegroundServerService::class.java)
        context.stopService(serviceIntent)
    }

    override fun promptNetworkSettings() {
        val panelIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_WIFI)
        } else {
            Intent(Settings.ACTION_WIFI_SETTINGS)
        }
        panelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(panelIntent)
    }
}