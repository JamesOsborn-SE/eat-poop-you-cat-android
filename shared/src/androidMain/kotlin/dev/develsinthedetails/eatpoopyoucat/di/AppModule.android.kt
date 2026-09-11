package dev.develsinthedetails.eatpoopyoucat.di

import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.AndroidServerManager
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.ServerManager
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformServerModule: Module = module {
    single<ServerManager> { AndroidServerManager() }
}