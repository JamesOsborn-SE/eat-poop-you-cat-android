package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import android.net.Uri
import androidx.core.net.toUri
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithRosters
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.cbor.cbor
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.time.Clock
import kotlin.uuid.Uuid

class Client(val repository: AppRepository) {
    @OptIn(ExperimentalSerializationApi::class)
    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            cbor()
        }
        install(Resources)
        defaultRequest {
            contentType(ContentType.Application.Cbor)
        }
    }

    suspend fun ping(address: Uri, gameId: Uuid) {
        if (address.scheme.equals("http")) {
            val getGame = httpClient.get(Ping()) {
                url {
                    protocol = URLProtocol.HTTP
                    host = address.host.toString()
                    port = address.port
                }
            }
            if (getGame.status == HttpStatusCode.OK)
                repository.updateRosterPing(address.toString(), gameId, Clock.System.now())
        }
    }

    suspend fun getGame(address: Uri, gameId: Uuid): GameWithRosters? {
        if (address.scheme.equals("http")) {
            val getGame = httpClient.get((GameRoot.Id(GameRoot(),id=gameId))) {
                url {
                    protocol = URLProtocol.HTTP
                    host = address.host.toString()
                    port = address.port
                }
            }
            if (getGame.status == HttpStatusCode.OK)
                return getGame.body<GameWithRosters>()
        }
        return null
    }

    suspend fun joinGame(address: Uri, player: Roster): Boolean {
        if (address.scheme.equals("http")) {
            val req = httpClient.post(GameRoot.JoinGame()) {
                url {
                    protocol = URLProtocol.HTTP
                    host = address.host.toString()
                    port = address.port
                }
                setBody(player)
            }
            return req.status.isSuccess()
        }
        return false
    }

    suspend fun askToTakeTurn(player: Roster): Boolean {
        if (player.address.startsWith("http")) {
            val req = httpClient.get(GameRoot.Id.AskTakeTurn(GameRoot.Id(GameRoot(),id = player.gameId))) {
                val address = player.address.toUri()
                url {
                    protocol = URLProtocol.HTTP
                    host = address.host.toString()
                    port = address.port
                }
                setBody(player)
            }
        }
        return false
    }

    suspend fun takeTurn(uri: String, entry: Entry): Boolean {
        if (uri.startsWith("http")) {
            val address = uri.toUri()
            val req = httpClient.put(GameRoot.TakeTurn()) {
                url {
                    protocol = URLProtocol.HTTP
                    host = address.host.toString()
                    port = address.port
                }
                setBody(entry)
            }
            return req.status.isSuccess()
        }
        return false
    }

    suspend fun updateRoster(uri: String, gameId: Uuid, hash: String): GameWithRosters? {
        if (uri.startsWith("http")) {
            val address = uri.toUri()
            val req =
                httpClient.post(GameRoot.Id.UpdateRoster(GameRoot.Id(GameRoot(),id = gameId), hash)) {
                    url {
                        protocol = URLProtocol.HTTP
                        host = address.host.toString()
                        port = address.port
                    }
                    setBody(hash)
                }
            return req.body()
        }
        return null
    }

    suspend fun updateGame(uri: String, game: GameWithEntries): List<Entry> {
        val knownSequences = game.entries.map { it.sequence }
        if (uri.startsWith("http")) {
            val address = uri.toUri()
            val req = httpClient.post(GameRoot.Id.UpdateGame(GameRoot.Id(GameRoot(),id=game.game.id))) {
                url {
                    protocol = URLProtocol.HTTP
                    host = address.host.toString()
                    port = address.port
                }
                setBody(knownSequences)
            }
            return req.body()
        }
        return emptyList()
    }

}