package com.example.eatpoopyoucat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.nearby.connection.PayloadTransferUpdate.Status

class MainActivity : AppCompatActivity() {
    private val tag = "EatPoopYouCat"

    // Our handle to Nearby Connections
    private var connectionsClient: ConnectionsClient? = null

    // CodeName will be a hash of the sentence that starts the game
    private lateinit var codeName: String

    private var requiredPermissions = when {
        Build.VERSION.SDK_INT >= VERSION_CODES.S -> {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
        Build.VERSION.SDK_INT >= VERSION_CODES.Q -> {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
        else -> {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        }

    }
    private val requestCodeRequiredPermissions = 1
    private val strategy = Strategy.P2P_STAR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        codeName = getString(R.string.new_game)
        super.onStart()
        if (!hasPermissions(this, requiredPermissions)) {
            if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
                requestPermissions(requiredPermissions, requestCodeRequiredPermissions)
            }
        }
        startAdvertising()
        startDiscovery()
        setStatusText(getString(R.string.status_searching))
    }

    /** Returns true if the app was granted all the permissions. Otherwise, returns false.  */
    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
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
