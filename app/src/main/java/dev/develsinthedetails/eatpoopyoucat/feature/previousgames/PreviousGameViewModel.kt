package dev.develsinthedetails.eatpoopyoucat.feature.previousgames

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.navigation.toRoute
import dev.develsinthedetails.eatpoopyoucat.app.PreviousGame
import dev.develsinthedetails.eatpoopyoucat.app.UuidNavType
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import kotlin.reflect.typeOf
import kotlin.uuid.Uuid


class PreviousGameViewModel(
    state: SavedStateHandle,
    repository: AppRepository,
) : ViewModel() {
    private val typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
    private val route = state.toRoute<PreviousGame>(typeMap)
    private val gameId: Uuid = checkNotNull(route.gameId)
    val gameWithEntries = repository.getGameWithEntries(gameId).asLiveData()
}