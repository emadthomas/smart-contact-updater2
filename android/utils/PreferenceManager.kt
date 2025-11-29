package com.example.smartcontactupdater.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "SmartContactPrefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_SERVICE_ENABLED = "service_enabled"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_API_KEY = "api_key"
    }

    fun isServiceEnabled(): Boolean {
        return prefs.getBoolean(KEY_SERVICE_ENABLED, false)
    }

    fun setServiceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SERVICE_ENABLED, enabled).apply()
    }

    fun getServerUrl(): String {
        return prefs.getString(KEY_SERVER_URL, "http://10.0.2.2:3000") ?: "http://10.0.2.2:3000"
    }

    fun setServerUrl(url: String) {
        prefs.edit().putString(KEY_SERVER_URL, url).apply()
    }

    fun getApiKey(): String {
        return prefs.getString(KEY_API_KEY, "mySecretKey123456") ?: "mySecretKey123456"
    }

    fun setApiKey(key: String) {
        prefs.edit().putString(KEY_API_KEY, key).apply()
    }
}
