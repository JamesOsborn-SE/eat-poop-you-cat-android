package dev.develsinthedetails.eatpoopyoucat.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDataStoreModule: Module = module {
    single<DataStore<Preferences>> {
        object : DataStore<Preferences> {
            private val memoryFlow = MutableStateFlow(loadFromStorage())

            override val data: Flow<Preferences> = memoryFlow

            override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
                val updatedPrefs = transform(memoryFlow.value)
                memoryFlow.value = updatedPrefs
                saveToStorage(updatedPrefs)
                return updatedPrefs
            }

            private fun saveToStorage(prefs: Preferences) {
                val map: Map<String, String> = prefs.asMap().entries.associate {
                    it.key.name to it.value.toString()
                }

                val jsonString = Json.encodeToString(map)
                localStorage.setItem("my_app_datastore", jsonString)
            }

            private fun loadFromStorage(): Preferences {
                val jsonString =
                    localStorage.getItem("my_app_datastore") ?: return emptyPreferences()

                return try {
                    val parsedMap = Json.decodeFromString<Map<String, String>>(jsonString)
                    val mutablePrefs = emptyPreferences().toMutablePreferences()

                    for ((key, value) in parsedMap) {
                        mutablePrefs[stringPreferencesKey(key)] = value
                    }
                    mutablePrefs
                } catch (_: Exception) {
                    emptyPreferences()
                }
            }
        }
    }

    single { AppSettings(get()) }
}