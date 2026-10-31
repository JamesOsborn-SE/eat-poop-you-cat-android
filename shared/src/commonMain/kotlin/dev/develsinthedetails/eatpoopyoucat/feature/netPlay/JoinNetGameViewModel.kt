package dev.develsinthedetails.eatpoopyoucat.feature.netPlay

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.app.Join
import dev.develsinthedetails.eatpoopyoucat.app.Notifier
import dev.develsinthedetails.eatpoopyoucat.app.UuidNavType
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.GameClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf
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
    private val client: GameClient,
    private val notifier: Notifier,
    state: SavedStateHandle,
) : ViewModel() {

    private val typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
    private val route = state.toRoute<Join>(typeMap)
    private val gameId: Uuid = checkNotNull(route.gameId)
    private val _uiState = MutableStateFlow(
        JoinUiState(playerId = appSettings.playerId, nickname = appSettings.nickname)
    )
    val uiState: StateFlow<JoinUiState> = _uiState.asStateFlow()

    private val _game = repository.getGameWithRostersFlow(gameId)

    init {
        viewModelScope.launch {
            _game.collect { gameData ->
                val isAlreadyInGame =
                    gameData?.roster?.any { it.playerId == appSettings.playerId } == true
                _uiState.update {
                    it.copy(
                        isInGameAlready = isAlreadyInGame,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateNickname(newName: String) {
        _uiState.update { it.copy(nickname = newName) }
    }

    fun onYesPlay() {
        val state = _uiState.value
        if (state.nickname.isBlank()) {
            println("nickname is blank")
            _uiState.update { it.copy(nicknameError = 1, isLoading = false) }
        } else {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                var player = repository.getPlayer(state.playerId)
                if (player == null)
                    player = Player(state.playerId, "")
                player = player.copy(nickname = state.nickname)
                appSettings.setNickname(player.nickname)
                repository.upsertPlayer(player)

                var gameWithRosters = _game.first()

                if (gameWithRosters?.roster?.any { it.playerId == player.id } == true) {
                    _uiState.update { it.copy(isInGameAlready = true) }
                    return@launch
                }

                if (gameWithRosters == null) {
                    gameWithRosters = client.getGame(gameId)
                }

                if (gameWithRosters != null) {
                    val gameWithEntries =
                        client.updateGame(GameWithEntries(gameWithRosters.game, emptyList()))
                    val myRoster = Roster(
                        gameId,
                        player.id,
                        player.nickname
                    )

                    val gotGame = client.joinGame(myRoster)
                    if (gotGame) {
                        repository.upsertGameWithRosters(gameWithRosters)
                        repository.upsertRoster(myRoster)
                        repository.upsertEntries(gameWithEntries)
                    } else{
                        notifier.show("Failed to join game. server ded?")
                    }
                } else {
                    println("DEBUG: gameWithRosters is null")
                    _uiState.update { it.copy(isError = true) }
                }
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}