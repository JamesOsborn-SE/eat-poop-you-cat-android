package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class SharedKtorServer(
    gameRouter: GameRouter,
    staticRouter: StaticRouter,
    repository: AppRepository,
    client: Client,
    appSettings: AppSettings
) {
    fun start()
    fun stop()
}