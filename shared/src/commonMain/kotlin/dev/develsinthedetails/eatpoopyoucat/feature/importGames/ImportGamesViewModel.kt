package dev.develsinthedetails.eatpoopyoucat.feature.importGames

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.NetGame
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImportGamesViewModel(
    val repository: AppRepository,
    private val appSettings: AppSettings,
) : ViewModel() {

    private val _isFinished = MutableStateFlow(false)
    val isFinished = _isFinished.asStateFlow()

    private val _numberOfGamesAdded = MutableStateFlow(0)
    val numberOfGamesAdded = _numberOfGamesAdded.asStateFlow()

    private val _numberOfEntriesAdded = MutableStateFlow(0)
    val numberOfEntriesAdded = _numberOfEntriesAdded.asStateFlow()

    init {
        viewModelScope.launch {
            val player = repository.getPlayer(appSettings.playerId)
            if (player == null)
                repository.createPlayer(Player(appSettings.playerId, ""))
        }
    }

    suspend fun addGames(gamesToImport: List<NetGame>, closeStream: () -> Unit) {
        gamesToImport.forEach {
            repository.upsertNetGame(it, appSettings.playerId)
            _numberOfGamesAdded.emit(_numberOfGamesAdded.value + 1)
            _numberOfEntriesAdded.emit(_numberOfEntriesAdded.value + it.entries.size)
        }
        closeStream()
        _isFinished.emit(true)
    }
}