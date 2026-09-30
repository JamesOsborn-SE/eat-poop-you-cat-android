package dev.develsinthedetails.eatpoopyoucat.feature.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
)

class HomeViewModel(
    private val appSettings: AppSettings,
    private val appRepository: AppRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState(
            isLoading = true,
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    var useNicknames = appSettings.useNicknamesFlow

    init {
        viewModelScope.launch {
            println("HomeViewModel init")
            _uiState.update { it.copy(isLoading = true) }
            var player = appRepository.getPlayer(appSettings.playerId)
            if (player == null) {
                appRepository.createPlayer(Player(id = appSettings.playerId, ""))
            }
            println("HomeViewModel init complete")
            _uiState.update { it.copy(isLoading = false) }

        }
    }

    fun updateUseNicknames(enable: Boolean) {
        viewModelScope.launch {
            appSettings.setUseNicknames(!enable)
        }
    }
}