package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.inProgressGames

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.app.InProgressGameDetails
import dev.develsinthedetails.eatpoopyoucat.app.appTypeMap
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.SharedKtorServer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid


data class InProgressGamesUiState(
    val address: String = "Sever Offline"
)


class InProgressGameDetailsViewModel(
    appSettings: AppSettings,
    private val repository: AppRepository,
    state: SavedStateHandle,
    private val server: SharedKtorServer
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        InProgressGamesUiState(
            address = "Server Offline"
        )
    )

    val uiState: StateFlow<InProgressGamesUiState> = _uiState.asStateFlow()

    fun updateAddress(link: String?) {
        _uiState.update { state ->
            state.copy(
                address = link ?: ""
            )
        }
    }

    val playerId = appSettings.playerId
    private val typeMap = appTypeMap
    private val route = state.toRoute<InProgressGameDetails>(typeMap)
    private val gameId: Uuid = checkNotNull(route.gameId)
    val players: Flow<List<Roster>?> = repository.getRostersByGameFlow(gameId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    val game: Flow<GameWithEntries?> = repository.getGameWithEntriesFlow(gameId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun gameOverMan(){
        viewModelScope.launch {
            val gameWithEntries = game.first() ?: return@launch
            var game = gameWithEntries.game
            val turns = gameWithEntries.entries.size
            game = game.copy(turns = turns)
            repository.upsertGame(game)
            server.sendGameComplete(game)
        }
    }

}