package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.ui.graphics.ImageBitmap
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import java.util.Date

val stringTime: String = Date().saveDateFormat()
fun defaultFilename(): String {
    return "EPYC-${Date().saveDateFormat()}.png"
}

val DEFAULT_DATA_FILENAME = "EPYC-$stringTime.json"

interface FileSaver {
    fun saveGames(
        games: List<GameWithEntries>,
        filename: String = "data.json" // Note: default params go in the interface
    ): String
}

expect suspend fun readBytesFromUriString(uriString: String): ByteArray?

expect fun shareImageUri(uri: Any?)

expect fun saveBitmap(bitmap: ImageBitmap, filename: String? = null): Any?