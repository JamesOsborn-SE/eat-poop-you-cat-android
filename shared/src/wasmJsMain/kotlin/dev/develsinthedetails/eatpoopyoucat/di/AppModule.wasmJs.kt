package dev.develsinthedetails.eatpoopyoucat.di

import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.IncomingEventProcessor
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.ServerManager
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.WebServerManager
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformGameServerModule: Module = module {
    single<ServerManager> { WebServerManager() }
}
val platformModule: Module = module {
    single<IncomingEventProcessor> { IncomingEventProcessor(get(), get(), get(), get()) }
}