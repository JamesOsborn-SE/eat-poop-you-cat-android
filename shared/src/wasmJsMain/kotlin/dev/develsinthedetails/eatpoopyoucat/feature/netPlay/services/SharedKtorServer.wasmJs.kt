package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WebServerManager : ServerManager {
    override val serverState: StateFlow<ServerState> = MutableStateFlow(ServerState.Stopped)
    override fun startServer() {
    }

    override fun stopServer() {
    }

    override fun promptNetworkSettings() {
    }

    override fun clearError() {
    }

}