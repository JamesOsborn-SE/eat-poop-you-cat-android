package dev.develsinthedetails.eatpoopyoucat.core.utilities

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import dev.develsinthedetails.eatpoopyoucat.app.AppContextProvider

@Composable
actual fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}

actual fun shareLink(link: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, link)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share via").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    AppContextProvider.context.startActivity(shareIntent)
}