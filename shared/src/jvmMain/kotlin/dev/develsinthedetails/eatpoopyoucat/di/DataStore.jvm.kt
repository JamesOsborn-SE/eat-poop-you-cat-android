package dev.develsinthedetails.eatpoopyoucat.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.databasesDir
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val platformDataStoreModule: Module = module {
    single<DataStore<Preferences>> {
        val filesDir = FileKit.databasesDir.file

        if (!filesDir.exists()) {
            filesDir.mkdirs()
        }

        val dataStoreFile = File(filesDir, "settings.preferences_pb")

        PreferenceDataStoreFactory.createWithPath(
            produceFile = { dataStoreFile.absolutePath.toPath() }
        )
    }

    single { AppSettings(get()) }
}