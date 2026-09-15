package dev.develsinthedetails.eatpoopyoucat.feature.setup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class HomeViewModel(
    private val repository: AppRepository,
    private val appSettings: AppSettings,
) : ViewModel() {
    var isLoading by mutableStateOf(false)
    var useNicknames = appSettings.useNicknamesFlow
    private var nickname by mutableStateOf("")
    private val playerId = appSettings.playerId


    init {
        viewModelScope.launch {
            updatePlayer(nickname)
        }
    }

    private fun updatePlayer(nickname: String) {
        val newPlayer = Player(playerId, nickname)
        viewModelScope.launch {
            val player = repository.getPlayer(playerId)

            if (player == null) {
                repository.createPlayer(newPlayer)
            }
        }
    }

    fun saveNewGame(onToSentence: () -> Unit) {
        isLoading = true
        val gameId = Uuid.random()
        viewModelScope.launch {
            val player = repository.getPlayer(playerId)
            println("DEBUG: $player")
            println("DEBUG: $playerId")
            if (player == null) {
                repository.createPlayer(Player(playerId, nickname))
            }
            repository.createGame(
                Game(
                    id = gameId,
                    timeout = null,
                    turns = null
                )
            )
            onToSentence.invoke()
            isLoading = false
        }
    }

    fun updateUseNicknames(enable: Boolean) {
        viewModelScope.launch {
            appSettings.setUseNicknames(!enable)
        }
    }
}