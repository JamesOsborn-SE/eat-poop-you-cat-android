package dev.develsinthedetails.eatpoopyoucat.feature.netPlay

import android.app.Notification
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.routing

class WebServerService: Service() {
    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
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
        super.onCreate()
    }

    override fun startForegroundService(service: Intent?): ComponentName? {
        val server = embeddedServer(Netty, port = 3947, host = "0.0.0.0") {
            routing {
                get("/status") {
                    call.respondText("Game Lobby Active")
                }
                get("/status/user/{userId}") {
                    // returns for the games the user has played, gameIds and status of each game (enum DONE, IN_PROGRESS, WAITING_FOR_TURN)
                }
                get("/status/game/{gameId}") {
                    // returns the status of the game
                }
                get("/game/{gameId}") {
                    // Gets the complete game with status
                }
                put("/user/{userId}/turn/{turnIndex}/game/{gameId}") {

                }
            }
        }
        server.start(wait = false)
        return super.startForegroundService(service)
    }
}