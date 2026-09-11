package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds


interface ServerManager {
    val currentAddress: String?
    fun startServer()
    fun stopServer()
    fun promptNetworkSettings()
}

@Composable
fun ManageServerLifecycle(
    serverManager: ServerManager,
    onUpdateAddress: (String?) -> Unit
) {
    LaunchedEffect(Unit) {
        val address = serverManager.currentAddress
        onUpdateAddress(address)

        if (address != null) {
            serverManager.startServer()
        } else {
            serverManager.promptNetworkSettings()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            onUpdateAddress(serverManager.currentAddress)
            delay(1.seconds)
        }
    }
}