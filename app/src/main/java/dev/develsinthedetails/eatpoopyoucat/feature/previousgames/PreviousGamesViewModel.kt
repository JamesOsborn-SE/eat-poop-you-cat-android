package dev.develsinthedetails.eatpoopyoucat.feature.previousgames

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class PreviousGamesViewModel(
    val repository: AppRepository,
) : ViewModel() {

    val games = repository.getAllGamesWithEntries().asLiveData()

    fun deleteGame(gameId: Uuid) {
        viewModelScope.launch {
            repository.deleteGame(gameId)
        }
    }

    fun cleanup(invalidGames: List<GameWithEntries>) {
        viewModelScope.launch {
            invalidGames.forEach {
                    repository.deleteGame(it.game.id)
            }
        }
    }
}