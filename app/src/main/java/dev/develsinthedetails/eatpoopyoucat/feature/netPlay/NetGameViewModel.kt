package dev.develsinthedetails.eatpoopyoucat.feature.netPlay

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.app.LanGame
import dev.develsinthedetails.eatpoopyoucat.app.UuidNavType
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf
import kotlin.uuid.Uuid

class NetGameViewModel (
    state: SavedStateHandle,
    private val repository: AppRepository,
) : ViewModel() {
    private val typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
    private val route = state.toRoute<LanGame>(typeMap)
    private val previousEntryId: Uuid = checkNotNull(route.id)
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
            isLoading = false
        }
    }

    fun updateNickname(it: String) {
        nickname = it
        isError = false
    }

    fun isValidNickname(context: Context): Boolean {
        val poolOfNickNames = context.resources.getStringArray(R.array.nicknames)
            .filterNot {
                previousNicknames.contains(it)
            }.toMutableList()
        val madeUpNickName = if(poolOfNickNames.isNotEmpty()) poolOfNickNames.random() else context.getString(R.string.oof)

        isError = (nickname.isBlank() || previousNicknames.contains(nickname))
            .also {
                if (it)
                    nickname = madeUpNickName
            }
        return !isError
    }
}