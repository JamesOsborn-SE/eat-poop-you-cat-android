package dev.develsinthedetails.eatpoopyoucat.app

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class AppSettings(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        val PLAYER_ID = stringPreferencesKey("PLAYER_ID")
        val USE_NICKNAMES = stringPreferencesKey("USE_NICKNAMES")
    }

    val isReadyFlow = MutableStateFlow(false)

    var playerId: Uuid = Uuid.NIL
        private set

    private val appScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        appScope.launch {
            val prefs = dataStore.data.first()
            val savedId = prefs[PLAYER_ID]

            if (savedId != null) {
                playerId = Uuid.parse(savedId)
            } else {
                val newId = Uuid.random().toString()
                dataStore.edit { it[PLAYER_ID] = newId }
                playerId = Uuid.parse(newId)
            }
            waitForReady()
            isReadyFlow.emit(true)
        }
    }

    val useNicknamesFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[USE_NICKNAMES]?.toBoolean() ?: false
    }

    suspend fun setUseNicknames(useNicknames: Boolean) {
        dataStore.edit { prefs ->
            prefs[USE_NICKNAMES] = useNicknames.toString()
        }
    }

    tailrec suspend fun waitForReady(): Boolean {
        if (playerId != Uuid.NIL) return true
        delay(100.milliseconds)
        return waitForReady()
    }
}