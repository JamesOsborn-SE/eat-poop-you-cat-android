package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.StateFlow

sealed class ServerState {
    data object Stopped : ServerState()
    data object Starting : ServerState()
    data class Running(val address: String) : ServerState()
    data class Error(val message: String) : ServerState()
}

interface ServerManager {
    val serverState: StateFlow<ServerState>
    fun startServer()
    fun stopServer()
    fun promptNetworkSettings()
    fun clearError()
}

@Composable
fun ManageServerLifecycle(
    serverManager: ServerManager,
    onUpdateAddress: (String?) -> Unit,
    onStateChange: (ServerState) -> Unit = {}
) {
    val state by serverManager.serverState.collectAsState()

    LaunchedEffect(state) {
        onStateChange(state)

        when (val currentState = state) {
            is ServerState.Running -> {
                onUpdateAddress(currentState.address)
            }

            is ServerState.Stopped -> {
                onUpdateAddress(null)
            }

            is ServerState.Error -> {
                onUpdateAddress(null)
                serverManager.promptNetworkSettings()
                serverManager.clearError()
            }

            is ServerState.Starting -> {

            }
        }
    }

    LaunchedEffect(Unit) {
        if (serverManager.serverState.value is ServerState.Stopped) {
            serverManager.startServer()
        }
    }
}