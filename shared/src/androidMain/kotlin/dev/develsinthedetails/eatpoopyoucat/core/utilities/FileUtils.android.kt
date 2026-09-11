package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.develsinthedetails.eatpoopyoucat.app.AppContextProvider
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitShareSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberShareFileLauncher as rememberFileKitShareLauncher


@Composable
internal actual fun rememberShareFileLauncher(
    onError: (FileKitDialogException) -> Unit,
): ShareFileLauncher {
    val context = AppContextProvider.context
    val launcher = rememberFileKitShareLauncher(
        onError = onError,
        shareSettings = FileKitShareSettings(authority = "${context.packageName}.fileprovider")
    )

    return remember(launcher) {
        object : ShareFileLauncher {
            override val isSupported: Boolean = true

            override fun launch(files: List<PlatformFile>) {
                launcher.launch(files)
            }
        }
    }
}