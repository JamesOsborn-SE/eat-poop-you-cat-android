package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.core.utilities.SERVER_PORT
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface

class DesktopServerManager(
    private val sharedKtorServer: SharedKtorServer
) : ServerManager {

    override val currentAddress: String?
        get() = getLocalIpv4Address()?.let { "http://$it:$SERVER_PORT" }

    override fun startServer() {
        sharedKtorServer.start()
    }

    override fun stopServer() {
        sharedKtorServer.stop()
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