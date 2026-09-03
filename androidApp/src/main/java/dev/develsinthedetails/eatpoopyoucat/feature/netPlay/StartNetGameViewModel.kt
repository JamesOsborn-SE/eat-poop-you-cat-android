package dev.develsinthedetails.eatpoopyoucat.feature.netPlay

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.app.StartNetGame
import dev.develsinthedetails.eatpoopyoucat.app.UuidNavType
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.NetworkUtils
import dev.develsinthedetails.eatpoopyoucat.core.utilities.validateNickname
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf
import kotlin.time.Clock
import kotlin.uuid.Uuid

data class NewNetGameUiState(
    val gameId: Uuid,
    val gameMode: GameMode,
    val player: Player = Player(Uuid.NIL, ""),
    val address: String = "Server Offline",

    val isError: Boolean = false,
    val isLoading: Boolean = true,
    val timeout: Int = 5,
    val turnTimeout: Int = 5,

    val nicknameError: Int? = null,
    val nicknameIsSatisfied: Boolean = false,

    )

@OptIn(ExperimentalCoroutinesApi::class)
class StartNetGameViewModel(
    state: SavedStateHandle,
    val repository: AppRepository,
    appSettings: AppSettings
) : ViewModel() {
    private val typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
    private val route = state.toRoute<StartNetGame>(typeMap)

    private val _uiState = MutableStateFlow(
        NewNetGameUiState(
            gameId = route.gameId,
            gameMode = route.gameMode,
            address = NetworkUtils.getLocalIpAddress() ?: "Server Offline"
        )
    )
    val uiState: StateFlow<NewNetGameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val player = repository.getPlayer(appSettings.playerId)
            if (player != null)
                _uiState.update { it.copy(player = player, isLoading = false) }

        }
    }

    fun updateTurnTimeOut(int:String?){
        val turnTimeout = int?.toInt()?:0
        _uiState.update { it.copy(turnTimeout=turnTimeout) }
    }
    fun updateTimeOut(int:String?){
        val timeout = int?.toInt()?:0
        _uiState.update { it.copy(timeout=timeout) }
    }

    fun updateAddress(link: String?) {
        _uiState.update { state ->
            state.copy(
                address = link ?: ""
            )
        }
    }

    fun updateNickname(nickname: String?) {
        _uiState.update { state ->
            state.copy(
                player = _uiState.value.player.copy(nickname = nickname ?: "")
            )
        }
        isNicknameValid()
    }

    fun isNicknameValid(): Boolean {
        val isValid = validateNickname(_uiState.value.player.nickname, listOf())
        if (!isValid) {
            _uiState.update {
                it.copy(
                    nicknameError = R.string.no_nickname_chosen_warning
                )
            }
        } else {
            _uiState.update { it.copy(nicknameIsSatisfied = true) }
        }
        return isValid
    }

    fun createRoster() {
        if (isNicknameValid())
            viewModelScope.launch {
                val state = _uiState.value
                repository.upsertPlayer(state.player)
                repository.createGame(Game(state.gameId, state.timeout,null, Clock.System.now(), gameMode = state.gameMode))
                repository.upsertRoster(
                    Roster(
                        state.gameId, state.player.id, state.player.nickname, state.address, -1, true,
                        Clock.System.now()
                    )
                )
            }
    }
}