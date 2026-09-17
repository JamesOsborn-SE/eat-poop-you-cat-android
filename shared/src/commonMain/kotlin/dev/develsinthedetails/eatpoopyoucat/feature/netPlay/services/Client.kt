package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.core.utilities.getRealAddress
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithRosters
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.cbor.cbor
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.time.Clock
import kotlin.uuid.Uuid

class Client(val repository: AppRepository) {
    @OptIn(ExperimentalSerializationApi::class)
    val httpClient = HttpClient {
        install(ContentNegotiation) { cbor() }
        install(Resources)
        defaultRequest {
            contentType(ContentType.Application.Cbor)
        }
    }

    // todo normalize url address
    suspend fun ping(address: Url, gameId: Uuid) {
        val address = getRealAddress(address)
        if (address.protocol.equals("http")) {
            val getGame = httpClient.get(Api.Ping()) {
                url {
                    protocol = URLProtocol.HTTP
                    host = address.host
                    port = address.port
                }
            }
            if (getGame.status == HttpStatusCode.OK)
                repository.updateRosterPing(address.toString(), gameId, Clock.System.now())
        }
    }

    suspend fun getGame(address: Url, gameId: Uuid): GameWithRosters? {
        val address = getRealAddress(address)
        if (address.protocol.equals("http")) {
            val getGame = httpClient.get((Api.GameRoot.Id(Api.GameRoot(), id = gameId))) {
                url {
                    protocol = URLProtocol.HTTP
                    host = address.host
                    port = address.port
                }
            }
            if (getGame.status == HttpStatusCode.OK)
                return getGame.body<GameWithRosters>()
        }
        return null
    }

    suspend fun joinGame(address: Url, player: Roster): Boolean {
        val address = getRealAddress(address)
        println("DEBUG: JoinGame Player:$player")
        if (address.protocol.equals("http")) {
            val req = httpClient.post(Api.GameRoot.JoinGame()) {
                url {
                    protocol = URLProtocol.HTTP
                    host = address.host
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
            val req = httpClient.post(
                Api.GameRoot.Id.AskTakeTurn(
                    Api.GameRoot.Id(
                        Api.GameRoot(),
                        id = player.gameId
                    )
                )
            ) {
                val address = getRealAddress(Url(player.address))
                url {
                    protocol = URLProtocol.HTTP
                    host = address.host
                    port = address.port
                }
                setBody(player)
            }
            return req.status.isSuccess()
        }
        return false
    }

    suspend fun takeTurn(url: String, entry: Entry): Boolean {
        if (url.startsWith("http")) {
            val address = getRealAddress(Url(url))
            val req = httpClient.put(
                Api.GameRoot.TakeTurn(
                    Api.GameRoot.Id(
                        Api.GameRoot(),
                        id = entry.gameId
                    )
                )
            ) {
                url {
                    protocol = URLProtocol.HTTP
                    host = address.host
                    port = address.port
                }
                setBody(entry)
            }
            return req.status.isSuccess()
        }
        return false
    }

    suspend fun updateRoster(url: String, gameId: Uuid, hash: String): GameWithRosters? {
        if (url.startsWith("http")) {
            val address = getRealAddress(Url(url))
            val req =
                httpClient.post(
                    Api.GameRoot.Id.UpdateRoster(
                        Api.GameRoot.Id(
                            Api.GameRoot(),
                            id = gameId
                        ), hash
                    )
                ) {
                    url {
                        protocol = URLProtocol.HTTP
                        host = address.host
                        port = address.port
                    }
                    setBody(hash)
                }
            return req.body()
        }
        return null
    }

    suspend fun updateGame(url: String, game: GameWithEntries): List<Entry> {
        val knownSequences = game.entries.map { it.sequence }
        if (url.startsWith("http")) {
            val address = getRealAddress(Url(url))
            val req = httpClient.post(
                Api.GameRoot.Id.UpdateGame(
                    Api.GameRoot.Id(
                        Api.GameRoot(),
                        id = game.game.id
                    )
                )
            ) {
                url {
                    protocol = URLProtocol.HTTP
                    host = address.host
                    port = address.port
                }
                setBody(knownSequences)
            }
            return req.body()
        }
        return emptyList()
    }
}