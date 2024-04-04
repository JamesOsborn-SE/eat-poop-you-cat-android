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

@JvmField
val DEFAULT_FILENAME = "EPYC-${System.currentTimeMillis()}.png"
val DEFAULT_DATA_FILENAME = "EPYC-${System.currentTimeMillis()}.json"

fun saveBitmap(context: Context, bitmap: Bitmap, filename: String = DEFAULT_FILENAME): Uri? {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
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
fun saveGames(context: Context, games: List<GameWithEntries>, filename: String = DEFAULT_DATA_FILENAME): Uri? {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "application/gzip")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
    }

    val contentResolver = context.contentResolver

    val fileUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        )
    } else {
        contentResolver.insert(
            MediaStore.Files.getContentUri(Environment.DIRECTORY_DOWNLOADS+filename),
            contentValues
        )
    }

    return fileUri.also {
        val fileOutputStream = fileUri?.let { contentResolver.openOutputStream(it) }
        fileOutputStream?.let { file ->
            Gzip.compress(Json.encodeToString(games), file)
        }
        fileOutputStream?.close()
    }
}

fun shareGamesUri(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.type = "application/gzip"
    context.startActivity(intent)
}