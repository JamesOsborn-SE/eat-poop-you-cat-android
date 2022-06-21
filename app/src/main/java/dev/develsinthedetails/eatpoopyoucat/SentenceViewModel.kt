package dev.develsinthedetails.eatpoopyoucat

import android.view.View.VISIBLE
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.develsinthedetails.eatpoopyoucat.data.Drawing
import javax.inject.Inject

@HiltViewModel
class SentenceViewModel @Inject constructor(
    private val state: SavedStateHandle
) : ViewModel() {
    var drawing: Drawing?
        get() = state.get(DRAWING_KEY)
        set(value) = state.set(DRAWING_KEY, value)
    var sentenceToDrawHint: String?
        get() = state.get(SENTENCE_HINT_KEY)
        set(value) = state.set(SENTENCE_HINT_KEY, value)
    var createdByVisibility : Int?
        get() = state.get(CREATE_VIS_KEY)
        set(value) = state.set(CREATE_VIS_KEY, value)
    var drawViewVisibility = VISIBLE
    var playerName: String?
        get() = state.get(PLAYER_KEY)
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
    }
}
