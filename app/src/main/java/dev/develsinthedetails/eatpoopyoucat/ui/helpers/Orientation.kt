package dev.develsinthedetails.eatpoopyoucat.ui.helpers

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun OrientationSwapperEvenly(
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    flip: Boolean = false,
    vararg contents: @Composable () -> Unit
) {
    val isColumn =
        flip.xor(LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT)
    if (isColumn) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            for (content in contents) {
                content()
            }
        }
    } else {
        Row(
            modifier = rowModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            for (content in contents) {
                content()
            }
        }
    }
}

@Composable
fun OrientationSwapper(
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    flip: Boolean = false,
    vararg contents: @Composable () -> Unit
) {
    val isColumn =
        flip.xor(LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT)
    if (isColumn) {
        Column(
            modifier = modifier,
        ) {
            for (content in contents) {
                content()
            }
        }
    } else {
        Row(
            modifier = rowModifier,
        ) {
            for (content in contents) {
                content()
            }
        }
    }
}