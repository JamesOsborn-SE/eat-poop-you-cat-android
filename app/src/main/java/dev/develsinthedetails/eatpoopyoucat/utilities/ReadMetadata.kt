package dev.develsinthedetails.eatpoopyoucat.utilities

import android.content.Context
import android.util.Log
import androidx.compose.ui.text.intl.Locale
import java.io.InputStream

class ReadMetadata(val context: Context) {
    fun getFullDescription(): String {
        val fileContents = getMetadataFile(context, "full_description.txt")
        val lines = fileContents.split("\n")
        val link=lines.last()
        return fileContents.dropLast(link.count()).trim()
    }

    fun getPrivacyPolicy(): String {
        return getMetadataFile(context, "privacy_policy.txt").trim()
    }

    private fun getMetadataFile(context: Context, filename: String): String {
        val path = "metadata/android/${Locale.current.toLanguageTag()}/"
        val file = if (context.resources.assets.list(path)?.contains(filename) == true) {
            path + filename
        } else { // en-US should always exist
            "metadata/android/en-US/${filename}"
        }
        try {
            val inputStream: InputStream = context.assets.open(file)
            val fileContents = inputStream.bufferedReader().use { it.readText() }
            Log.d(TAG, fileContents)
            return fileContents
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
        return ""
    }

    companion object {
        private const val TAG = "ReadMetadata"
    }
}