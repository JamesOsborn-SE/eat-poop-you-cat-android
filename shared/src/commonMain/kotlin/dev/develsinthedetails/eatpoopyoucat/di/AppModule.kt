package dev.develsinthedetails.eatpoopyoucat.di

import dev.develsinthedetails.eatpoopyoucat.app.Notifier
import dev.develsinthedetails.eatpoopyoucat.core.utilities.getGameIdFromUrl
import dev.develsinthedetails.eatpoopyoucat.core.utilities.getServerUrl
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.feature.draw.DrawViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.importGames.ImportGamesViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.JoinNetGameViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.StartNetGameViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.inProgressGames.InProgressGameDetailsViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.inProgressGames.InProgressGamesViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.Client
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.GameServerRouter
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.IncomingEventProcessor
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.NavigationManager
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.SharedKtorServer
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.StaticRouter
import dev.develsinthedetails.eatpoopyoucat.feature.previousGames.PreviousGameDetailsViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.previousGames.PreviousGamesViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.sentence.SentenceViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.setup.HomeViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.setup.NewGameViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect val platformServerModule: Module
val address = getServerUrl()
val gameId = getGameIdFromUrl()
val appModule = module {
    includes(databaseModule)
    single {
        AppRepository(
            gameDao = get(),
            entryDao = get(),
            playerDao = get(),
            rosterDao = get()
        )
    }
    singleOf(::SharedKtorServer)
    includes(platformServerModule)
    includes(platformDataStoreModule)
    single { Client(serverUrl = address, get()) }
    singleOf(::NavigationManager)
    single {
        IncomingEventProcessor(
            client = get(),
            repository = get(),
            notifier = get(),
            navigationManager = get()
        )
    }
    singleOf(::Notifier)
    singleOf(::GameServerRouter)
    singleOf(::StaticRouter)
    viewModelOf(::HomeViewModel)
    viewModelOf(::PreviousGameDetailsViewModel)
    viewModelOf(::SentenceViewModel)
    viewModelOf(::PreviousGamesViewModel)
    viewModelOf(::DrawViewModel)
    viewModelOf(::ImportGamesViewModel)
    viewModelOf(::InProgressGamesViewModel)
    viewModelOf(::StartNetGameViewModel)
    viewModelOf(::NewGameViewModel)
    viewModelOf(::InProgressGameDetailsViewModel)
    viewModelOf(::JoinNetGameViewModel)
}
