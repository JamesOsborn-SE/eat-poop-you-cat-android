package dev.develsinthedetails.eatpoopyoucat.feature.inProgressGames

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.CustomRoundedPolygon
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.PixelArtImage
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
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Composable
fun InProgressGames(
    viewModel: InProgressGamesViewModel = koinViewModel(),
    toGame: (Uuid) -> Unit,
    onBack: () -> Unit
) {
    val games by viewModel.games.collectAsState(initial = null)
    InProgressGames(games, viewModel.playerId, toGame, onBack)
}

@Composable
fun InProgressGames(
    games: List<GameWithRosters>?,
    playerId: Uuid,
    toGame: (Uuid) -> Unit,
    onBack: () -> Unit
) {

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
            if (games.isEmpty()) {
                Text("go back start a game")
                return@Surface
            }
            val waitingGame = games.filter { g ->
                g.roster.any { r ->
                    r.playerId == playerId && (r.sequence ?: -1) < 0
                }
            }
            val notWaiting = games.filter { g ->
                g.roster.any { r ->
                    r.playerId == playerId && (r.sequence ?: -1) >= 0
                }
            }
            Column {
                // TODO Pixel pushing
                if (waitingGame.isNotEmpty()) {
                    Text(
                        "Waiting for turn", modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 20.dp)
                    )
                    ListGames(waitingGame, toGame, playerId)
                    HorizontalDivider(modifier = Modifier.padding(20.dp))
                }

                if (notWaiting.isNotEmpty()) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 15.dp),
                        text = "In Progress Games"
                    )
                    ListGames(notWaiting, toGame, playerId)
                }
            }
        }
    }

}

@Composable
private fun ListGames(
    games: List<GameWithRosters>,
    toGame: (Uuid) -> Unit,
    playerId: Uuid
) {
    LazyColumn(
        modifier = Modifier.clickable(onClick = {}),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(games.sortedByDescending { it.game.createdAt }) { index, gameWithRosters ->
            val game = gameWithRosters.game
            val player = gameWithRosters.roster
            val rowColor = if (index % 2 == 0) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.background
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rowColor)
                    .padding(10.dp)
                    .clickable { toGame(gameWithRosters.game.id) }
            ) {
                Text(
                    text = "Created at: ${game.createdAt.localDateTimestamp()}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row {
                    when (game.gameMode) {
                        GameMode.LAN -> {
                            Icon(
                                imageVector = Icons.Default.Lan,
                                contentDescription = "Share text",
                                modifier = Modifier
                                    .size(50.dp)
                                    .padding(end = 10.dp)
                            )
                        }

                        GameMode.INET ->
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "Share text",
                                modifier = Modifier
                                    .size(50.dp)
                                    .padding(end = 10.dp)
                            )

                        else -> Icon(
                            imageVector = Icons.Default.QuestionMark,
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
                        itemsIndexed(player.sortedBy { it.sequence }
                            .take(4)) { index, entry ->
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
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InProgressGamesPreview() {
    // Todo replace randos with static Uuids for screenshot reasons
    val playerId = Uuid.random()
    val gameId = Uuid.random()
    val gameId2 = Uuid.random()
    val gameId3 = Uuid.random()
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
                    sequence = 0,
                    nickname = "",
                    address = "http://127.0.0.1:666".toUri(),
                    isLeader = false,
                    lastSeen = Instant.fromEpochSeconds(1786057118)
                ),
                Roster(
                    gameId, Uuid.random(),
                    sequence = 1,
                    nickname = "",
                    address = "http://127.0.0.1:666".toUri(),
                    isLeader = false,
                    lastSeen = Instant.fromEpochSeconds(1786057118)
                ),
                Roster(
                    gameId, Uuid.random(),
                    sequence = 3,
                    nickname = "",
                    address = "http://127.0.0.1:666".toUri(),
                    isLeader = false,
                    lastSeen = Instant.fromEpochSeconds(1786057118)
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
                    sequence = 2,
                    nickname = "",
                    address = "http://127.0.0.1:666".toUri(),
                    isLeader = false,
                    lastSeen = Instant.fromEpochSeconds(1786057118)
                ),
                Roster(
                    gameId2, Uuid.random(),
                    sequence = 1,
                    nickname = "",
                    address = "http://127.0.0.1:666".toUri(),
                    isLeader = false,
                    lastSeen = Instant.fromEpochSeconds(1786057118)
                ),
                Roster(
                    gameId2, Uuid.random(),
                    sequence = 0,
                    nickname = "",
                    address = "http://127.0.0.1:666".toUri(),
                    isLeader = false,
                    lastSeen = Instant.fromEpochSeconds(1786057118)
                ),
                Roster(
                    gameId2, Uuid.random(),
                    sequence = 3,
                    nickname = "",
                    address = "http://127.0.0.1:666".toUri(),
                    isLeader = false,
                    lastSeen = Instant.fromEpochSeconds(1786057118)
                ),
                Roster(
                    gameId2, Uuid.random(),
                    sequence = 4,
                    nickname = "",
                    address = "http://127.0.0.1:666".toUri(),
                    isLeader = false,
                    lastSeen = Instant.fromEpochSeconds(1786057118)
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
                    sequence = -1,
                    nickname = "",
                    address = "http://127.0.0.1:666".toUri(),
                    isLeader = false,
                    lastSeen = Instant.fromEpochSeconds(1786057118)
                ),
            )
        ),
    )

    AppTheme {
        InProgressGames(
            gameWithRosters, onBack = {}, playerId = playerId, toGame = {}
        )
    }
}