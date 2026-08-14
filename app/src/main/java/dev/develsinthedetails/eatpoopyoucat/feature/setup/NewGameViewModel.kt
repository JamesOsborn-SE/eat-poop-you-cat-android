package dev.develsinthedetails.eatpoopyoucat.feature.setup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.app.NewGame
import dev.develsinthedetails.eatpoopyoucat.app.UuidNavType
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf
import kotlin.uuid.Uuid

class NewGameViewModel(
    state: SavedStateHandle,
    private val repository: AppRepository,
    private val appSettings: AppSettings,
) : ViewModel() {
    val gameId = Uuid.random()
    val entryId = Uuid.random()
    var game = Game(
            id = gameId,
            timeout = null,
            turns = null,
        )
    val entry = Entry(
        id = entryId,
        playerId = appSettings.playerId,
        sequence = 0,
        gameId = gameId,
        timePassed = 0,
        sentence = null,
        drawing = null
    )
    fun saveNewGame(gameMode: GameMode): Game {
        game = game.copy(gameMode=gameMode)
        viewModelScope.launch {
            repository.createGame(game)
            repository.createEntry(entry)
        }
        return game
    }

    private val typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
    private val route = state.toRoute<NewGame>(typeMap)
}