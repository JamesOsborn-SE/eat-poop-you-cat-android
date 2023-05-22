package dev.develsinthedetails.eatpoopyoucat.permissionsUtiliy

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.core.content.ContextCompat

class PermissionsUtility {

    companion object {
        @JvmStatic
        private val requestCodeRequiredPermissions = 1
        private var requiredPermissions = when {
            Build.VERSION.SDK_INT >= VERSION_CODES.S -> {
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
            Build.VERSION.SDK_INT >= VERSION_CODES.R -> {
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                )
            }
            Build.VERSION.SDK_INT == VERSION_CODES.Q -> {
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
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }

        }

        fun requestPermissions(activity: Activity) {
            //TODO use this when features added
            return
            if (!hasPermissions(activity, requiredPermissions)) {
                activity.requestPermissions(requiredPermissions, requestCodeRequiredPermissions)
            }
        }

        /** Can't ask for ACCESS_BACKGROUND_LOCATION at the same time as location permissions  **/
        fun requestBackGroundPermission(activity: Activity) {
            //TODO use this when features added
            return
            if (!hasPermissions(activity, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)))
                if (Build.VERSION.SDK_INT >= VERSION_CODES.Q) {
                    activity.requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        2
                    )
                }
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
    }
}