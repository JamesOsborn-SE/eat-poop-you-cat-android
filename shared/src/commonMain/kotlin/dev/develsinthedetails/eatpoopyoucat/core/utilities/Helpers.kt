package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable
import io.ktor.http.Url

fun validateNickname(
    currentNickname: String?,
    takenNicknames: List<String>,
): Boolean = !(currentNickname.isNullOrBlank() || takenNicknames.contains(currentNickname))

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

expect fun getRealAddress(address: Url): Url