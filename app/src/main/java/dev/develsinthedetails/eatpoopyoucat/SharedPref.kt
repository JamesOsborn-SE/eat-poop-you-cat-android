package dev.develsinthedetails.eatpoopyoucat

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import java.util.UUID


object SharedPref {
    private var mSharedPref: SharedPreferences? = null
    const val PLAYER_ID = "PLAYER_ID"
    fun init(context: Context) {
        if (mSharedPref == null) mSharedPref =
            context.getSharedPreferences(context.packageName, Activity.MODE_PRIVATE)
    }

    fun playerId(): UUID {
        var result: String? = read(PLAYER_ID, null)
        if (result == null) {
            result = UUID.randomUUID().toString()
            write(PLAYER_ID, result)
        }
        return UUID.fromString(result)
    }

    private fun read(key: String, defValue: String?): String? {
        return mSharedPref!!.getString(key, defValue)
    }

    fun write(key: String, value: String?) {
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.putString(key, value)
        prefsEditor.apply()
    }
}