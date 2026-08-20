package dev.develsinthedetails.eatpoopyoucat.feature.netPlay

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.core.utilities.shareDecode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.shareDecodeUuid

class NetGameActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri: Uri? = intent?.data
        if (uri != null) {
            try {
                val gameIdStr = uri.getQueryParameter("game")
                val playerAddress = uri.getQueryParameter("server")

                if (gameIdStr != null && playerAddress != null) {
                    val gameId = gameIdStr.shareDecodeUuid()
                    val address = playerAddress.shareDecode()
                    setContent {
                        AppTheme {
                            NetGameScreen(gameId=gameId, address = address)
                        }
                    }
                }
            } catch (e: Exception) {
                // TODO: Handle invalid UUID or missing parameters (e.g., show error or finish())
                finish()
            }
        }


    }
}