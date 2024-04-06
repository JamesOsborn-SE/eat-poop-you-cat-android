package dev.develsinthedetails.eatpoopyoucat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.GameWithEntries
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportGamesViewModel @Inject constructor(
    val repository: AppRepository,
) : ViewModel() {

    private var numberOfGamesAdded: MutableStateFlow<Int> = MutableStateFlow(-1)
    val numberOfGamesAddedLiveData = numberOfGamesAdded.asLiveData()

    private fun addGame(gameWithEntries: GameWithEntries) {
        viewModelScope.launch {

            repository.createGame(gameWithEntries.game)
            gameWithEntries.entries.forEach {
                repository.createEntry(it)
            }
        }
    }

    suspend fun addGames(games: List<GameWithEntries>, closeStream: () -> Unit) {
        val existingGamesIds = repository.getAllGamesWithEntriesNow().map { it.game.id }

        val missingGames = games.filter { !existingGamesIds.contains(it.game.id) }
        missingGames.forEach {
            addGame(it)
        }
            numberOfGamesAdded.emit(missingGames.count())

        closeStream()
    }
}