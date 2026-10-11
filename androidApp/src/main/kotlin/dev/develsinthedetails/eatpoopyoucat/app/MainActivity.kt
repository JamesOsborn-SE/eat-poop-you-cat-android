package dev.develsinthedetails.eatpoopyoucat.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dev.develsinthedetails.eatpoopyoucat.config.DEEPLINK_PLAY
import dev.develsinthedetails.eatpoopyoucat.core.utilities.shareDecodeUrl
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import org.koin.android.ext.android.inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MainActivity : ComponentActivity() {
    private val appSettings: AppSettings by inject()

    @OptIn(ExperimentalUuidApi::class)
    private var netGameParams = mutableStateOf<Pair<Uuid, String>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { !appSettings.isReadyFlow.value }
        AppContextProvider.context = applicationContext
        handleIntent(intent)
        FileKit.init(this)
        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun handleIntent(intent: Intent?) {
        val uri: Uri = intent?.data ?: return

        if (uri.path?.contains(DEEPLINK_PLAY) == true) {
            try {
                netGameParams.value = uri.toString().shareDecodeUrl()
            } catch (_: Exception) {
                // Ignore invalid deep links
            }
        }
    }
}