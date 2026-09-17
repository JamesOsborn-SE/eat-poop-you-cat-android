@file:Suppress("KotlinConstantConditions")

package dev.develsinthedetails.eatpoopyoucat.feature.setup

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.config.DEBUG
import dev.develsinthedetails.eatpoopyoucat.config.GIT_HASH
import dev.develsinthedetails.eatpoopyoucat.config.VERSION_NAME
import dev.develsinthedetails.eatpoopyoucat.config.VERSION_CODE
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.core.utilities.ReadMetadata
import eatpoopyoucat.shared.generated.resources.Res
import eatpoopyoucat.shared.generated.resources.about
import eatpoopyoucat.shared.generated.resources.debugging_on_label
import eatpoopyoucat.shared.generated.resources.git_hash_label
import eatpoopyoucat.shared.generated.resources.issues
import eatpoopyoucat.shared.generated.resources.player_id
import eatpoopyoucat.shared.generated.resources.translations_welcome
import eatpoopyoucat.shared.generated.resources.version_label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import kotlin.uuid.Uuid

@Composable
fun CreditsScreen(playerId: Uuid, onBack: () -> Unit){
    val uriHandler = LocalUriHandler.current
    val metadataReader = remember { ReadMetadata() }
    val fullDescription by produceState(initialValue = "Loading...") {
        value = metadataReader.getFullDescription()
    }

    Scaffolds.Backable(
        title = stringResource(
            Res.string.about
        ), onBack = onBack
    ) {
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
                        text = fullDescription
                    )
                }

                Row {
                    TextButton(onClick = {
                        uriHandler.openUri("https://hosted.weblate.org/engage/eat-poop-you-cat-android/")
                    }) {
                        Text(
                            text =
                                stringResource(Res.string.translations_welcome)
                        )
                    }
                    Row {
                        TextButton(onClick = {
                            uriHandler.openUri("https://github.com/JamesOsborn-SE/eat-poop-you-cat-android/issues")
                        }) {
                            Text(
                                text =
                                    stringResource(Res.string.issues)
                            )
                        }
                    }
                }
                Text(text = stringResource(Res.string.version_label, "$VERSION_NAME ($VERSION_CODE)"))
                Text(
                    text = stringResource(
                        Res.string.git_hash_label,
                        GIT_HASH
                    )
                )
                if (DEBUG) {
                    Text(text = stringResource(Res.string.debugging_on_label, DEBUG))
                    Text(
                        text = stringResource(
                            Res.string.player_id,
                            playerId
                        )
                    )
                }
            }
        }
    }
}
@Composable
fun CreditsScreen(appSettings: AppSettings = koinInject(), onBack: () -> Unit) {
CreditsScreen(appSettings.playerId, onBack)
}

@Preview
@Composable
fun CreditsScreenPreview() {
    AppTheme {
        CreditsScreen(Uuid.NIL) {}
    }
}