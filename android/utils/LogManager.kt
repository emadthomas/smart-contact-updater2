package com.example.smartcontactupdater.utils

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class LogManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("logs", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun addLog(phoneNumber: String, title: String, message: String) {
        try {
            val logs = getLogs().toMutableList()
            
            val logEntry = JSONObject().apply {
                put("phoneNumber", phoneNumber)
                put("title", title)
                put("message", message)
                put("timestamp", dateFormat.format(Date()))
            }
            
            logs.add(0, logEntry)
            
            // Keep only last 50 logs
            if (logs.size > 50) {
                logs.subList(50, logs.size).clear()
            }
            
            saveLogs(logs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLogs(): List<JSONObject> {
        return try {
            val logsJson = prefs.getString("logs", "[]") ?: "[]"
            val logsArray = JSONArray(logsJson)
            
            (0 until logsArray.length()).map { logsArray.getJSONObject(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearLogs() {
        prefs.edit().remove("logs").apply()
    }

    private fun saveLogs(logs: List<JSONObject>) {
        val logsArray = JSONArray(logs)
        prefs.edit().putString("logs", logsArray.toString()).apply()
    }
}
