package dev.develsinthedetails.eatpoopyoucat

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.permissionsUtiliy.PermissionsUtility
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.nearby.connection.PayloadTransferUpdate.Status

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val tag = "EatPoopYouCat"

    // Our handle to Nearby Connections
    private var connectionsClient: ConnectionsClient? = null

    // CodeName will be a hash of the sentence that starts the game
    private lateinit var codeName: String

    private val strategy = Strategy.P2P_STAR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        PermissionsUtility.requestPermissions(this)
        codeName = getString(R.string.new_game)
        super.onStart()

        startAdvertising()
        startDiscovery()
        setStatusText(getString(R.string.status_searching))
        PermissionsUtility.requestBackGroundPermission(this)
    }

    /** Starts looking for other players using Nearby Connections.  */
    private fun startDiscovery() {
        // Note: Discovery may fail. To keep this demo simple, we don't handle failures.
        connectionsClient?.startDiscovery(
            packageName, endpointDiscoveryCallback,
            DiscoveryOptions.Builder().setStrategy(strategy).build()
        )
    }

    /** Broadcasts our presence using Nearby Connections so other players can find us.  */
    private fun startAdvertising() {
        // Note: Advertising may fail. To keep this demo simple, we don't handle failures.
        connectionsClient?.startAdvertising(
            codeName, packageName, connectionLifecycleCallback,
            AdvertisingOptions.Builder().setStrategy(strategy).build()
        )
    }

    // Callbacks for finding other devices
    private val endpointDiscoveryCallback: EndpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                Log.i(tag, "onEndpointFound: endpoint found, connecting")
                connectionsClient?.requestConnection(
                    codeName,
                    endpointId,
                    connectionLifecycleCallback
                )
            }

            override fun onEndpointLost(endpointId: String) {}
        }

    // Callbacks for connections to other devices
    private val connectionLifecycleCallback: ConnectionLifecycleCallback =
        object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                Log.i(tag, "onConnectionInitiated: accepting connection")
                connectionsClient?.acceptConnection(endpointId, payloadCallback)
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                if (result.status.isSuccess) {
                    Log.i(tag, "onConnectionResult: connection successful")
                    connectionsClient?.stopDiscovery()
                    connectionsClient?.stopAdvertising()
                    setStatusText(getString(R.string.status_connected))
                } else {
                    Log.i(tag, "onConnectionResult: connection failed")
                }
            }

            override fun onDisconnected(endpointId: String) {
                Log.i(tag, "onDisconnected: disconnected from the opponent")
            }
        }

    // Callbacks for receiving payloads
    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            //do stuff
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.status == Status.SUCCESS) {
                //do more stuff
                Log.wtf(tag, "Not implemented")
            }
        }
    }

    /** Shows a status message to the user.  */
    private fun setStatusText(text: String) {
        Log.wtf(tag, "Not implemented but got: $text")
    }
}
