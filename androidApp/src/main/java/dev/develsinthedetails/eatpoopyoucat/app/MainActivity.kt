package dev.develsinthedetails.eatpoopyoucat.app

import android.content.Intent
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dev.develsinthedetails.eatpoopyoucat.R.string.deeplink_host
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkAndPromptForAppLinks(getString(deeplink_host))
        }
    }
    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkAndPromptForAppLinks(domain: String) {
        val manager = getSystemService(DomainVerificationManager::class.java)
        val userState = manager?.getDomainVerificationUserState(packageName)

        val domainState = userState?.hostToStateMap?.get(domain)

        if (domainState != DomainVerificationUserState.DOMAIN_STATE_VERIFIED &&
            domainState != DomainVerificationUserState.DOMAIN_STATE_SELECTED) {

            val intent = Intent(
                Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                "package:$packageName".toUri()
            )
            startActivity(intent)
        }
    }
    private val appSettings: AppSettings by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition {
            !appSettings.isReady
        }
        val importedFileUri = intent?.data?.toString()
        setContent {
            AppTheme {
                NavGraph(externalImportUri = importedFileUri)
            }
        }
    }
}

