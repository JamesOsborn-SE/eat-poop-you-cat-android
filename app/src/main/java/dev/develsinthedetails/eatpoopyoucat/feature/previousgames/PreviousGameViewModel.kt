package dev.develsinthedetails.eatpoopyoucat.feature.previousgames

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.core.utilities.ID


class PreviousGameViewModel(
    state: SavedStateHandle,
    repository: AppRepository,
) : ViewModel() {
    private val gameId: String = checkNotNull(state.get<String>(ID))
    val gameWithEntries = repository.getGameWithEntries(gameId).asLiveData()
}