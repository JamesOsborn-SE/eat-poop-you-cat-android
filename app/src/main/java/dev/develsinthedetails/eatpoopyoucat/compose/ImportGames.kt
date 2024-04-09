package dev.develsinthedetails.eatpoopyoucat.compose

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import dev.develsinthedetails.eatpoopyoucat.MainActivity
import dev.develsinthedetails.eatpoopyoucat.compose.helpers.SpinnerScreen
import dev.develsinthedetails.eatpoopyoucat.data.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.utilities.Gzip
import dev.develsinthedetails.eatpoopyoucat.utilities.Screen
import dev.develsinthedetails.eatpoopyoucat.viewmodels.ImportGamesViewModel
import kotlinx.coroutines.async
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

@Composable
fun ImportGames(
    viewModel: ImportGamesViewModel = hiltViewModel(),
    fileUri: Uri?,
    finish: () -> Unit
) {
    val context = LocalContext.current
    if (fileUri == null) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        finish()
        return
    }
    val inputStream = context.contentResolver.openInputStream(fileUri)
    val cba = readAllBytesWorkAround(inputStream) ?: return
    val gamesString = Gzip.decompressToString(cba)
    val games = Json.decodeFromString<List<GameWithEntries>>(gamesString)
    val addedGames = viewModel.numberOfGamesAdded.observeAsState(initial = 0)
    val addedEntries = viewModel.numberOfEntriesAdded.observeAsState(initial = 0)
    val finished = viewModel.isFinished.observeAsState(initial = false)
    var showAlert by remember { mutableStateOf(false) }

    val onDismissRequest = {
        finish()
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("routeTo", Screen.Games.route)
        context.startActivity(intent)
    }

    if (!finished.value)
        SpinnerScreen()
    else
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
        )

    LaunchedEffect(key1 = fileUri) {
        async {
            viewModel.addGames(games) {
                inputStream?.close()
            }
        }.await()
        showAlert = true
    }

    if (showAlert && finished.value)
        AlertDialog(
            title = { Text(text = "Import") },
            text = { Text(text = "Imported ${addedGames.value} game(s) and ${addedEntries.value} Entrie(s)") },
            onDismissRequest = { onDismissRequest() },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text("Werd")
                }
            })
}


@Throws(IOException::class)
private fun readAllBytesWorkAround(inputStream: InputStream?): ByteArray? {
    if (inputStream == null)
        return null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return inputStream.readAllBytes()
    }

    val bufLen = 4 * 0x400 // 4KB
    val buf = ByteArray(bufLen)
    var readLen: Int
    var exception: IOException? = null
    try {
        ByteArrayOutputStream().use { outputStream ->
            while (inputStream.read(buf, 0, bufLen)
                    .also { readLen = it } != -1
            ) outputStream.write(buf, 0, readLen)
            return outputStream.toByteArray()
        }
    } catch (e: IOException) {
        exception = e
        throw e
    } finally {
        if (exception == null) inputStream.close() else try {
            inputStream.close()
        } catch (e: IOException) {
            exception.addSuppressed(e)
        }
    }
}
