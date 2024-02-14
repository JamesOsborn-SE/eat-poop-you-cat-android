package dev.develsinthedetails.eatpoopyoucat.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.utilities.ID
import javax.inject.Inject

@HiltViewModel
class PreviousGameViewModel @Inject constructor(
    state: SavedStateHandle,
    repository: AppRepository,
) : ViewModel() {
    private val gameId: String = checkNotNull(state.get<String>(ID))
    val gameWithEntries = repository.getGameWithEntries(gameId).asLiveData()
}