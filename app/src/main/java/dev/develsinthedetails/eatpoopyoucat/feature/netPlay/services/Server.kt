package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.routes.GameRouter
import io.ktor.serialization.kotlinx.cbor.cbor
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.zstd.zstd
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.resources.Resources
import io.ktor.server.routing.routing
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.android.ext.android.inject

class Server : Service() {
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? =
        null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun onCreate() {
        createNotificationChannel()
        super.onCreate()
        val notification = Notification
            .Builder(this, "webserver")
            .setContentTitle("Host Game")
            .setContentText("Starting...")
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        } else {
            startForeground(1, notification)
        }

        if (server == null) {
            server =
                embeddedServer(Netty, port = 3947, host = "0.0.0.0", watchPaths = emptyList()) {
                    install(ContentNegotiation) {
                        cbor()
                    }
                    install(Compression) {
                        zstd()
                    }
                    install(Resources)
                    val gameRouter by inject<GameRouter>()
                    routing {
                        with(gameRouter) {
                            gameRoutes()
                        }
                    }
                }.start(wait = false)
        }
    }

    override fun onDestroy() {
        server?.stop(1000, 2000)
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