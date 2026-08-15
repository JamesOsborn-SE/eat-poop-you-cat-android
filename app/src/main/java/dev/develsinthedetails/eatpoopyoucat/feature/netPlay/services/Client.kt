package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import android.net.Uri
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithRosters
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import dev.develsinthedetails.eatpoopyoucat.data.models.hash
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
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
    }

    suspend fun ping(address: Uri, gameId: Uuid) {
        if (address.scheme.equals("http")) {
            val getGame = httpClient.get((Ping(gameId))) {
                url {
                    protocol = URLProtocol.HTTP
                    host = address.host.toString()
                    port = address.port
                }
            }
            if (getGame.status == HttpStatusCode.OK)
                repository.updateRosterPing(address, gameId, Clock.System.now())
        }
    }

    suspend fun getGame(address: Uri, gameId: Uuid): GetGameWithRosters? {
        if (address.scheme.equals("http")) {
            val getGame = httpClient.get((GetGameWithRosters(gameId))) {
                url {
                    protocol = URLProtocol.HTTP
                    host = address.host.toString()
                    port = address.port
                }
            }
            if (getGame.status == HttpStatusCode.OK)
                return getGame.body<GetGameWithRosters>()
        }
        return null
    }

    suspend fun joinGame(address: Uri, player: Roster): Boolean {
        if (address.scheme.equals("http")) {
            val req = httpClient.post(JoinGame()) {
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
        if (player.address.scheme.equals("http")) {
            val req = httpClient.post(AskTakeTurn(player.gameId)) {
                url {
                    protocol = URLProtocol.HTTP
                    host = player.address.host.toString()
                    port = player.address.port
                }
                setBody(player)
            }
        }
        return false
    }

    suspend fun takeTurn(address: Uri, entry: Entry): Boolean {
        if (address.scheme.equals("http")) {
            val req = httpClient.post(TakeTurn()) {
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

    suspend fun updateRoster(address: Uri, game: GameWithRosters): GameWithRosters? {
        if (address.scheme.equals("http")) {
            val req = httpClient.post(UpdateRoster(game.game.id)) {
                url {
                    protocol = URLProtocol.HTTP
                    host = address.host.toString()
                    port = address.port
                }
                setBody(game.hash())
            }
            return req.body()
        }
        return null
    }

    suspend fun updateGame(address: Uri, game: GameWithEntries): List<Entry> {
        val knownSequences = game.entries.map { it.sequence }
        if (address.scheme.equals("http")) {
            val req = httpClient.post(UpdateRoster(game.game.id)) {
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