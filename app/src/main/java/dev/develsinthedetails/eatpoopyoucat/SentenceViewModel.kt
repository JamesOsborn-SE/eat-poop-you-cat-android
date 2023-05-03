package dev.develsinthedetails.eatpoopyoucat

import android.content.SharedPreferences
import android.view.View.VISIBLE
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.Drawing
import dev.develsinthedetails.eatpoopyoucat.data.Entry
import dev.develsinthedetails.eatpoopyoucat.data.Game
import dev.develsinthedetails.eatpoopyoucat.utilities.CommonStringNames
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SentenceViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val repository: AppRepository,
    private var shared: SharedPreferences
) : ViewModel() {
    init {
        viewModelScope.launch {
            saveEntry()
        }
    }
    var entry: Entry?
        get() = state[ENTRY]
        set(value) = state.set(ENTRY, value)
    var drawing: Drawing?
        get() = state[DRAWING_KEY]
        set(value) = state.set(DRAWING_KEY, value)
    var sentenceToDrawHint: String?
        get() = state[SENTENCE_HINT_KEY]
        set(value) = state.set(SENTENCE_HINT_KEY, value)
    var createdByVisibility : Int?
        get() = state[CREATE_VIS_KEY]
        set(value) = state.set(CREATE_VIS_KEY, value)
    var drawViewVisibility = VISIBLE
    var playerName: String?
        get() = state[PLAYER_KEY]
        set(value) = state.set(PLAYER_KEY, value)
    var createdBy: LiveData<String>
        get() = state.getLiveData(CREATED_BY_KEY)
        set(value) = state.set(CREATED_BY_KEY, value)

    companion object {
        private const val CREATED_BY_KEY = "createdBy"
        private const val PLAYER_KEY = "player"
        private const val DRAWING_KEY = "drawing"
        private const val SENTENCE_HINT_KEY = "sentenceToDrawHint"
        private const val CREATE_VIS_KEY = "createdByVisibility"
        private const val ENTRY = "entry"
    }

    private suspend fun saveEntry() {
        var game: Game;
        if (entry == null) {
            game = Game(id = UUID.randomUUID(), null, null)
            repository.createGame(game)
            entry = Entry(
                UUID.randomUUID(),
                UUID.fromString(
                    shared.getString(
                        CommonStringNames.playerId,
                        CommonStringNames.Empty,
                    ),
                ),
                0,
                game.id,
                0,
            );
        }
        else{
            game = repository.getGame(entry!!.gameId)
        }

        repository.createEntry(entry!!)
    }
}
