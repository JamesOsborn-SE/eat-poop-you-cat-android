package dev.develsinthedetails.eatpoopyoucat.feature.setup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class HomeViewModel(
    private val repository: AppRepository,
    private val appSettings: AppSettings,
) : ViewModel() {
    var isLoading by mutableStateOf(false)

    private var userName by mutableStateOf("")
    private lateinit var playerId: Uuid


    init {
        viewModelScope.launch {
            playerId=appSettings.getPlayerId()
            updatePlayer(userName)
        }
    }

    private fun updatePlayer(nickname: String) {
        val newPlayer = Player(playerId, nickname)
        viewModelScope.launch {
            val player = repository.getPlayer(playerId).first()

            if (player == null) {
                repository.createPlayer(newPlayer)
            } else {
                repository.updatePlayer(newPlayer)
            }
        }
    }

    fun saveNewGame(entryId: Uuid, onNavigateToSentence: () -> Unit) {
        isLoading = true
        val gameId = Uuid.random()

        viewModelScope.launch {
            repository.createGame(
                Game(
                    id = gameId,
                    timeout = null,
                    turns = null
                )
            )
            repository.createEntry(
                Entry(
                    id = entryId,
                    playerId = playerId,
                    sequence = 0,
                    gameId = gameId,
                    timePassed = 0,
                    sentence = null,
                    drawing = null
                )
            )
            onNavigateToSentence.invoke()
            isLoading = false
        }
    }
}