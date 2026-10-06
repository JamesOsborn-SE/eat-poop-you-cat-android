package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Game


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class SharedKtorServer(
    gameServerRouter: GameServerRouter,
    staticRouter: StaticRouter,
    repository: AppRepository,
    appSettings: AppSettings,
    navigationManager: NavigationManager
) {
    fun start()
    fun stop()
    suspend fun sendGameComplete(game: Game)
    companion object {
        val providesServer: Boolean
    }
}