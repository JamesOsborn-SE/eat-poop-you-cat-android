package dev.develsinthedetails.eatpoopyoucat.utilities

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dev.develsinthedetails.eatpoopyoucat.data.GameWithEntries
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// TODO: more better names like isodate?
val DEFAULT_FILENAME = "EPYC-${System.currentTimeMillis()}.png"
val DEFAULT_DATA_FILENAME = "EPYC-${System.currentTimeMillis()}.json"

fun saveBitmap(context: Context, bitmap: Bitmap, filename: String = DEFAULT_FILENAME): Uri? {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }

    val contentResolver = context.contentResolver

    val imageUri: Uri? = contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    )

    return imageUri.also {
        val fileOutputStream = imageUri?.let { contentResolver.openOutputStream(it) }
        fileOutputStream?.let { it1 -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, it1) }
        fileOutputStream?.close()
    }
}

fun shareImageUri(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.type = "image/png"
    context.startActivity(intent)
}

fun saveGames(
    context: Context,
    games: List<GameWithEntries>,
    filename: String = DEFAULT_DATA_FILENAME
): String {
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

    fileUri.also {
        val fileOutputStream = fileUri?.let { contentResolver.openOutputStream(it) }
        fileOutputStream?.let { file ->
            Gzip.compress(Json.encodeToString(games), file)
        }
        fileOutputStream?.close()
    }
    return "$saveDirectory/$filename.gz"
}
