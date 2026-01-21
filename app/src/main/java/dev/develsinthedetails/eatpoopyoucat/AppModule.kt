package dev.develsinthedetails.eatpoopyoucat

import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.viewmodels.DrawViewModel
import dev.develsinthedetails.eatpoopyoucat.viewmodels.GreetingViewModel
import dev.develsinthedetails.eatpoopyoucat.viewmodels.ImportGamesViewModel
import dev.develsinthedetails.eatpoopyoucat.viewmodels.NicknameViewModel
import dev.develsinthedetails.eatpoopyoucat.viewmodels.PreviousGameViewModel
import dev.develsinthedetails.eatpoopyoucat.viewmodels.PreviousGamesViewModel
import dev.develsinthedetails.eatpoopyoucat.viewmodels.SentenceViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::AppRepository)
    viewModelOf(::SentenceViewModel)
    viewModelOf(::PreviousGamesViewModel)
    viewModel { PreviousGameViewModel(get(), get()) }
    viewModelOf(::GreetingViewModel)
    viewModelOf(::DrawViewModel)
    viewModelOf(::NicknameViewModel)
    viewModelOf(::ImportGamesViewModel)
}
