package dev.develsinthedetails.eatpoopyoucat.feature.previousGames

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.NetGame
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.ServerManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class PreviousGamesViewModel(
    appSettings: AppSettings,
    val repository: AppRepository,
    val serverManager: ServerManager
) : ViewModel() {

    val games = repository.getAllGamesFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = null
    )

    val playerId = appSettings.playerId

    fun deleteGame(gameId: Uuid) {
        viewModelScope.launch {
            repository.deleteGame(gameId)
        }
    }

    fun endGame(gameId: Uuid) {
        viewModelScope.launch {
            val gameWithEntries = repository.getGameWithEntries(gameId)
            if (gameWithEntries?.game != null) {
                if (gameWithEntries.entries.isNotEmpty()) {
                    repository.upsertGame(gameWithEntries.game.copy(turns = gameWithEntries.entries.size))
                } else {
                    repository.deleteGame(gameId)
                }
            }
        }
    }

    fun cleanup(invalidGames: List<NetGame>) {
        viewModelScope.launch {
            invalidGames.forEach {
                repository.deleteGame(it.game.id)
            }
        }
    }
}