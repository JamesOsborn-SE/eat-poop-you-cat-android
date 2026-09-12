package dev.develsinthedetails.eatpoopyoucat.di

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.develsinthedetails.eatpoopyoucat.core.utilities.DATABASE_NAME
import dev.develsinthedetails.eatpoopyoucat.data.local.AppDatabase
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.databasesDir
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val platformDatabaseModule: Module = module {
    single<AppDatabase> {
        val filesDir = FileKit.databasesDir.file
        if (!filesDir.exists()) {
            filesDir.mkdirs()
        }
        val dbFile = File(filesDir, DATABASE_NAME)

        Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}