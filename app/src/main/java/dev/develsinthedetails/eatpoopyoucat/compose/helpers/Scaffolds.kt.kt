package dev.develsinthedetails.eatpoopyoucat.compose.helpers

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.develsinthedetails.eatpoopyoucat.R

@OptIn(ExperimentalMaterial3Api::class)
object Scaffolds {
    @Composable
    fun InGame(
        title: String,
        showEndGameConfirm: () -> Unit = {},
        bottomBar: @Composable () -> Unit = {},
        content: @Composable (PaddingValues) -> Unit,
    ) {
        var showMenu by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(title)
                    },
                    actions = {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.open)
                            )
                        }
                        DropdownMenu(expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                onClick = showEndGameConfirm,
                                text = { Text(stringResource(id = R.string.end_game_for_all)) })
                        }
                    },
                )
            },
            bottomBar = bottomBar,
            content = content
        )
    }

    @Composable
    fun Home(
        title: String,
        bottomBar: @Composable () -> Unit = {},
        content: @Composable (PaddingValues) -> Unit,
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(title, textAlign = TextAlign.Center)
                    },
                    actions = {},
                )
            },
            bottomBar = bottomBar,
            content = content
        )
    }
    @Composable
    fun PreviousGames(
        title: String,
        onBackupGames: () -> Unit,
        onImportGames: ManagedActivityResultLauncher<String, Uri?>?,
        bottomBar: @Composable () -> Unit = {},
        content: @Composable (PaddingValues) -> Unit,
    ) {
        var showMenu by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(title)
                    },
                    actions = {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(id = R.string.open)
                            )
                        }
                        DropdownMenu(expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    onImportGames!!.launch("application/gzip")
                                    showMenu = false
                                },
                                text = { Text(stringResource(id = R.string.import_games)) })
                            DropdownMenuItem(
                                onClick = {
                                    onBackupGames()
                                    showMenu = false
                                },
                                text = { Text(stringResource(id = R.string.backup_games)) })
                        }
                    },
                )
            },
            bottomBar = bottomBar,
            content = content
        )
    }

    @Composable
    fun PreviousGame(
        title: String,
        onContinueGame: () -> Unit,
        onShareGame: () -> Unit,
        onBackupGame: () -> Unit,
        onImportGame: ManagedActivityResultLauncher<String, Uri?>?,
        bottomBar: @Composable () -> Unit = {},
        content: @Composable (PaddingValues) -> Unit,
    ) {
        var showMenu by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(title)
                    },
                    actions = {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "open"
                            )
                        }
                        DropdownMenu(expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    onShareGame()
                                    showMenu = false
                                },
                                text = { Text(stringResource(id = R.string.share_this_game)) })
                            DropdownMenuItem(
                                onClick = {
                                    onContinueGame()
                                    showMenu = false
                                },
                                text = { Text(stringResource(id = R.string.continue_previous_game)) })
                            DropdownMenuItem(
                                onClick = {
                                    onBackupGame()
                                    showMenu = false
                                },
                                text = { Text(stringResource(id = R.string.backup_games)) })
                            DropdownMenuItem(
                                onClick = {
                                    onImportGame!!.launch("application/gzip")
                                    showMenu = false
                                },
                                text = { Text(stringResource(id = R.string.import_games)) })
                        }
                    },
                )
            },
            bottomBar = bottomBar,
            content = content
        )
    }
}

@Preview
@Composable
fun HomeBarPreview() {
    Scaffolds.Home(
        title = stringResource(
            id = R.string.welcome_message,
            stringResource(id = R.string.app_name)
        ), {}) {

    }
}