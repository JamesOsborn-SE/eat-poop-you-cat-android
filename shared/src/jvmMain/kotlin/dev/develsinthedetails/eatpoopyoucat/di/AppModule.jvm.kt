package dev.develsinthedetails.eatpoopyoucat.di

import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.DesktopServerManager
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.GameEventHandler
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.Server
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.ServerManager
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.StaticRouter
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformServerModule: Module = module {
    singleOf(::GameEventHandler)
    singleOf(::StaticRouter)
    singleOf(::Server)
    single<ServerManager> { DesktopServerManager(get()) }
}