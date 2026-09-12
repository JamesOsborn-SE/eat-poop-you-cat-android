package dev.develsinthedetails.eatpoopyoucat.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, context.packageName))
    }
)

actual val platformDataStoreModule: Module = module {
    single<DataStore<Preferences>> {
        androidContext().dataStore
    }
    single { AppSettings(get()) }
}