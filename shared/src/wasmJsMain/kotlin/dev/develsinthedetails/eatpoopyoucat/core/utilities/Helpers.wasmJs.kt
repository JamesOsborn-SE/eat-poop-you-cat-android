package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.ktor.http.Url
import kotlinx.browser.window
import kotlin.uuid.Uuid

@OptIn(ExperimentalWasmJsInterop::class)
private fun hasNotificationApi(): Boolean = js("typeof Notification !== 'undefined'")

@OptIn(ExperimentalWasmJsInterop::class)
private fun getNotificationPermission(): String = js("Notification.permission")

@OptIn(ExperimentalWasmJsInterop::class)
private fun requestNotificationPermission(callback: (String) -> Unit) {
    js("Notification.requestPermission().then(p => callback(p))")
}

@Composable
actual fun rememberNotificationPermissionState(): NotificationPermissionState {

    var permissionState by remember {
        mutableStateOf(
            if (hasNotificationApi()) getNotificationPermission() else "denied"
        )
    }

    return remember(permissionState) {
        object : NotificationPermissionState {
            override val hasPermission: Boolean = permissionState == "granted"

            override fun requestPermission() {
                if (hasNotificationApi()) {
                    requestNotificationPermission { newStatus ->
                        permissionState = newStatus
                    }
                }
            }
        }
    }
}

actual fun getGameIdFromUrl(): Uuid? {
    val search = window.location.search

    if (search.isBlank()) return null
    val gameIdString = search.removePrefix("?")
        .split("&")
        .map { it.split("=") }
        .firstOrNull { it.size == 2 && it[0] == "game" }
        ?.get(1)

    if (gameIdString != null) {
        window.history.replaceState(
            data = null,
            title = "",
            url = window.location.pathname
        )
    }

    return try {
        gameIdString?.let { Uuid.parse(it) }
    } catch (e: Exception) {
        println("Error parsing game id: $gameIdString")
        println("Error parsing game exception: $e")
        null
    }
}

fun getServerBaseUrl(): String {
    return window.location.origin
}

actual fun getServerUrl(): Url {
    return Url(getServerBaseUrl())
}