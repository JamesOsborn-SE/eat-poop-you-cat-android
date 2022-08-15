package dev.develsinthedetails.eatpoopyoucat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val state: SavedStateHandle
) : ViewModel() {
    var playerName: String?
        get() = state.get(PLAYER_KEY)
        set(value) = state.set(PLAYER_KEY, value)

    companion object {
        private const val PLAYER_KEY = "player"
    }
}