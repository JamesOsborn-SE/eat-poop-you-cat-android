package dev.develsinthedetails.eatpoopyoucat.feature.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class NewGameViewModel(
    private val repository: AppRepository,
) : ViewModel() {
    val gameId = Uuid.random()
    var game = Game(
            id = gameId,
            timeout = null,
            turns = null,
        )
    fun saveNewGame(gameMode: GameMode): Game {
        game = game.copy(gameMode=gameMode)
        viewModelScope.launch {
            repository.createGame(game)
        }
        return game
    }
}