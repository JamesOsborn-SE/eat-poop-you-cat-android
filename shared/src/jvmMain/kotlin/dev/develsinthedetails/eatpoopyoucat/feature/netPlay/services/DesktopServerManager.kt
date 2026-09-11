package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import java.net.NetworkInterface

class DesktopServerManager(
    private val sharedKtorServer: SharedKtorServer
) : ServerManager {

    override val currentAddress: String?
        get() = getLocalIpv4Address()?.let { "http://$it:3947" }

    override fun startServer() {
        // Just start Ktor directly
        sharedKtorServer.start()
    }

    override fun stopServer() {
        sharedKtorServer.stop()
    }

    override fun promptNetworkSettings() {
        // Desktop doesn't have a unified OS-level intent for Wi-Fi settings.
        // A simple console log or triggering a custom Compose Dialog works here.
        println("ServerManager: Please ensure you are connected to a LAN/Wi-Fi network.")
    }

    // Standard JVM approach to finding the local network IP
    private fun getLocalIpv4Address(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in interfaces) {
                if (networkInterface.isLoopback || !networkInterface.isUp) continue

                for (address in networkInterface.inetAddresses) {
                    // Grab the first non-loopback IPv4 address
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