package dev.develsinthedetails.eatpoopyoucat.di

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import androidx.sqlite.execSQL
import dev.develsinthedetails.eatpoopyoucat.core.utilities.DATABASE_NAME
import dev.develsinthedetails.eatpoopyoucat.data.local.AppDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import org.w3c.dom.Worker

private fun getWorker(): Worker =
    js("""new Worker(window.location.origin + "/sqlite-worker.js", { type: "module" })""")

actual val platformDatabaseModule: Module = module {
    single<AppDatabase> {
        Room.databaseBuilder<AppDatabase>(name = DATABASE_NAME)
            .setDriver(WebWorkerSQLiteDriver(getWorker()))
            .addCallback(object : RoomDatabase.Callback() {
                override suspend fun onOpen(connection: SQLiteConnection) {
                    super.onOpen(connection)
                    connection.execSQL("PRAGMA foreign_keys = ON;")
                }
            })
            .build()
    }
}