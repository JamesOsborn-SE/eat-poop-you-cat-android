package dev.develsinthedetails.eatpoopyoucat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreviousGamesViewModel @Inject constructor(
    val repository: AppRepository,
) : ViewModel() {
    fun deleteGame(gameId: String) {
        viewModelScope.launch {
            repository.deleteGame(gameId)
        }
    }

    val games = repository.getAllGamesWithEntries().asLiveData()
}