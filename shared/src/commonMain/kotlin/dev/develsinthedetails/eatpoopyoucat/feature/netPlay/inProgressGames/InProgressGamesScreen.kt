package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.inProgressGames

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.CustomRoundedPolygon
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.PixelArtImage
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.PlatformLazyVerticalScrollbar
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Spinner
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.generateOrganicProfile
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.generatePixelProfile4Bit
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.PIXEL_PALETTE_4_BIT
import dev.develsinthedetails.eatpoopyoucat.core.utilities.localDateTimestamp
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithRosters
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.ManageServerLifecycle
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.ServerManager
import eatpoopyoucat.shared.generated.resources.Res
import eatpoopyoucat.shared.generated.resources.ic_delete_forever_rounded
import eatpoopyoucat.shared.generated.resources.ic_lan
import eatpoopyoucat.shared.generated.resources.ic_question_mark
import eatpoopyoucat.shared.generated.resources.ic_warning_rounded
import eatpoopyoucat.shared.generated.resources.ic_wifi
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Composable
fun InProgressGames(
    viewModel: InProgressGamesViewModel = koinViewModel(),
    serverManager: ServerManager = koinInject(),
    toGame: (Uuid) -> Unit,
    onBack: () -> Unit,
) {

    val games by viewModel.games.collectAsState(initial = null)
    ManageServerLifecycle(serverManager, onUpdateAddress = {})
    InProgressGames(
        games,
        viewModel.playerId,
        toGame,
        onBack,
        viewModel::endGame,
        viewModel::deleteGame,
    )
}

@Composable
fun InProgressGames(
    games: List<GameWithRosters>?,
    playerId: Uuid,
    toGame: (Uuid) -> Unit,
    onBack: () -> Unit,
    onEndGame: (Uuid) -> Unit,
    onDeleteGame: (Uuid) -> Unit
) {
    // TODO Pixel pushing
    val inProgressGames = games?.filter { it.game.turns == null }
    val listState = rememberLazyListState()
    Scaffolds.Backable("Network games", onBack = onBack) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 15.dp),
            color = MaterialTheme.colorScheme.background,
        ) {
            if (games == null) {
                Spinner()
                return@Surface
            }
            if (inProgressGames?.isEmpty() == true) {
                Text("go back start a game")
                return@Surface
            }
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.clickable(onClick = {}),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(bottom = 15.dp),
                            text = "In Progress Games"
                        )
                    }
                    itemsIndexed(inProgressGames!!.sortedByDescending { it.game.createdAt }) { index, gameWithRosters ->
                        ListGame(gameWithRosters, index, toGame, playerId, onEndGame, onDeleteGame)
                    }
                }
                PlatformLazyVerticalScrollbar(
                    listState = listState,
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                )
            }
        }
    }
}

private enum class GameSwipeAnchor {
    Closed,
    EndForAll,
    Delete
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListGame(
    gameWithRosters: GameWithRosters,
    index: Int,
    toGame: (Uuid) -> Unit,
    playerId: Uuid,
    onEndGame: (Uuid) -> Unit,
    onDeleteGame: (Uuid) -> Unit
) {
    val game = gameWithRosters.game
    val player = gameWithRosters.roster
    val actionWidth = 88.dp
    val actionWidthPx = with(LocalDensity.current) { actionWidth.toPx() }
    val scope = rememberCoroutineScope()

    val state = remember(game.id) {
        AnchoredDraggableState(initialValue = GameSwipeAnchor.Closed)
    }

    val rowColor = if (index % 2 == 0) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.background
    }

    Box(Modifier.fillMaxWidth()) {
        Row(Modifier.matchParentSize()) {
            Box(
                Modifier
                    .width(actionWidth)
                    .fillMaxHeight()
                    .background(Color.Blue)
                    .clickable {
                        scope.launch {
                            onEndGame(gameWithRosters.game.id)
                            state.animateTo(GameSwipeAnchor.Closed)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row {
                        Icon(
                            painterResource(Res.drawable.ic_warning_rounded),
                            contentDescription = "Delete"
                        )
                    }
                    Row {
                        Text("End for all", color = Color.White)
                    }
                }
            }
            Spacer(Modifier.weight(1f))

            Box(
                Modifier
                    .width(actionWidth)
                    .fillMaxHeight()
                    .background(Color.Red)
                    .clickable {
                        onDeleteGame(gameWithRosters.game.id)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row {
                        Icon(
                            painterResource(Res.drawable.ic_delete_forever_rounded),
                            contentDescription = "Delete"
                        )
                    }
                    Row {
                        Text("Delete", color = Color.White)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    IntOffset(state.requireOffset().roundToInt(), 0)
                }
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)

                    state.updateAnchors(
                        DraggableAnchors {
                            GameSwipeAnchor.Closed at 0f
                            GameSwipeAnchor.EndForAll at actionWidthPx
                            GameSwipeAnchor.Delete at -actionWidthPx
                        }
                    )

                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(0, 0)
                    }
                }
                .anchoredDraggable(state, Orientation.Horizontal)
                .background(rowColor)
                .clickable { toGame(game.id) }
                .padding(10.dp)
        ) {
            ListGameItem(game, player, playerId)
        }
    }
}

@Composable
private fun ListGameItem(
    game: Game,
    player: List<Roster>,
    playerId: Uuid
) {
    Row(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        Column {
            Text(
                text = "Created at: ${game.createdAt.localDateTimestamp()}",
                style = MaterialTheme.typography.bodyMedium
            )
            Row {
                when (game.gameMode) {
                    GameMode.LAN -> {
                        Icon(
                            painter = painterResource(Res.drawable.ic_lan),
                            contentDescription = "Share text",
                            modifier = Modifier
                                .size(50.dp)
                                .padding(end = 10.dp)
                        )
                    }

                    GameMode.INET ->
                        Icon(
                            painter = painterResource(Res.drawable.ic_wifi),
                            contentDescription = "Share text",
                            modifier = Modifier
                                .size(50.dp)
                                .padding(end = 10.dp)
                        )

                    else -> Icon(
                        painter = painterResource(Res.drawable.ic_question_mark),
                        contentDescription = "Share text"
                    )
                }
                val generatedProfile = generateOrganicProfile(game.id)
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(generatedProfile.backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    CustomRoundedPolygon(
                        generated = generatedProfile,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                LazyRow(modifier = Modifier.padding(horizontal = 3.dp)) {
                    itemsIndexed(player.take(4)) { _, entry ->
                        var m = Modifier
                            .size(50.dp)
                            .rotate(90f)
                            .padding(horizontal = 5.dp)
                        if (playerId == entry.playerId) {
                            m = m.dropShadow(
                                shape = RoundedCornerShape(3.dp),
                                shadow = Shadow(
                                    radius = 4.dp,
                                    spread = 2.dp,
                                    color = Color.Yellow,
                                    offset = DpOffset(x = 0.dp, 0.dp)
                                )
                            )
                        }
                        PixelArtImage(
                            generatePixelProfile4Bit(entry.playerId),
                            PIXEL_PALETTE_4_BIT, m
                        )
                    }
                }
            }
            Text(
                text = "Players: ${player.size}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview
@Composable
fun InProgressGamesPreview() {
    val playerId = Uuid.parse("085900db-809b-408b-b656-62fcfa1c921b")
    val gameId = Uuid.parse("085900db-809b-408b-b656-62fcfa1c921b")
    val gameId2 = Uuid.parse("927fb5d6-a27a-48b6-a97c-3494f17e6beb")
    val gameId3 = Uuid.parse("0d7b2219-6db7-4ab7-a3b1-2ad06169dfc9")
    val gameWithRosters = listOf(
        GameWithRosters(
            Game(
                gameId,
                turns = null,
                timeout = null,
                createdAt = Instant.fromEpochSeconds(1786057118),
                gameMode = GameMode.LAN
            ),
            listOf(
                Roster(
                    gameId,
                    playerId = playerId,
                    nickname = ""
                ),
                Roster(
                    gameId,
                    Uuid.parse("927fb5d6-a27a-48b6-a97c-3494f17e6beb"),
                    nickname = ""
                ),
                Roster(
                    gameId, Uuid.parse("670f27a7-e146-4463-8774-935958c8d298"),
                    nickname = ""
                )
            )
        ),
        GameWithRosters(
            Game(
                id = gameId2,
                turns = null,
                timeout = null,
                createdAt = Instant.fromEpochSeconds(1786057118),
                gameMode = GameMode.INET
            ),
            listOf(
                Roster(
                    gameId2,
                    playerId = playerId,
                    nickname = ""
                ),
                Roster(
                    gameId2, Uuid.parse("0ce3fd21-6b6e-41e3-9d4d-547a2f83b281"),
                    nickname = ""
                ),
                Roster(
                    gameId2, Uuid.parse("a81c33fb-c43f-46eb-9e95-f93485906e2e"),
                    nickname = ""
                ),
                Roster(
                    gameId2, Uuid.parse("bc58e47a-6e72-4509-bf4d-7b72b6af813f"),
                    nickname = ""
                ),
                Roster(
                    gameId2, Uuid.parse("6c47151b-c6cc-4d22-8a05-652779d1c72c"),
                    nickname = ""
                )
            )
        ),
        GameWithRosters(
            Game(
                gameId3,
                turns = null,
                timeout = null,
                createdAt = Instant.fromEpochSeconds(1786057118),
                gameMode = GameMode.LAN
            ),
            listOf(
                Roster(
                    gameId,
                    playerId = playerId,
                    nickname = ""
                ),
            )
        ),
    )

    AppTheme {
        InProgressGames(
            gameWithRosters,
            onBack = {},
            playerId = playerId,
            toGame = {},
            onEndGame = {},
            onDeleteGame = {}
        )
    }
}