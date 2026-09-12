package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.inProgressGames

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.app.InProgressGameDetails
import dev.develsinthedetails.eatpoopyoucat.app.appTypeMap
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlin.uuid.Uuid

class InProgressGameDetailsViewModel(
    appSettings: AppSettings,
    repository: AppRepository,
    state: SavedStateHandle,
) : ViewModel() {
    val playerId = appSettings.playerId
    private val typeMap = appTypeMap
    private val route = state.toRoute<InProgressGameDetails>(typeMap)
    private val gameId: Uuid = checkNotNull(route.gameId)
    val players: Flow<List<Roster>?> = repository.getRostersByGameFlow(gameId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    val game: Flow<Game?> = repository.getGameFlow(gameId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

}