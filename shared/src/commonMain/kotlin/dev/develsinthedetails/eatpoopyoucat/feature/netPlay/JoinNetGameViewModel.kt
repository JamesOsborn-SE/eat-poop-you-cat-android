package dev.develsinthedetails.eatpoopyoucat.feature.netPlay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.Client
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid


data class JoinUiState(
    val playerId: Uuid,
    val nickname: String = "",
    val isError: Boolean = false,
    val isLoading: Boolean = true,
    val timeout: Int = 5,
    val turnTimeout: Int = 5,
    val nicknameError: Int? = null,
    val nicknameIsSatisfied: Boolean = false,
    val isInGameAlready: Boolean = false,
)

class JoinNetGameViewModel(
    private val repository: AppRepository,
    private val appSettings: AppSettings,
    private val client: Client
) : ViewModel() {
    private lateinit var gameId: Uuid
    private val _uiState = MutableStateFlow(
        JoinUiState(playerId = appSettings.playerId, nickname = appSettings.nickname)
    )
    val uiState: StateFlow<JoinUiState> = _uiState.asStateFlow()

    fun initFromDeepLink(gameId: Uuid) {
        this.gameId = gameId
        viewModelScope.launch {
            val game = client.getGame(gameId)
            if (game != null && game.roster.any { it.playerId == _uiState.value.playerId }) {
                repository.upsertGameWithRosters(game)
                val gameWithEntries =
                    client.updateGame(GameWithEntries(game = game.game, entries = listOf()))
                repository.upsertEntries(gameWithEntries)
                println("already in game")
                _uiState.update { it.copy(isInGameAlready = true) }
            }
        }
        _uiState.update { it.copy(isLoading = false) }
    }

    fun updateNickname(newName: String) {
        _uiState.update { it.copy(nickname = newName) }
    }

    fun onYesPlay() {
        val state = _uiState.value
        if (!state.nickname.isBlank()) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                println("DEBUG: Game=$gameId")
                var player = repository.getPlayer(state.playerId)
                if (player == null)
                    player = Player(state.playerId, "")
                player = player.copy(nickname = state.nickname)
                appSettings.setNickname(player.nickname)
                repository.upsertPlayer(player)

                val gameWithRosters = client.getGame(gameId)
                if (gameWithRosters != null) {
                    val gameWithEntries =
                        client.updateGame(GameWithEntries(gameWithRosters.game, emptyList()))
                    val myRoster = Roster(
                        gameId,
                        player.id,
                        player.nickname
                    )

                    repository.upsertGameWithRosters(gameWithRosters)
                    repository.upsertRoster(myRoster)
                    repository.upsertEntries(gameWithEntries)
                    client.joinGame(myRoster)
                    _uiState.update { it.copy(isInGameAlready = true) }
                } else {
                    _uiState.update { it.copy(isError = true) }
                }
                _uiState.update { it.copy(isLoading = false) }
            }
        } else {
            _uiState.update { it.copy(nicknameError = 1, isLoading = false) }
        }
    }
}