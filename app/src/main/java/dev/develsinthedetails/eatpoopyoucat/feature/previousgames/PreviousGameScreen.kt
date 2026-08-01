package dev.develsinthedetails.eatpoopyoucat.feature.previousgames

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.VerticalAlignTop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.feature.draw.DrawBox
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.SpinnerScreen
import dev.develsinthedetails.eatpoopyoucat.core.utilities.ImageExport
import dev.develsinthedetails.eatpoopyoucat.core.utilities.getBitmapFromVectorDrawable
import dev.develsinthedetails.eatpoopyoucat.core.utilities.localTimestamp
import dev.develsinthedetails.eatpoopyoucat.core.utilities.saveBitmap
import dev.develsinthedetails.eatpoopyoucat.core.utilities.shareImageUri
import dev.develsinthedetails.eatpoopyoucat.core.utilities.valueOrEmpty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.text.DateFormat
import kotlin.uuid.Uuid

@Composable
fun PreviousGameScreen(
    modifier: Modifier = Modifier,
    viewModel: PreviousGameViewModel = koinViewModel(),
    onBack: () -> Unit,
    onContinueGame: (Uuid) -> Unit = {},
    onBackupGame: (games: List<GameWithEntries>?) -> Unit,
    onImportGames: ManagedActivityResultLauncher<String, Uri?>,
) {
    val game by viewModel.gameWithEntries.observeAsState(initial = null)

    if (game != null) {
        PreviousGameScreen(
            modifier = modifier,
            entries = game!!.entries,
            onContinueGame = { onContinueGame(game!!.entries.last().id) },
            onBackupGame = { onBackupGame(listOf(game!!)) },
            onImportGame = onImportGames,
            onBack = onBack,
        )
    } else
        SpinnerScreen()
}

@Composable
fun PreviousGameScreen(
    modifier: Modifier = Modifier,
    entries: List<Entry>,
    onBack: () -> Unit,
    onContinueGame: () -> Unit,
    onBackupGame: () -> Unit,
    onImportGame: ManagedActivityResultLauncher<String, Uri?>?,
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val appName = stringResource(id = R.string.app_name)
    val bottomBlurb = stringResource(R.string.is_available_on_f_droid_and_google_play, appName)
    val option = BitmapFactory.Options()
    option.inPreferredConfig = Bitmap.Config.ARGB_8888
    val appIcon = getBitmapFromVectorDrawable(LocalContext.current, R.mipmap.ic_launcher_round)

    val shareGame = {
        shareGame(
            coroutineScope,
            entries,
            appIcon,
            appName,
            bottomBlurb,
            context
        )
    }
    var title = pluralStringResource(id = R.plurals.previous_games, 1)
    if (entries.first().createdAt != null)
        title += "\n${DateFormat.getDateInstance().format(entries.first().createdAt!!)}"
    Scaffolds.PreviousGame(
        title = title,
        onBackupGame = onBackupGame,
        onImportGame = onImportGame,
        onShareGame = shareGame(),
        onContinueGame = onContinueGame,
        onBack = onBack,
    )
    { innerPadding ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            Scaffold(floatingActionButton = {
                Row {
                    FloatingActionButton(
                        modifier = Modifier.padding(3.dp),
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        }) {

                        Icon(
                            Icons.Rounded.VerticalAlignTop,
                            modifier = Modifier.padding(3.dp),
                            contentDescription = stringResource(id = R.string.scroll_to_top)
                        )
                    }
                    FloatingActionButton(
                        modifier = Modifier.padding(3.dp),
                        onClick = onContinueGame
                    ) {
                        Icon(
                            Icons.Rounded.Replay,
                            modifier = Modifier.padding(3.dp),
                            contentDescription = stringResource(id = R.string.continue_previous_game)
                        )
                    }
                    FloatingActionButton(
                        modifier = Modifier.padding(3.dp),
                        onClick = shareGame()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            modifier = Modifier.padding(3.dp),
                            contentDescription = stringResource(R.string.share_this_game)
                        )
                    }
                }
            },
                content = { contentPadding ->
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.padding(contentPadding),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {

                        items(
                            items = entries,
                            key = { entry ->
                                entry.id
                            }
                        ) { entry ->
                            EntryListItem(entry)
                        }
                    }
                })
        }
    }
}

private fun shareGame(
    coroutineScope: CoroutineScope,
    entries: List<Entry>,
    appIcon: Bitmap,
    appName: String,
    bottomBlurb: String,
    context: Context
): () -> Unit = {
    coroutineScope.launch {
        val ie = ImageExport(
            entries,
            appIcon,
            appName,
            bottomBlurb
        )
        val game = saveBitmap(context, ie.makeBitmap())

        if (game != null)
            shareImageUri(context, game)
        else
            Toast.makeText(
                context,
                context.getString(R.string.share_failed),
                Toast.LENGTH_SHORT
            ).show()
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

@Preview(device = "spec:parent=Nexus 7 2013")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(device = "spec:parent=Nexus 7 2013,orientation=landscape")
@Preview(
    device = "spec:parent=Nexus 7 2013,orientation=landscape",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviousGameScreenPreviewWrapper() {
    PreviousGameScreenPreview()
}
