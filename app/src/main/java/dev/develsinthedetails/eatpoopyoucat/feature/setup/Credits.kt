@file:Suppress("KotlinConstantConditions")

package dev.develsinthedetails.eatpoopyoucat.feature.setup

import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.develsinthedetails.eatpoopyoucat.BuildConfig
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.core.utilities.ReadMetadata
import org.koin.compose.koinInject
import kotlin.uuid.Uuid

@Composable
fun CreditsScreen(appSettings: AppSettings = koinInject(), onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val playerId by produceState<Uuid?>(initialValue = null) {
        value = appSettings.getPlayerId()
    }
    Scaffolds.Backable(title = stringResource(
        id = R.string.about
    ), onBack = onBack) {
        Surface(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .verticalScroll(ScrollState(0)),
            color = (MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    Text(
                        modifier = Modifier
                            .padding(8.dp),
                        text = ReadMetadata(LocalContext.current).getFullDescription()
                    )
                }

                Row {
                    TextButton(onClick = {
                        uriHandler.openUri("https://hosted.weblate.org/engage/eat-poop-you-cat-android/")
                    }) {
                        Text(
                            text =
                            stringResource(id = R.string.translations_welcome)
                        )
                    }
                    Row {
                        TextButton(onClick = {
                            uriHandler.openUri("https://github.com/JamesOsborn-SE/eat-poop-you-cat-android/issues")
                        }) {
                            Text(
                                text =
                                stringResource(id = R.string.issues)
                            )
                        }
                    }
                }
                Text(text = stringResource(R.string.version_label, BuildConfig.VERSION_NAME))
                Text(
                    text = stringResource(
                        R.string.git_hash_label,
                        stringResource(R.string.git_hash)
                    )
                )
                if (BuildConfig.DEBUG) {
                    Text(text = stringResource(R.string.debugging_on_label, BuildConfig.DEBUG))
                    Text(text = stringResource(R.string.player_id, playerId.toString()))
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(device = "spec:parent=Nexus 7 2013,orientation=landscape")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = "spec:parent=Nexus 7 2013,orientation=landscape"
)
@Composable
fun CreditsScreenPreview() {
    AppTheme {
        CreditsScreen{}
    }
}