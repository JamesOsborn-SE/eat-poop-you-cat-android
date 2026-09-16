package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository

class WebServerManager() : ServerManager {
    override val currentAddress: String? = null

    override fun startServer() {
    }

    override fun stopServer() {
    }

    override fun promptNetworkSettings() {
    }

}

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class SharedKtorServer actual constructor(
    gameRouter: GameRouter,
    staticRouter: StaticRouter,
    repository: AppRepository,
    client: Client,
    appSettings: AppSettings
) {
    actual fun start() {
    }

    actual fun stop() {
    }
}