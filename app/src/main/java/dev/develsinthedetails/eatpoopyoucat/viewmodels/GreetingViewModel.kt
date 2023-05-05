package dev.develsinthedetails.eatpoopyoucat.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.SharedPref
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.Coordinates
import dev.develsinthedetails.eatpoopyoucat.data.Entry
import dev.develsinthedetails.eatpoopyoucat.data.Game
import dev.develsinthedetails.eatpoopyoucat.data.Line
import dev.develsinthedetails.eatpoopyoucat.data.LineSegment
import dev.develsinthedetails.eatpoopyoucat.data.Player
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GreetingViewModel @Inject constructor(
    private val repository: AppRepository,
    private val application: Application
) : ViewModel() {

    var isLoading by mutableStateOf(false)

    var userName by mutableStateOf("")
        private set

    private val playerId: UUID = SharedPref.playerId()


    init {
        viewModelScope.launch {
        val player = repository.getPlayer(playerId)
        userName = player.first()?.name ?: application.getString(R.string.default_nickname)
        updatePlayer(userName)
        }
    }

    fun updateNickName(nickname: String) {
        userName = nickname
        updatePlayer(nickname)
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

    fun saveNewGame(entryId: UUID, onNavigateToSentence: () -> Unit) {
        isLoading = true
        val gameId = UUID.randomUUID()
        Log.i("test", "${entryId.version()}")
        viewModelScope.launch {
            repository.createGame(
                Game(
                    id = gameId,
                    timeout = null,
                    turns = null
                )
            )
            // TODO fo testing delete later
            val lineSegments: MutableList<LineSegment> = mutableListOf()
            lineSegments.add(LineSegment(start= Coordinates(xValue=292.9756f, yValue=189.96094f), end= Coordinates(xValue=292.9756f, yValue=189.96094f)))
            val drawingLines: MutableList<Line> = mutableListOf()
            drawingLines.add(Line(lineSegments))
            repository.createEntry(
                Entry(
                    id = entryId,
                    playerId = playerId,
                    sequence = 0,
                    gameId = gameId,
                    timePassed = 0,
                    sentence = null,
                    drawing = null //(Drawing(drawingLines).toByteArray()),
                )
            )
            onNavigateToSentence.invoke()
            isLoading = false
        }
    }

}