package dev.develsinthedetails.eatpoopyoucat

import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.viewmodels.Draw
import dev.develsinthedetails.eatpoopyoucat.viewmodels.Greeting
import dev.develsinthedetails.eatpoopyoucat.viewmodels.ImportGames
import dev.develsinthedetails.eatpoopyoucat.viewmodels.NetGameViewModel
import dev.develsinthedetails.eatpoopyoucat.viewmodels.Nickname
import dev.develsinthedetails.eatpoopyoucat.viewmodels.PreviousGame
import dev.develsinthedetails.eatpoopyoucat.viewmodels.PreviousGames
import dev.develsinthedetails.eatpoopyoucat.viewmodels.Sentence
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::AppRepository)
    viewModelOf(::Sentence)
    viewModelOf(::PreviousGames)
    viewModel { PreviousGame(get(), get()) }
    viewModelOf(::Greeting)
    viewModelOf(::Draw)
    viewModelOf(::Nickname)
    viewModelOf(::ImportGames)
    viewModelOf(::NetGameViewModel)
}
