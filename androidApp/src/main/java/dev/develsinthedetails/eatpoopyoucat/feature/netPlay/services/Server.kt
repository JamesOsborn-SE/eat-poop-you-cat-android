package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.cbor.cbor
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.android.ext.android.inject
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid

class Server : Service() {
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeMonitors = ConcurrentHashMap<Uuid, Job>()

    private val repository by inject<AppRepository>()
    private val appSettings by inject<AppSettings>()

    private val client by inject<Client>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun onCreate() {
        createNotificationChannel()
        super.onCreate()

        startForegroundServiceNotification()
        startKtorServer()

        observePendingGames()
    }

    private fun observePendingGames() {
        serviceScope.launch {
            repository.getActiveHostedGameWithRostersFlow(playerId =appSettings.playerId).collect { pendingGames ->
                val pendingGameIds = pendingGames.map { it.id }.toSet()

                val iterator = activeMonitors.entries.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    if (entry.key !in pendingGameIds) {
                        entry.value.cancel()
                        iterator.remove()
                    }
                }

                for (game in pendingGames) {
                    if (!activeMonitors.containsKey(game.id)) {
                        monitorGameStart(game.id, timeoutMillis = (game.timeout?.toLong()
                            ?: (60 * 1)) * 1000L
                        )
                    }
                }
            }
        }
    }

    private fun monitorGameStart(gameId: Uuid, targetPlayers: Int = 4, timeoutMillis: Long) {
        val job = serviceScope.launch {
            val rosterFlow = repository.getRostersByGameFlow(gameId)

            val triggeredRoster = withTimeoutOrNull(timeoutMillis.milliseconds) {
                rosterFlow.first { roster -> roster.size >= targetPlayers }
            }

            val finalRoster = triggeredRoster ?: rosterFlow.first()

            if (finalRoster.isNotEmpty()) {
                val startingPlayer = finalRoster.random()

                println("Game $gameId started automatically! Turn assigned to: ${startingPlayer.nickname}")

                client.askToTakeTurn(startingPlayer)
            }

            activeMonitors.remove(gameId)
        }

        activeMonitors[gameId] = job
    }

    private fun startForegroundServiceNotification() {
        val notification = Notification.Builder(this, "webserver")
            .setContentTitle("Net Play Game")
            .setContentText("Starting...")
            .setSmallIcon(R.drawable.ic_notification)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        } else {
            startForeground(1, notification)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun startKtorServer() {
        if (server == null) {
            server = embeddedServer(Netty, port = 3947, host = "0.0.0.0", watchPaths = emptyList()) {
                install(ContentNegotiation) { cbor() }
                install(Resources)

                val gameRouter by inject<GameRouter>()
                routing {
                    with(gameRouter) { gameRoutes() }
                    get("api/endpoints") {
                        val routes = this@routing.getAllRoutes()
                        call.respondText(routes.joinToString("\n"))
                    }
                }
                install(StatusPages) {
                    status(HttpStatusCode.NotFound) { call, status ->
                        call.respondText(text = "404: Page Not Found", status = status)
                    }
                }
            }.start(wait = false)
        }
    }

    override fun onDestroy() {
        server?.stop(1000, 2000)
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "webserver",
            "Web Server Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }
}