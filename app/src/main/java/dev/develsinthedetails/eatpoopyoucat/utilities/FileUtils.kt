package dev.develsinthedetails.eatpoopyoucat.utilities

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore

@JvmField
val DEFAULT_FILENAME = "EPYC-${System.currentTimeMillis()}.png"

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