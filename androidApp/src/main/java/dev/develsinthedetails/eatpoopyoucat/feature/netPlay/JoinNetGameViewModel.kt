package dev.develsinthedetails.eatpoopyoucat.feature.netPlay

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.core.utilities.NetworkUtils
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.Client
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.uuid.Uuid


data class JoinUiState(
    val gameId: Uuid,
    val player: Player = Player(Uuid.NIL, ""),
    val address: String = "Server Offline",

    val isError: Boolean = false,
    val isLoading: Boolean = true,
    val timeout: Int = 5,
    val turnTimeout: Int = 5,

    val nicknameError: Int? = null,
    val nicknameIsSatisfied: Boolean = false,

    )

class JoinNetGameViewModel(
    private val state: SavedStateHandle,
    private val repository: AppRepository,
    private val appSettings: AppSettings,
    private val client: Client
) : ViewModel() {
    private var gameId: Uuid? = null
    private var playerAddress: String? = null
    private val _uiState = MutableStateFlow(
        JoinUiState(
            gameId = Uuid.NIL,
            address = NetworkUtils.getLocalIpAddress() ?: "Server Offline"
        )
    )
    val uiState: StateFlow<JoinUiState> = _uiState.asStateFlow()

    fun updateAddress(link: String?) {
        _uiState.update { state ->
            state.copy(
                address = link ?: ""
            )
        }
    }

    init {
        viewModelScope.launch {
            val player = repository.getPlayer(appSettings.playerId)
            if (player != null) {
                _uiState.update { it.copy(player = player) }
            }
        }
    }

    fun initFromDeepLink(parsedGameId: Uuid, parsedAddress: String) {
        this.gameId = parsedGameId
        this.playerAddress = parsedAddress

        _uiState.update { it.copy(isLoading = false) }

    }

    fun updateNickname(newName: String) {
        _uiState.update { it.copy(player = _uiState.value.player.copy(nickname = newName)) }
    }

    fun onYesPlay() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            if (playerAddress != null && gameId != null)
                viewModelScope.launch {
                    val player = _uiState.value.player

                    val game = client.getGame(playerAddress!!.toUri(), gameId!!)
                    if (game !== null) {
                        val myRoster = Roster(
                            gameId!!,
                            player.id,
                            player.nickname,
                            _uiState.value.address,
                            -1,
                            false,
                            Clock.System.now()
                        )
                        client.joinGame(playerAddress!!.toUri(), myRoster)
                        repository.updateGame(game.game)
                        for (r in game.roster) {
                            repository.upsertPlayer(Player(r.playerId, r.nickname, r.address))
                        }
                        repository.upsertPlayer(player)
                        repository.upsertRosters(game.roster)

                        repository.upsertRoster(myRoster)
                        client.joinGame(playerAddress!!.toUri(), myRoster)
                    }

                }
            else {
                TODO() // error
            }
            _uiState.update { it.copy(isLoading = false) }


        }
    }
}