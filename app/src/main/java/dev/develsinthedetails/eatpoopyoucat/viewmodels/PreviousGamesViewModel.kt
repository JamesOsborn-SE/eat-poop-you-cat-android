package dev.develsinthedetails.eatpoopyoucat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.GameWithEntries
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreviousGamesViewModel @Inject constructor(
    val repository: AppRepository,
) : ViewModel() {

    val games = repository.getAllGamesWithEntries().asLiveData()

    fun deleteGame(gameId: String) {
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