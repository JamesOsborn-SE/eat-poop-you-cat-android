package dev.develsinthedetails.eatpoopyoucat.core.utilities

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import kotlinx.serialization.json.Json
import okio.sink

class AndroidFileSaver(private val context: Context) : FileSaver {

    override fun saveGames(games: List<GameWithEntries>, filename: String): String {
        val saveDirectory = Environment.DIRECTORY_DOWNLOADS
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/gzip")
            put(MediaStore.MediaColumns.RELATIVE_PATH, saveDirectory)
        }

        val contentResolver = context.contentResolver
        val url: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri(saveDirectory + filename)
        }

        val fileUri: Uri? = contentResolver.insert(url, contentValues)
        fileUri?.let { uri ->
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val jsonData = Json.encodeToString(games)
                Gzip.compress(jsonData, outputStream.sink())
            }
        }

        return "$saveDirectory/$filename.gz"
    }
}