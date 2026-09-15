package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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