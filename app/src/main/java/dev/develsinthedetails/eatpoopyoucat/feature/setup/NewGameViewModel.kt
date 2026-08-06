package dev.develsinthedetails.eatpoopyoucat.feature.setup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import dev.develsinthedetails.eatpoopyoucat.app.NewGame
import dev.develsinthedetails.eatpoopyoucat.app.UuidNavType
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import kotlin.reflect.typeOf
import kotlin.uuid.Uuid

class NewGameViewModel(
    state: SavedStateHandle,
    private val repository: AppRepository,
    ): ViewModel() {
    private val typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
    private val route = state.toRoute<NewGame>(typeMap)
    private val previousEntryId: Uuid = checkNotNull(route.id)
    var previousEntry: Entry? by mutableStateOf(null)
        private set
}