package dev.develsinthedetails.eatpoopyoucat.feature.previousGames

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.app.PreviousGameDetails
import dev.develsinthedetails.eatpoopyoucat.app.UuidNavType
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.NetGame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf
import kotlin.uuid.Uuid

data class InProgressGamesUiState(
    val address: String = "Sever Offline"
)

class PreviousGameDetailsViewModel(
    state: SavedStateHandle,
    appSettings: AppSettings,
    private val repository: AppRepository,
) : ViewModel() {
    val playerId = appSettings.playerId
    private val _uiState = MutableStateFlow(
        InProgressGamesUiState(
            address = "Server Offline"
        )
    )
    val uiState: StateFlow<InProgressGamesUiState> = _uiState.asStateFlow()

    private val typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
    private val route = state.toRoute<PreviousGameDetails>(typeMap)
    val gameId: Uuid = checkNotNull(route.gameId)
    val game: Flow<NetGame?> = repository.getNetGameFlow(gameId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun updateAddress(link: String?) {
        _uiState.update { state ->
            state.copy(
                address = link ?: ""
            )
        }
    }

    fun gameOverMan() {
        viewModelScope.launch {
            val netGame = game.first() ?: return@launch
            if (netGame.entries.isEmpty()) return@launch
            println("gameOverMan: $netGame")
            println("gameOverMan: ${netGame.roster}")
            if (netGame.roster.any { it.playerId == playerId && it.isLeader }) {
                println("gameOverMan: leader")
                var game = netGame.game
                val turns = netGame.entries.size
                game = game.copy(turns = turns)
                repository.upsertGame(game)
            }
        }
    }
}