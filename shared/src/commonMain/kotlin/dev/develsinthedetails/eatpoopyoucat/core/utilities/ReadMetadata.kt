package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.ui.text.intl.Locale
import eatpoopyoucat.shared.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

class ReadMetadata() {
    suspend fun getFullDescription(): String {
        val fileContents = getMetadataFile("full_description.txt")
        val lines = fileContents.split("\n")
        if (lines.isEmpty()) return ""
        val link = lines.last()
        return fileContents.dropLast(link.count()).trim()
    }

    suspend fun getPrivacyPolicy(): String {
        return getMetadataFile("privacy_policy.txt").trim()
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun getMetadataFile(filename: String): String {
        val localePath = "files/metadata/android/${Locale.current.toLanguageTag()}/$filename"
        val fallbackPath = "files/metadata/android/en-US/$filename"

        return try {
            // Attempt to read localized file
            Res.readBytes(localePath).decodeToString().also {
                println("ReadMetadata: Loaded $localePath")
            }
        } catch (e: Exception) {
            // File doesn't exist, fallback to en-US
            println("ReadMetadata: $localePath not found, trying fallback.")
            try {
                Res.readBytes(fallbackPath).decodeToString()
            } catch (fallbackError: Exception) {
                println("ReadMetadata: Fallback failed - $fallbackError")
                ""
            }
        }
    }
}