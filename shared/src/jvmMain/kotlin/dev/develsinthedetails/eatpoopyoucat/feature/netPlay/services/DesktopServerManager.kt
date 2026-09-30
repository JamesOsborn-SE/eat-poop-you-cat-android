package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.core.utilities.SERVER_PORT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface

class DesktopServerManager(
    private val sharedKtorServer: SharedKtorServer,
) : ServerManager {

    private val _serverState = MutableStateFlow<ServerState>(ServerState.Stopped)

    override val serverState: StateFlow<ServerState> = _serverState.asStateFlow()

    override fun startServer() {
        _serverState.value = ServerState.Starting

        val ip = getLocalIpv4Address()

        if (ip == null) {
            _serverState.value = ServerState.Error("No active network connection detected.")
            return
        }

        sharedKtorServer.start()

        val address = "http://$ip:$SERVER_PORT"
        _serverState.value = ServerState.Running(address)
    }

    override fun stopServer() {
        sharedKtorServer.stop()
        _serverState.value = ServerState.Stopped
    }

    override fun promptNetworkSettings() {
        println("ServerManager: Please ensure you are connected to a LAN/Wi-Fi network.")
    }

    private fun getLocalIpv4Address(): String? {
        return try {
            DatagramSocket().use { socket ->
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002)
                socket.localAddress.hostAddress
            }
        } catch (e: Exception) {
            getFallbackLocalIpv4Address()
        }
    }

    private fun getFallbackLocalIpv4Address(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in interfaces) {
                if (networkInterface.isLoopback || !networkInterface.isUp) continue
                if (networkInterface.displayName.contains("docker") ||
                    networkInterface.displayName.contains("veth") ||
                    networkInterface.displayName.contains("br-") ||
                    networkInterface.displayName.contains("waydroid")
                ) continue

                for (address in networkInterface.inetAddresses) {
                    if (!address.isLoopbackAddress && address.hostAddress.indexOf(':') < 0) {
                        return address.hostAddress
                    }
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}