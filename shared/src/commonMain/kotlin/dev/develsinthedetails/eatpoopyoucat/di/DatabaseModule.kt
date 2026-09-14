package dev.develsinthedetails.eatpoopyoucat.di

import dev.develsinthedetails.eatpoopyoucat.data.local.AppDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformDatabaseModule: Module
val databaseModule = module {
    includes(platformDatabaseModule)
    single { get<AppDatabase>().gameDao() }
    single { get<AppDatabase>().entryDao() }
    single { get<AppDatabase>().playerDao() }
    single { get<AppDatabase>().rosterDao() }
}