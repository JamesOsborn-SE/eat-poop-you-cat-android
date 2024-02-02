package dev.develsinthedetails.eatpoopyoucat.compose.helpers

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.res.stringResource
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
                                contentDescription = "open"
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
}