package dev.develsinthedetails.eatpoopyoucat.feature.previousGames

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.PlatformLazyVerticalScrollbar
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.core.utilities.Gzip
import dev.develsinthedetails.eatpoopyoucat.core.utilities.ImageExport
import dev.develsinthedetails.eatpoopyoucat.core.utilities.defaultDataFilename
import dev.develsinthedetails.eatpoopyoucat.core.utilities.localDateTimestamp
import dev.develsinthedetails.eatpoopyoucat.core.utilities.localTimestamp
import dev.develsinthedetails.eatpoopyoucat.core.utilities.rememberBackupFileSaver
import dev.develsinthedetails.eatpoopyoucat.core.utilities.rememberBitmapFromResource
import dev.develsinthedetails.eatpoopyoucat.core.utilities.rememberShareFileLauncher
import dev.develsinthedetails.eatpoopyoucat.core.utilities.saveToGallery
import dev.develsinthedetails.eatpoopyoucat.core.utilities.valueOrEmpty
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.EntryType
import dev.develsinthedetails.eatpoopyoucat.data.models.type
import dev.develsinthedetails.eatpoopyoucat.feature.draw.DrawBox
import eatpoopyoucat.shared.generated.resources.Res
import eatpoopyoucat.shared.generated.resources.app_name
import eatpoopyoucat.shared.generated.resources.continue_previous_game
import eatpoopyoucat.shared.generated.resources.ic_launcher_foreground
import eatpoopyoucat.shared.generated.resources.ic_replay_rounded
import eatpoopyoucat.shared.generated.resources.ic_share_filled
import eatpoopyoucat.shared.generated.resources.ic_vertical_align_top_rounded
import eatpoopyoucat.shared.generated.resources.is_available_on
import eatpoopyoucat.shared.generated.resources.no_games_to_save
import eatpoopyoucat.shared.generated.resources.previous_games
import eatpoopyoucat.shared.generated.resources.saving
import eatpoopyoucat.shared.generated.resources.scroll_to_top
import eatpoopyoucat.shared.generated.resources.share_this_game
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.dialogs.compose.util.encodeToByteArray
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid

@Composable
fun PreviousGameDetailsRoute(
    modifier: Modifier = Modifier,
    viewModel: PreviousGameDetailsViewModel = koinViewModel(),
    onBack: () -> Unit,
    onContinueGame: (Uuid, EntryType) -> Unit,
    onNavigateToImport: () -> Unit
) {
    val game by viewModel.gameWithEntries.collectAsState(initial = null)
    val lastEntry = game?.entries?.last()
    val snackbarHostState = remember { SnackbarHostState() }
    val appIcon = rememberBitmapFromResource(Res.drawable.ic_launcher_foreground)
    val appName = stringResource(Res.string.app_name)
    val isAvailableOn =
        stringResource(Res.string.is_available_on, appName)

    val textMeasurer = rememberTextMeasurer()
    val scope = rememberCoroutineScope()
    val launcher = rememberBackupFileSaver(
        onError = { failure ->
            scope.launch {
                snackbarHostState.showSnackbar("Backup failed: ${failure.message}")
            }
        },
        onSuccess = {
            scope.launch {
                snackbarHostState.showSnackbar("Backup saved successfully!")
            }
        },
    )
    var shareError by remember { mutableStateOf<String?>(null) }
    val shareLauncher = rememberShareFileLauncher(
        onError = { failure -> shareError = failure.message },
    )
    PreviousGameDetailsScreen(
        modifier = modifier,
        entries = game?.entries,
        onContinueGame = {
            onContinueGame(
                lastEntry?.id ?: Uuid.NIL,
                lastEntry?.type ?: EntryType.Unknown
            )
        },
        onBackupGame = {
            if (game?.entries?.isNotEmpty() == true) {
                scope.launch {
                    snackbarHostState.showSnackbar(getString(Res.string.saving))
                    val gamesJson = Json.encodeToString(listOf(game!!))
                    val bytes = Gzip.compress(gamesJson)
                    launcher.saveBackup(
                        bytes = bytes,
                        suggestedName = defaultDataFilename(),
                        extension = "gz"
                    )
                }
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar(getString(Res.string.no_games_to_save))
                }
            }
        },
        onImportGame = onNavigateToImport,
        onBack = onBack,
        onShareGame = {
            scope.launch {
                val ie = ImageExport(
                    game!!.entries,
                    appIcon,
                    appName,
                    isAvailableOn,
                    textMeasurer
                )
                val bytes = ie.makeBitmap().encodeToByteArray(ImageFormat.PNG)

                saveToGallery(bytes, defaultDataFilename())
                snackbarHostState.showSnackbar("Saved to device gallery")
                if (shareLauncher.isSupported) {
                    shareLauncher.launch(bytes, defaultDataFilename())
                }
            }
        },
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun PreviousGameDetailsScreen(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    entries: List<Entry>?,
    onBack: () -> Unit,
    onContinueGame: () -> Unit,
    onBackupGame: () -> Unit,
    onImportGame: () -> Unit,
    onShareGame: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var title = pluralStringResource(Res.plurals.previous_games, 1)
    if (entries?.firstOrNull()?.createdAt != null) {
        title += "\n${entries.first().createdAt.localDateTimestamp()}"
    }

    Scaffolds.PreviousGame(
        title = title,
        onBackupGame = onBackupGame,
        onImportGame = onImportGame,
        onShareGame = onShareGame,
        onContinueGame = onContinueGame,
        onBack = onBack,
    ) { innerPadding ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            Scaffold(
                floatingActionButton = {
                    Row {
                        FloatingActionButton(
                            modifier = Modifier.padding(3.dp),
                            onClick = {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(0)
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_vertical_align_top_rounded),
                                modifier = Modifier.padding(3.dp),
                                contentDescription = stringResource(Res.string.scroll_to_top)
                            )
                        }
                        FloatingActionButton(
                            modifier = Modifier.padding(3.dp),
                            onClick = onContinueGame
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_replay_rounded),
                                modifier = Modifier.padding(3.dp),
                                contentDescription = stringResource(Res.string.continue_previous_game)
                            )
                        }
                        FloatingActionButton(
                            modifier = Modifier.padding(3.dp),
                            onClick = onShareGame
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_share_filled),
                                modifier = Modifier.padding(3.dp),
                                contentDescription = stringResource(Res.string.share_this_game)
                            )
                        }
                    }
                },
            ) { contentPadding ->
                if (entries == null) {
                    Text("Loading...", modifier = Modifier.padding(contentPadding))
                    return@Scaffold
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.padding(contentPadding),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(
                            items = entries,
                            key = { entry -> entry.id }
                        ) { entry ->
                            EntryListItem(entry)
                        }
                    }
                    PlatformLazyVerticalScrollbar(
                        listState = listState,
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(innerPadding).fillMaxSize().wrapContentSize()
        )
    }
}

@Composable
fun EntryListItem(entry: Entry) {
    val sentence = entry.sentence
    val drawing = entry.drawing
    val playerName = entry.localPlayerName
    val createdAt = entry.createdAt.localTimestamp()

    if (sentence != null) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)) {
            Text(
                text = sentence,
                textAlign = TextAlign.Start,
                fontSize = 18.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.Start)
                    .padding(
                        PaddingValues(
                            start = 8.dp,
                            top = 16.dp,
                            end = 8.dp,
                            bottom = 12.dp
                        )
                    )
            )
        }
    }

    if (drawing != null) {
        DrawBox(
            drawingZippedJson = drawing
        )
    }
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "^^ ${playerName.valueOrEmpty()} $createdAt",
                modifier = Modifier.padding(end = 16.dp)
            )
        }
    }
}