package dev.develsinthedetails.eatpoopyoucat.data

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { AppDatabase.getInstance(androidContext()) }

    single { get<AppDatabase>().gameDao() }
    single { get<AppDatabase>().entryDao() }
    single { get<AppDatabase>().playerDao() }
}