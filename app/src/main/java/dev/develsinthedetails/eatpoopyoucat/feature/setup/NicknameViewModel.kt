package dev.develsinthedetails.eatpoopyoucat.feature.setup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.app.NewGame
import dev.develsinthedetails.eatpoopyoucat.app.UuidNavType
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf
import kotlin.uuid.Uuid

sealed interface EntryUiState {
    data object Loading : EntryUiState
    data class FatalError(val message: String) : EntryUiState
    data class Content(
        val previousEntry: Entry,
        val previousNicknames: List<String>,
        val nickname: String = "",
        val nicknameError: Int? = null,
    ) : EntryUiState
}

class NicknameViewModel(
    state: SavedStateHandle,
    private val repository: AppRepository,
) : ViewModel() {
    private val typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
    private val route = state.toRoute<NewGame>(typeMap)
    private val previousEntryId: Uuid = checkNotNull(route.id)
    private val _uiState = MutableStateFlow<EntryUiState>(EntryUiState.Loading)
    val state: StateFlow<EntryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val entry = repository.getEntryAsync(previousEntryId)
                val nicknames = repository.getGameWithEntriesAsync(entry.gameId)
                    .entries
                    .mapNotNull { it.localPlayerName?.takeIf { name -> name.isNotBlank() } }
                _uiState.value = EntryUiState.Content(
                    previousEntry = entry,
                    previousNicknames = nicknames
                )
            } catch (e: Exception) {
                _uiState.value = EntryUiState.FatalError(e.message ?: "Unknown error")
            }
        }
    }

    fun updateNickname(nickname: String) {
        when (_uiState.value) {
            is EntryUiState.Content -> {
                _uiState.update { (_uiState.value as EntryUiState.Content).copy(nickname=nickname) }
            }
            else -> return
        }
    }

        fun validateAndAutoAssignNickname(
            hardcodedNames: List<String>,
            fallbackName: String
        ): Boolean {
            val currentState = _uiState.value
            if (currentState !is EntryUiState.Content) return false

            val currentNickname = currentState.nickname
            val takenNicknames = currentState.previousNicknames

            val isInvalid = currentNickname.isBlank() || takenNicknames.contains(currentNickname)

            if (isInvalid) {
                val generatedName = hardcodedNames
                    .filterNot { takenNicknames.contains(it) }
                    .randomOrNull() ?: fallbackName

                _uiState.update  { (_uiState.value as EntryUiState.Content).copy(nickname=generatedName) }
                return false
            } else {
                _uiState.update  { (_uiState.value as EntryUiState.Content).copy(nicknameError = R.string.no_nickname_chosen_warning) }
                return true
            }
        }
    }
