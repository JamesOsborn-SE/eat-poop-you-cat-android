package dev.develsinthedetails.eatpoopyoucat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.develsinthedetails.eatpoopyoucat.SharedPref
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.Entry
import dev.develsinthedetails.eatpoopyoucat.data.GameWithEntries
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportGamesViewModel @Inject constructor(
    val repository: AppRepository,
) : ViewModel() {

    private val _isFinished: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isFinished = _isFinished.asLiveData()
    private var _numberOfGamesAdded: MutableStateFlow<Int> = MutableStateFlow(0)
    val numberOfGamesAdded = _numberOfGamesAdded.asLiveData()

    private var _numberOfEntriesAdded: MutableStateFlow<Int> = MutableStateFlow(0)
    val numberOfEntriesAdded = _numberOfEntriesAdded.asLiveData()

    private fun addGame(gameWithEntries: GameWithEntries) {
        viewModelScope.launch {
            repository.createGame(gameWithEntries.game)
            addEntries(gameWithEntries.entries)
            _numberOfGamesAdded.emit(++_numberOfGamesAdded.value)
        }
    }
    private fun addEntries(entries: List<Entry>) {
        viewModelScope.launch {
            entries.forEach {
                val newEntry = it.copy(playerId = SharedPref.playerId())
                repository.createEntry(newEntry)
                _numberOfEntriesAdded.emit(++_numberOfEntriesAdded.value)
            }
        }
    }

    suspend fun addGames(gamesToImport: List<GameWithEntries>, closeStream: () -> Unit) {
        val existingGameIds = repository.getAllGames().map { g->g.id }
        gamesToImport.forEach {
            if (!existingGameIds.any { g -> g == it.game.id })
                addGame(it)
            else {
                val existingEntries = repository.getEntriesAsync(it.game.id.toString())
                val missingEntries =
                    it.entries.filter { e -> !existingEntries.any { ee -> ee.id == e.id } }
                if(missingEntries.any())
                    addEntries(missingEntries)
            }
        }
        closeStream()
        _isFinished.emit(true)
    }
}