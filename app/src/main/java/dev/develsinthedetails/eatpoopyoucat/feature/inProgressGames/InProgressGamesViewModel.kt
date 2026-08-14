package dev.develsinthedetails.eatpoopyoucat.feature.inProgressGames

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithRosters
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class InProgressGamesViewModel(
    appSettings: AppSettings,
    repository: AppRepository,
) : ViewModel() {
    val playerId = appSettings.playerId
    val games: StateFlow<List<GameWithRosters>?> = repository.getInProgressGamesWithRosters()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )
}
