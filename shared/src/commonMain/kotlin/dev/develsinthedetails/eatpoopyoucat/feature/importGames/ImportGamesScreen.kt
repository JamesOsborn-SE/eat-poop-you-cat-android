package dev.develsinthedetails.eatpoopyoucat.feature.importGames

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.SpinnerScreen
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.core.utilities.Gzip
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import eatpoopyoucat.shared.generated.resources.Res
import eatpoopyoucat.shared.generated.resources.ack
import eatpoopyoucat.shared.generated.resources.imported
import eatpoopyoucat.shared.generated.resources.imported_entries
import eatpoopyoucat.shared.generated.resources.imported_games
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ImportGamesScreen(
    viewModel: ImportGamesViewModel = koinViewModel(),
    finish: () -> Unit
) {
    val addedGames by viewModel.numberOfGamesAdded.collectAsState(initial = 0)
    val addedEntries by viewModel.numberOfEntriesAdded.collectAsState(initial = 0)
    val finished by viewModel.isFinished.collectAsState(initial = false)

    var showAlert by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = listOf("gz", "zip"))
    ) { platformFile ->
        if (platformFile != null) {
            coroutineScope.launch {
                try {
                    val bytes = platformFile.readBytes()
                    val gamesString = Gzip.decompressToString(bytes)
                    val games = Json.decodeFromString<List<GameWithEntries>>(gamesString)

                    viewModel.addGames(games) {
                        // todo Callback when done?
                    }
                    showAlert = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    finish()
                }
            }
        } else {
            finish()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch()
    }

    if (!finished) {
        SpinnerScreen()
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
    }

    if (showAlert && finished) {
        ImportedAlert(
            games = addedGames,
            entries = addedEntries,
            onDismissRequest = { finish() }
        )
    }
}

@Preview
@Composable
fun ImportedAlertPreview() {
    val addedGames = 1
    val addedEntries = 69
    AppTheme {
        ImportedAlert(
            games = addedGames,
            entries = addedEntries,
            onDismissRequest = {})
    }
}

@Composable
private fun ImportedAlert(
    games: Int,
    entries: Int,
    onDismissRequest: () -> Unit?
) {
    val gamesText = pluralStringResource(Res.plurals.imported_games, quantity = games, games)
    val entriesText =
        pluralStringResource(Res.plurals.imported_entries, quantity = entries, entries)
    AlertDialog(
        title = { Text(text = stringResource(Res.string.imported)) },
        text = {
            Column {
                Text(text = gamesText)
                Text(text = entriesText)
            }
        },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(stringResource(Res.string.ack))
            }
        })
}
