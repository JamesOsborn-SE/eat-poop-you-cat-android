package dev.develsinthedetails.eatpoopyoucat.app

import android.content.Intent
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dev.develsinthedetails.eatpoopyoucat.config.DEEPLINK_HOST
import dev.develsinthedetails.eatpoopyoucat.config.DEEPLINK_PLAY
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.core.utilities.shareDecode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.shareDecodeUuid
import org.koin.android.ext.android.inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MainActivity : ComponentActivity() {
    private val appSettings: AppSettings by inject()
    private var externalImportUri = mutableStateOf<String?>(null)

    @OptIn(ExperimentalUuidApi::class)
    private var netGameParams = mutableStateOf<Pair<Uuid, String>?>(null)

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkAndPromptForAppLinks()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkAndPromptForAppLinks() {
        val manager = getSystemService(DomainVerificationManager::class.java)
        val userState = manager?.getDomainVerificationUserState(packageName)
        val domainState = userState?.hostToStateMap?.get(DEEPLINK_HOST)

        if (domainState != DomainVerificationUserState.DOMAIN_STATE_VERIFIED &&
            domainState != DomainVerificationUserState.DOMAIN_STATE_SELECTED
        ) {
            val intent = Intent(
                Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                "package:$packageName".toUri()
            )
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { !appSettings.isReady }
        AppContextProvider.context = applicationContext
        handleIntent(intent)

        setContent {
            AppTheme {
                NavGraph(
                    externalImportUri = externalImportUri.value,
                    netGameParams = netGameParams.value,
                    onExternalUriConsumed = { externalImportUri.value = null },
                    onNetGameParamsConsumed = { netGameParams.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun handleIntent(intent: Intent?) {
        val uri: Uri = intent?.data ?: return

        if (uri.path?.endsWith("json.gz") == true || intent.type == "application/gzip" || uri.scheme == "content") {
            externalImportUri.value = uri.toString()
        } else if (uri.path?.contains(DEEPLINK_PLAY) == true) {
            try {
                val gameIdStr = uri.getQueryParameter("game")
                val playerAddress = uri.getQueryParameter("server")

                if (gameIdStr != null && playerAddress != null) {
                    val gameId = gameIdStr.shareDecodeUuid()
                    val address = playerAddress.shareDecode()
                    netGameParams.value = Pair(gameId, address)
                }
            } catch (_: Exception) {
                // Ignore invalid deep links
            }
        }
    }
}