package com.vcreate.ecg

import android.content.Context
import android.content.SharedPreferences

class SensitiveAddressPreferenceManager (context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences("ADDRESS", Context.MODE_PRIVATE)
    private val editor = preferences.edit()

    fun saveBluetoothAddress(address: String) {
        saveString("BLUETOOTH_ADDRESS", address)
    }

    fun getBluetoothAddress() : String? {
        return getString("BLUETOOTH_ADDRESS")
    }

    private fun saveString(key: String, value: String) {
        editor.putString(key, value)
        editor.apply()
    }

    private fun getString(key: String): String? {
        return preferences.getString(key, "")
    }
}