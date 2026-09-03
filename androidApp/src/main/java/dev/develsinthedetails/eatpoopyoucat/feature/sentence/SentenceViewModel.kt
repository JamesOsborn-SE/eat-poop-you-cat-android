package dev.develsinthedetails.eatpoopyoucat.feature.sentence

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.app.Sentence
import dev.develsinthedetails.eatpoopyoucat.app.UuidNavType
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.generateNickname
import dev.develsinthedetails.eatpoopyoucat.core.utilities.validateNickname
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf
import kotlin.uuid.Uuid

data class SentenceUiState(
    val gameId: Uuid,
    val previousEntry: Entry? = null,
    val isError: Boolean = false,
    val isLoading: Boolean = true,
    val sentence: String = String(),
    val gameMode: GameMode,

    val nickname: String? = null,
    val nicknameError: Int? = null,
    val nicknameIsSatisfied: Boolean = false,
    val previousNicknames: List<String> = listOf(),

    )

class SentenceViewModel(
    state: SavedStateHandle,
    private val repository: AppRepository,
    private val appSettings: AppSettings,
) : ViewModel() {
    private val typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
    private val route = state.toRoute<Sentence>(typeMap)
    private val gameMode = checkNotNull(route.gameMode)
    private val gameId = checkNotNull(route.gameId)
    private val _uiState = MutableStateFlow(SentenceUiState(gameId = gameId, gameMode = gameMode))
    val uiState: StateFlow<SentenceUiState> = _uiState.asStateFlow()
    val entryId = Uuid.random()

    init {
        viewModelScope.launch {
            val doNotUseNicknames =
                !appSettings.useNicknamesFlow.first() && gameMode == GameMode.LOCAL
            val previousEntry = repository.getLastEntry(gameId)
            _uiState.update {
                it.copy(
                    previousEntry = previousEntry,
                    isLoading = false,
                    nicknameIsSatisfied = doNotUseNicknames
                )
            }
        }
    }

    fun updateSentence(sentence: String) {
        _uiState.update { it.copy(sentence = sentence) }
    }

    fun saveEntry(nextTo: (Uuid) -> Unit) {
        val state = _uiState.value
        if (state.sentence.isBlank()) {
            _uiState.update { it.copy(isError = true) }
            return
        }
        _uiState.update { it.copy(isLoading = true) }

        val entry = state.previousEntry
        val sequence = entry?.sequence ?: -1
        val playerId = appSettings.playerId
        val newEntry = Entry(
            id = entryId,
            gameId = gameId,
            localPlayerName = state.nickname,
            sentence = state.sentence,
            drawing = null,
            sequence = sequence.inc(),
            playerId = playerId,
            timePassed = 0,
        )

        viewModelScope.launch {
            repository.upsertEntry(newEntry)
            nextTo.invoke(entryId)
        }
        _uiState.update { it.copy(isLoading = false) }
    }

    fun deleteGame() {
        viewModelScope.launch {
            _uiState.value.previousEntry?.let { repository.deleteGame(it.gameId) }
        }
    }

    fun updateNickname(nickname: String?) {
        _uiState.update { state ->
            state.copy(
                nickname = nickname
            )
        }
    }

    fun isNicknameValid(hardcodedNicknames: List<String>, fallbackNickname: String) {
        viewModelScope.launch {
            val pun =
                repository.getPreviouslyUsedNicknames(_uiState.value.gameId)
            val isValid = validateNickname(_uiState.value.nickname, pun)
            if (!isValid) {
                val generatedNick = generateNickname(hardcodedNicknames, pun, fallbackNickname)
                _uiState.update {
                    it.copy(
                        nickname = generatedNick,
                        nicknameError = R.string.no_nickname_chosen_warning
                    )
                }
            } else {
                _uiState.update { it.copy(nicknameIsSatisfied = true) }
            }
        }
    }
}