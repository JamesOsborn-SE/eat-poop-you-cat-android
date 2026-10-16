package dev.develsinthedetails.eatpoopyoucat.di

import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.AndroidServerManager
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.GameEventHandler
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.GameServer
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.ServerManager
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.StaticRouter
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformGameServerModule: Module = module {
    single<ServerManager> { AndroidServerManager() }
    singleOf(::GameEventHandler)
    singleOf(::StaticRouter)
    singleOf(::GameServer)
}