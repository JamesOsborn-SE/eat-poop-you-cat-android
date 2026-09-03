package dev.develsinthedetails.eatpoopyoucat.feature.importGames

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ImportGamesViewModel(
    val repository: AppRepository,
    private val appSettings: AppSettings,
) : ViewModel() {

    private val _isFinished: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isFinished = _isFinished.asLiveData()
    private var _numberOfGamesAdded: MutableStateFlow<Int> = MutableStateFlow(0)
    val numberOfGamesAdded = _numberOfGamesAdded.asLiveData()
    private var _numberOfEntriesAdded: MutableStateFlow<Int> = MutableStateFlow(0)
    val numberOfEntriesAdded = _numberOfEntriesAdded.asLiveData()
    init {
        viewModelScope.launch {
            val player = repository.getPlayer(appSettings.playerId)
            if (player == null)
                repository.createPlayer(Player(appSettings.playerId, ""))
        }
    }
    private suspend fun addGame(gameWithEntries: GameWithEntries) {
        repository.createGame(gameWithEntries.game)
        addEntries(gameWithEntries.entries)
        _numberOfGamesAdded.emit(++_numberOfGamesAdded.value)
    }

    private suspend fun addEntries(entries: List<Entry>) {
        entries.forEach {
            val player = repository.getPlayer(it.playerId)
            if (player == null)
                repository.createPlayer(Player(it.playerId, it.localPlayerName?:""))
            repository.createEntry(it)
            _numberOfEntriesAdded.emit(++_numberOfEntriesAdded.value)
        }
    }

    suspend fun addGames(gamesToImport: List<GameWithEntries>, closeStream: () -> Unit) {
        val existingGameIds = repository.getAllGames().map { g -> g.id }
        gamesToImport.forEach {
            if (!existingGameIds.any { g -> g == it.game.id })
                addGame(it)
            else {
                val existingEntries = repository.getEntries(it.game.id)
                val missingEntries =
                    it.entries.filter { e -> !existingEntries.any { ee -> ee.id == e.id } }
                if (missingEntries.any())
                    addEntries(missingEntries)
            }
        }
        closeStream()
        _isFinished.emit(true)
    }
}