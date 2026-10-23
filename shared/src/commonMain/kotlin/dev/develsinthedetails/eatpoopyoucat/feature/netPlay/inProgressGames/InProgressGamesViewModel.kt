package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.inProgressGames

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithRosters
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class InProgressGamesViewModel(
    appSettings: AppSettings,
    val repository: AppRepository,
) : ViewModel() {

    fun deleteGame(gameId: Uuid) {
        viewModelScope.launch {
            repository.deleteGame(gameId)
        }
    }

    fun endGame(gameId: Uuid) {
        viewModelScope.launch {
            val gameWithEntries = repository.getGameWithEntries(gameId)
            if (gameWithEntries?.game != null) {
                if (gameWithEntries.entries.size > 0) {
                    repository.upsertGame(gameWithEntries.game.copy(turns = gameWithEntries.entries.size))
                } else {
                    repository.deleteGame(gameId)
                }
            }
        }
    }

    val playerId = appSettings.playerId
    val games: StateFlow<List<GameWithRosters>?> = repository.getInProgressGamesWithRostersFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )
}
