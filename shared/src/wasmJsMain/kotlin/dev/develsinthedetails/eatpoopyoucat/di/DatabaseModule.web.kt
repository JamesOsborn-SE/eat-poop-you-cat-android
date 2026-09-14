package dev.develsinthedetails.eatpoopyoucat.di

import androidx.room3.Room
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import dev.develsinthedetails.eatpoopyoucat.core.utilities.DATABASE_NAME
import dev.develsinthedetails.eatpoopyoucat.data.local.AppDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import org.w3c.dom.Worker

actual val platformDatabaseModule: Module = module {
    single<AppDatabase> {
        Room.databaseBuilder<AppDatabase>(
            name = DATABASE_NAME
        )
            .setDriver(WebWorkerSQLiteDriver(Worker("sqlite.worker.js")))
            .build()
    }
}