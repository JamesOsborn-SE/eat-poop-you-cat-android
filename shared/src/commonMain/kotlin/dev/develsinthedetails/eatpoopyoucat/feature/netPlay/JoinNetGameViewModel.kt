package dev.develsinthedetails.eatpoopyoucat.feature.netPlay

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.app.Join
import dev.develsinthedetails.eatpoopyoucat.app.UuidNavType
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.GameClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    state: SavedStateHandle,
) : ViewModel() {

    private val typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
    private val route = state.toRoute<Join>(typeMap)
    private val gameId: Uuid = checkNotNull(route.gameId)
    private val _uiState = MutableStateFlow(
        JoinUiState(playerId = appSettings.playerId, nickname = appSettings.nickname)
    )
    val uiState: StateFlow<JoinUiState> = _uiState.asStateFlow()

    init {
        repository.getGameWithRostersFlow(gameId)
            .onEach { gameData ->
                val isAlreadyInGame = gameData?.roster?.any { it.playerId == appSettings.playerId }?:false

                if (isAlreadyInGame) {
                    _uiState.update {
                        it.copy(
                            isInGameAlready = true,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isInGameAlready = false,
                            isLoading = false
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
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