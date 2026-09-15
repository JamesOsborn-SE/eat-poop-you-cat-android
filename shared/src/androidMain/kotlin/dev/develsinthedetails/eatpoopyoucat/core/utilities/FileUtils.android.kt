package dev.develsinthedetails.eatpoopyoucat.core.utilities


// androidMain
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.develsinthedetails.eatpoopyoucat.app.AppContextProvider
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.dialogs.FileKitShareSettings
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.saveImageToGallery
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.launch
import io.github.vinceglb.filekit.dialogs.compose.rememberShareFileLauncher as rememberFileKitShareLauncher

@Composable
actual fun rememberShareFileLauncher(
    onError: (Exception) -> Unit,
): ShareFileLauncher {
    val context = AppContextProvider.context
    val scope = rememberCoroutineScope()

    val launcher = rememberFileKitShareLauncher(
        onError = { onError(Exception(it.message, it)) },
        shareSettings = FileKitShareSettings(authority = "${context.packageName}.fileprovider")
    )

    return remember(launcher, scope) {
        object : ShareFileLauncher {
            override val isSupported: Boolean = true

            override fun launch(bytes: ByteArray, fileName: String) {
                scope.launch {
                    try {
                        val file = PlatformFile(FileKit.cacheDir, fileName)
                        file.write(bytes)
                        launcher.launch(file)
                    } catch (e: Exception) {
                        onError(e)
                    }
                }
            }
        }
    }
}

actual suspend fun saveToGallery(bytes: ByteArray, fileName: String) {
    val file = PlatformFile(FileKit.cacheDir, fileName)
    if (!file.exists()) {
        file.write(bytes)
    }
    FileKit.saveImageToGallery(file)
}