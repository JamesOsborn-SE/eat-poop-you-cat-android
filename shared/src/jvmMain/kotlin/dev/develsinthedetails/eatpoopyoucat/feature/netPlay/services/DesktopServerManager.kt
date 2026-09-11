package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import java.net.NetworkInterface

class DesktopServerManager(
    private val sharedKtorServer: SharedKtorServer
) : ServerManager {

    override val currentAddress: String?
        get() = getLocalIpv4Address()?.let { "http://$it:3947" }

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
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in interfaces) {
                if (networkInterface.isLoopback || !networkInterface.isUp) continue

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