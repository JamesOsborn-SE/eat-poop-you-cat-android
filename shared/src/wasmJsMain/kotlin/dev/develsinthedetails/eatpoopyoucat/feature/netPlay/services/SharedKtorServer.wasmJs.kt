package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
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

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class SharedKtorServer actual constructor(
    gameServerRouter: GameServerRouter,
    staticRouter: StaticRouter,
    repository: AppRepository,
    appSettings: AppSettings,
    navigationManager: NavigationManager
) {
    actual fun start() {
    }

    actual fun stop() {
    }

    actual suspend fun sendGameComplete(game: Game) {}
}