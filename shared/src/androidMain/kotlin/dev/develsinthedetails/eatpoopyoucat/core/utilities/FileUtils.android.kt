package dev.develsinthedetails.eatpoopyoucat.core.utilities

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.net.toUri
import dev.develsinthedetails.eatpoopyoucat.app.AppContextProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

actual suspend fun readBytesFromUriString(uriString: String): ByteArray? {
    val context = AppContextProvider.context
    val uri = uriString.toUri()
    val inputStream = context.contentResolver.openInputStream(uri) ?: return null

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return withContext(Dispatchers.IO) {
            inputStream.readAllBytes()
        }
    }

    val bufLen = 4 * 0x400 // 4KB
    val buf = ByteArray(bufLen)
    var readLen: Int
    var exception: IOException? = null
    try {
        ByteArrayOutputStream().use { outputStream ->
            while (inputStream.read(buf, 0, bufLen)
                    .also { readLen = it } != -1
            ) outputStream.write(buf, 0, readLen)
            return outputStream.toByteArray()
        }
    } catch (e: IOException) {
        exception = e
        throw e
    } finally {
        if (exception == null) withContext(Dispatchers.IO) {
            inputStream.close()
        } else try {
            withContext(Dispatchers.IO) {
                inputStream.close()
            }
        } catch (e: IOException) {
            exception.addSuppressed(e)
        }
    }
}

actual fun shareImageUri(uri: Any?) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.putExtra(Intent.EXTRA_STREAM, uri as Uri?)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.type = "image/png"
    AppContextProvider.context.startActivity(intent)
}

actual fun saveBitmap(bitmap: ImageBitmap, filename: String?): Any? {
    var filename = filename
    if (filename == null) {
        filename = defaultFilename()
    }
    val context = AppContextProvider.context
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
        fileOutputStream?.let { it1 ->
            bitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it1)
        }
        fileOutputStream?.close()
    }
}