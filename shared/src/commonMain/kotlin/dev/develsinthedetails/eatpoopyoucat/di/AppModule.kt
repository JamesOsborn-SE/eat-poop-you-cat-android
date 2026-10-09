package dev.develsinthedetails.eatpoopyoucat.di

import dev.develsinthedetails.eatpoopyoucat.app.Notifier
import dev.develsinthedetails.eatpoopyoucat.core.utilities.getServerUrl
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.feature.draw.DrawViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.importGames.ImportGamesViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.JoinNetGameViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.StartNetGameViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.inProgressGames.InProgressGameDetailsViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.inProgressGames.InProgressGamesViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.GameClient
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.NavigationManager
import dev.develsinthedetails.eatpoopyoucat.feature.previousGames.PreviousGameDetailsViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.previousGames.PreviousGamesViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.sentence.SentenceViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.setup.HomeViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.setup.NewGameViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect val platformServerModule: Module
val address = getServerUrl()
val appModule = module {
    includes(databaseModule)
    singleOf(::AppRepository)

    includes(platformServerModule)
    includes(platformDataStoreModule)
    single { GameClient(serverUrl = address, get(), get()) }
    singleOf(::NavigationManager)
    single<CoroutineScope> {
        CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }
    singleOf(::Notifier)

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
