package dev.develsinthedetails.eatpoopyoucat.di

import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.feature.draw.DrawViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.setup.HomeViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.importGames.ImportGamesViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.NetGameViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.setup.NicknameViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.previousGames.PreviousGameViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.previousGames.PreviousGamesViewModel
import dev.develsinthedetails.eatpoopyoucat.feature.sentence.SentenceViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::AppRepository)
    viewModelOf(::SentenceViewModel)
    viewModelOf(::PreviousGamesViewModel)
    viewModel { PreviousGameViewModel(get(), get()) }
    viewModelOf(::HomeViewModel)
    viewModelOf(::DrawViewModel)
    viewModelOf(::NicknameViewModel)
    viewModelOf(::ImportGamesViewModel)
    viewModelOf(::NetGameViewModel)
}
