package dev.develsinthedetails.eatpoopyoucat.feature.netPlay

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.Client
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class JoinNetGameViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: AppRepository,
    private val appSettings: AppSettings,
    private val client: Client
) : ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _nickname = MutableStateFlow<String>("")
    val nickname = _nickname.asStateFlow()

    private var gameId: Uuid? = null
    private var playerAddress: String? = null

    init {
        viewModelScope.launch {
            // todo all the network stuff

            _isLoading.value = false
        }
    }

    fun initFromDeepLink(parsedGameId: Uuid, parsedAddress: String) {
        this.gameId = parsedGameId
        this.playerAddress = parsedAddress
    }

    fun updateNickname(newName: String) {
        _nickname.value = newName
    }

    fun onYesPlay() {
        viewModelScope.launch {
            _isLoading.value = true
            viewModelScope.launch {
                val game = client.getGame(playerAddress!!.toUri(), gameId!!)
                if (game !==null) {
                    repository.updateGame(game.game)
                    repository.updateRosters(game.roster)
             }

            }
            _isLoading.value = false

        }
    }
}