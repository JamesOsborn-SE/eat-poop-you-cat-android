package dev.develsinthedetails.eatpoopyoucat.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, context.packageName))
    }
)

class AppSettings(private val context: Context) {

    companion object {
        val PLAYER_ID = stringPreferencesKey("PLAYER_ID")
        val USE_NICKNAMES = stringPreferencesKey("USE_NICKNAMES")
    }

    private val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val deferredPlayerId = appScope.async {
        val prefs = context.dataStore.data.first()
        val savedId = prefs[PLAYER_ID]

        if (savedId != null) {
            Uuid.parse(savedId)
        } else {
            val newId = Uuid.random().toString()
            write(PLAYER_ID, newId)
            Uuid.parse(newId)
        }
    }
    val useNicknamesFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[USE_NICKNAMES]?.toBoolean() ?: false
    }
    suspend fun getPlayerId(): Uuid {
        return deferredPlayerId.await()
    }

    suspend fun setUseNicknames(useNicknames: Boolean) {
        write(USE_NICKNAMES, useNicknames.toString())
    }

    private suspend fun write(key: Preferences.Key<String>, value: String?) {
        context.dataStore.edit { prefs ->
            if (value != null) {
                prefs[key] = value
            } else {
                prefs.remove(key)
            }
        }
    }
}