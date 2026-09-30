package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable
import eatpoopyoucat.shared.generated.resources.Res
import eatpoopyoucat.shared.generated.resources.pass_to_the_next
import eatpoopyoucat.shared.generated.resources.turn_passed
import io.ktor.http.Url
import org.jetbrains.compose.resources.getString
import kotlin.uuid.Uuid

fun validateNickname(
    currentNickname: String?,
    takenNicknames: List<String>,
): Boolean = !(currentNickname.isNullOrBlank() || takenNicknames.contains(currentNickname))

suspend fun nextText(gameMode: GameMode): String =
    if (gameMode == GameMode.LAN) getString(Res.string.pass_to_the_next) else getString(
        Res.string.turn_passed
    )

fun generateNickname(
    hardcodedNames: List<String>,
    takenNicknames: List<String>,
    fallbackName: String
): String = hardcodedNames
    .filterNot { takenNicknames.contains(it) }
    .randomOrNull() ?: fallbackName

interface NotificationPermissionState {
    val hasPermission: Boolean
    fun requestPermission()
}

@Composable
expect fun rememberNotificationPermissionState(): NotificationPermissionState

expect fun getServerUrl(): Url

expect fun getGameIdFromUrl(): Uuid?
