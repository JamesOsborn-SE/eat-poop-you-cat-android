package dev.develsinthedetails.eatpoopyoucat.feature.sentence

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.app.Sentence
import dev.develsinthedetails.eatpoopyoucat.app.UuidNavType
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf
import kotlin.uuid.Uuid

class SentenceViewModel(
    state: SavedStateHandle,
    private val repository: AppRepository,
    private val appSettings: AppSettings,
) : ViewModel() {

    var isError: Boolean by mutableStateOf(false)
        private set
    var isLoading: Boolean by mutableStateOf(false)
        private set

    private val typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
    private val route = state.toRoute<Sentence>(typeMap)
    private val previousEntryId: Uuid = checkNotNull(route.previousEntryId)
    val previousEntry: LiveData<Entry?> = repository.getEntry(previousEntryId).asLiveData()

    private val nickname = route.nickname

    val entryId = Uuid.random()

    var sentence: String by mutableStateOf("")
        private set

    fun updateSentence(it: String) {
        sentence = it
    }

    fun sentenceIsNotBlank(): Boolean {
        isError = sentence.isBlank()
        return !isError
    }

    fun saveEntry(nextTo: (Uuid) -> Unit) {
        isLoading = true
        val entry = previousEntry.value!!
        val isNewGame = entry.sequence == 0
        if (isNewGame) {
            val newEntry = entry.copy(
                localPlayerName = nickname,
                sentence = sentence,
                drawing = null
            )
            viewModelScope.launch {
                repository.updateEntry(newEntry)
                nextTo.invoke(entry.id)
                isLoading = false
            }
        } else {
            val playerId = appSettings.playerId
            val newEntry = entry.copy(
                id = entryId,
                localPlayerName = nickname,
                sentence = sentence,
                drawing = null,
                sequence = entry.sequence.inc(),
                playerId = playerId
            )

            viewModelScope.launch {
                repository.createEntry(newEntry)
                nextTo.invoke(entryId)
                isLoading = false
            }
        }
    }

    fun deleteGame() {
        viewModelScope.launch {
            previousEntry.value?.let { repository.deleteGame(it.gameId) }
        }
    }
}