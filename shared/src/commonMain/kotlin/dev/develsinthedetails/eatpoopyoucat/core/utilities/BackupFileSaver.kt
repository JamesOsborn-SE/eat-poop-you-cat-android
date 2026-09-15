package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.runtime.Composable

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class BackupFileSaver {
    /**
     * @param bytes The gzipped json data
     * @param suggestedName e.g., "epyc_backup"
     * @param extension e.g., "gz"
     */
    fun saveBackup(bytes: ByteArray, suggestedName: String, extension: String)
}

@Composable
expect fun rememberBackupFileSaver(
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
): BackupFileSaver