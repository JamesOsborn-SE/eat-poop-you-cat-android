package dev.develsinthedetails.eatpoopyoucat.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.develsinthedetails.eatpoopyoucat.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.properties.Delegates
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

    private val appScheme = context.getString(R.string.deeplink_scheme)
    val drawDeepLink = "$appScheme://${context.getString(R.string.deeplink_draw)}"
    val sentenceDeepLink = "$appScheme://${context.getString(R.string.deeplink_sentence)}"
    val previousGamesDeepLink = "$appScheme://${context.getString(R.string.deeplink_previous_games)}"
    val previousGameDetailsDeepLink = "$appScheme://${context.getString(R.string.deeplink_previous_game_details)}"
    val playDeepLink = "$appScheme://${context.getString(R.string.deeplink_play)}"

    @Volatile
    var isReady: Boolean = false
        private set

    var playerId: Uuid by Delegates.notNull()
        private set

    private val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        appScope.launch {
            val prefs = context.dataStore.data.first()
            val savedId = prefs[PLAYER_ID]

            if (savedId != null) {
                playerId = Uuid.parse(savedId)
            } else {
                val newId = Uuid.random().toString()
                context.dataStore.edit { it[PLAYER_ID] = newId }
                playerId = Uuid.parse(newId)
            }
            isReady = true
        }
    }

    val useNicknamesFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[USE_NICKNAMES]?.toBoolean() ?: false
    }

    suspend fun setUseNicknames(useNicknames: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[USE_NICKNAMES] = useNicknames.toString()
        }
    }
}