package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.saveImageToGallery
import io.github.vinceglb.filekit.write

@Composable
actual fun rememberShareFileLauncher(onError: (Exception) -> Unit): ShareFileLauncher {
    return object : ShareFileLauncher {
        override val isSupported: Boolean
            get() = false

        override fun launch(bytes: ByteArray, fileName: String) {
            onError(Exception("Not Supported"))
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