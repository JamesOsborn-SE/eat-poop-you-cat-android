package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import android.content.Intent
import android.os.Build
import android.provider.Settings
import dev.develsinthedetails.eatpoopyoucat.app.AppContextProvider
import dev.develsinthedetails.eatpoopyoucat.core.utilities.NetworkUtils
import dev.develsinthedetails.eatpoopyoucat.core.utilities.SERVER_PORT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidServerManager : ServerManager {
    val context = AppContextProvider.context

    private val _serverState = MutableStateFlow<ServerState>(ServerState.Stopped)
    override val serverState: StateFlow<ServerState> = _serverState.asStateFlow()

    override fun startServer() {
        _serverState.value = ServerState.Starting

        val ip = NetworkUtils.getLocalIpAddress()
        if (ip == null) {
            _serverState.value = ServerState.Error("No Wi-Fi or LAN connection detected.")
            return
        }

        val address = "http://$ip:$SERVER_PORT"
        val serviceIntent = Intent(context, AndroidForegroundServerService::class.java)

        context.startForegroundService(serviceIntent)

        _serverState.value = ServerState.Running(address)
    }

    override fun stopServer() {
        val serviceIntent = Intent(context, AndroidForegroundServerService::class.java)
        context.stopService(serviceIntent)
        _serverState.value = ServerState.Stopped
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

    override fun clearError() {
        if (_serverState.value is ServerState.Error) {
            _serverState.value = ServerState.Stopped
        }
    }
}