package dev.develsinthedetails.eatpoopyoucat.di

import dev.develsinthedetails.eatpoopyoucat.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { AppDatabase.getInstance(androidContext()) }

    single { get<AppDatabase>().gameDao() }
    single { get<AppDatabase>().entryDao() }
    single { get<AppDatabase>().playerDao() }
    single { get<AppDatabase>().rosterDao() }
}