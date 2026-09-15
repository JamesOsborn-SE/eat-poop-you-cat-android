package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.launch

actual class BackupFileSaver(
    private val launchBlock: (ByteArray, String, String) -> Unit
) {
    actual fun saveBackup(bytes: ByteArray, suggestedName: String, extension: String) {
        launchBlock(bytes, suggestedName, extension)
    }
}

@Composable
actual fun rememberBackupFileSaver(
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
): BackupFileSaver {
    val scope = rememberCoroutineScope()
    val pendingBytes = remember { mutableStateOf<ByteArray?>(null) }

    val launcher = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings.createDefault(),
        onError = { failure ->
            onError(Exception(failure.message, failure))
            pendingBytes.value = null
        },
        onResult = { file: PlatformFile? ->
            if (file != null) {
                val bytesToWrite = pendingBytes.value
                if (bytesToWrite != null) {
                    scope.launch {
                        try {
                            file.write(bytesToWrite)
                            onSuccess()
                        } catch (e: Exception) {
                            onError(e)
                        } finally {
                            pendingBytes.value = null
                        }
                    }
                }
            } else {
                pendingBytes.value = null
            }
        }
    )

    return remember(launcher) {
        BackupFileSaver { bytes, name, ext ->
            pendingBytes.value = bytes
            launcher.launch(
                suggestedName = name,
                defaultExtension = ext
            )
        }
    }
}