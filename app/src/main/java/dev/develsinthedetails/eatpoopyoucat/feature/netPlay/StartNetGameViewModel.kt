package dev.develsinthedetails.eatpoopyoucat.feature.netPlay

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.app.StartNetGame
import dev.develsinthedetails.eatpoopyoucat.app.UuidNavType
import dev.develsinthedetails.eatpoopyoucat.core.utilities.NetworkUtils
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlin.reflect.typeOf
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class StartNetGameViewModel(
    state: SavedStateHandle,
    repository: AppRepository,
    appSettings: AppSettings
) : ViewModel() {
    private val typeMap = mapOf(typeOf<Uuid>() to UuidNavType)
    private val route = state.toRoute<StartNetGame>(typeMap)
    val gameId: Uuid = checkNotNull(route.gameId)
    val gameMode = route.gameMode
    val address = NetworkUtils.getLocalIpAddress()
    val player: StateFlow<Player?> = repository.getPlayer(appSettings.playerId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    sealed class ServerAction {
        object Idle : ServerAction()
        object PromptWifiTurnOn : ServerAction()
        data class StartService(val ipAddress: String) : ServerAction()
    }

    private val _serverAction = MutableStateFlow<ServerAction>(ServerAction.Idle)
    val serverAction: StateFlow<ServerAction> = _serverAction.asStateFlow()

    fun onStartServerRequested(isWifiOn: Boolean, ipAddress: String?) {
        if (!isWifiOn || ipAddress == null) {
            _serverAction.value = ServerAction.PromptWifiTurnOn
        } else {
            _serverAction.value = ServerAction.StartService(ipAddress)
        }
    }

    fun resetAction() {
        _serverAction.value = ServerAction.Idle
    }

    fun validateNickname(nickname: String?){
        TODO("Not yet implemented")
    }
    fun createRoster() {
        TODO("Not yet implemented")
    }
}