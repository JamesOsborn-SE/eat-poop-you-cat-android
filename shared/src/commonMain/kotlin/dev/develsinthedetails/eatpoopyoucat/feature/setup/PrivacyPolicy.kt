package dev.develsinthedetails.eatpoopyoucat.feature.setup

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.core.utilities.ReadMetadata
import eatpoopyoucat.shared.generated.resources.Res
import eatpoopyoucat.shared.generated.resources.privacy_policy
import org.jetbrains.compose.resources.stringResource

@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit
) {
    val metadataReader = remember { ReadMetadata() }
    val privacyText by produceState(initialValue = "Loading...") {
        value = metadataReader.getPrivacyPolicy()
    }
    Scaffolds.Backable(
        title = stringResource(
            Res.string.privacy_policy
        ), onBack = onBack
    ) {
        Surface(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .verticalScroll(ScrollState(0)),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .padding(8.dp),
                    text = privacyText
                )
            }
        }
    }
}

@Preview
@Composable
fun PrivacyPolicyScreenPreview() {
    AppTheme {
        PrivacyPolicyScreen {}
    }
}