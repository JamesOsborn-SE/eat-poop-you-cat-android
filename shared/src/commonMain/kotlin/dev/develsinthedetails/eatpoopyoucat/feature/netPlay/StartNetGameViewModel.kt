package dev.develsinthedetails.eatpoopyoucat.feature.netPlay

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.app.StartNetGame
import dev.develsinthedetails.eatpoopyoucat.app.appTypeMap
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.validateNickname
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import eatpoopyoucat.shared.generated.resources.Res
import eatpoopyoucat.shared.generated.resources.no_nickname_chosen_warning
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock
import kotlin.uuid.Uuid

data class NewNetGameUiState(
    val gameId: Uuid,
    val gameMode: GameMode,
    val playerId: Uuid,
    val address: String = "Server Offline",

    val isError: Boolean = false,
    val isLoading: Boolean = false,
    val timeout: Int = 5,
    val turnTimeout: Int = 5,
    val nickname: String = "",
    val nicknameError: String? = null,
    val nicknameIsSatisfied: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class StartNetGameViewModel(
    state: SavedStateHandle,
    val repository: AppRepository,
    val appSettings: AppSettings
) : ViewModel() {
    private val typeMap = appTypeMap
    private val route = state.toRoute<StartNetGame>(typeMap)

    private val _uiState = MutableStateFlow(
        NewNetGameUiState(
            gameId = route.gameId,
            gameMode = route.gameMode,
            nickname = appSettings.nickname,
            playerId = appSettings.playerId,
            address = "Server Offline"
        )
    )
    val uiState: StateFlow<NewNetGameUiState> = _uiState.asStateFlow()

    fun updateTurnTimeOut(int: String?) {
        val turnTimeout = int?.toInt() ?: 0
        _uiState.update { it.copy(turnTimeout = turnTimeout) }
    }

    fun updateTimeOut(int: String?) {
        val timeout = int?.toInt() ?: 0
        _uiState.update { it.copy(timeout = timeout) }
    }

    fun updateAddress(link: String?) {
        _uiState.update { state ->
            state.copy(
                address = link ?: ""
            )
        }
    }

    fun updateNickname(nickname: String) {
        _uiState.update { it.copy(nickname = nickname) }
    }

    fun isNicknameValid(): Boolean {
        val isValid = validateNickname(_uiState.value.nickname, listOf())
        if (!isValid) {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        nicknameError = getString(Res.string.no_nickname_chosen_warning)
                    )
                }
            }
        } else {
            _uiState.update { it.copy(nicknameIsSatisfied = true) }
        }
        return isValid
    }

    fun startNetGame() {
        if (isNicknameValid())
            viewModelScope.launch {
                val state = _uiState.value
                appSettings.setNickname(state.nickname)
                var player = repository.getPlayer(state.playerId)
                if (player == null)
                    player = Player(state.playerId, "")
                player = player.copy(nickname = state.nickname)

                repository.upsertPlayer(player)
                repository.createGame(
                    Game(
                        state.gameId,
                        state.timeout,
                        null,
                        Clock.System.now(),
                        gameMode = state.gameMode
                    )
                )
                repository.upsertRoster(
                    Roster(
                        state.gameId,
                        player.id,
                        isLeader = true,
                        nickname = player.nickname
                    )
                )
            }
    }
}