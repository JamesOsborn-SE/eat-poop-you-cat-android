package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException

@Composable
internal actual fun rememberShareFileLauncher(onError: (FileKitDialogException) -> Unit): ShareFileLauncher {
    return object : ShareFileLauncher {
        override val isSupported: Boolean
            get() = false

        override fun launch(files: List<PlatformFile>) {
            onError(FileKitDialogException("Not Supported"))
        }
    }
}