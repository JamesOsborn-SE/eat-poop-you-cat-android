package dev.develsinthedetails.eatpoopyoucat.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.Entry
import dev.develsinthedetails.eatpoopyoucat.utilities.ID
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NicknameViewModel @Inject constructor(
    state: SavedStateHandle,
    private val repository: AppRepository,
) : ViewModel() {
    private val previousEntryId: String = checkNotNull(state.get<String>(ID))
    var previousNicknames: List<String> by mutableStateOf(listOf())
    var previousEntry: Entry? by mutableStateOf(null)
        private set

    var nickname: String by mutableStateOf("")
        private set

    var isLoading: Boolean by mutableStateOf(false)
        private set

    var isError: Boolean by mutableStateOf(false)
        private set

    init {
        isLoading = true
        viewModelScope.launch {
            previousEntry = repository.getEntryAsync(previousEntryId)
            previousNicknames = repository.getGameWithEntriesAsync(previousEntry!!.gameId)
                .entries
                .filter { !it.localPlayerName.isNullOrBlank() }
                .map { it.localPlayerName!! }
        }
        isLoading = false
    }

    fun updateNickname(it: String) {
        nickname = it
        isError = false
    }

    fun isValidNickname(context: Context): Boolean {
        isError = (nickname.isBlank() || previousNicknames.contains(nickname))
            .also {
                if (it)
                    nickname = if (previousNicknames.isNotEmpty())
                        context.getString(R.string.not_nickname, previousNicknames.random())
                    else
                        context.getString(R.string.not_nickname, context.getString(R.string.james))
            }
        return !isError
    }
}