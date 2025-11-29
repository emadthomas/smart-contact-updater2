package com.example.smartcontactupdater.api

import android.content.Context
import com.example.smartcontactupdater.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class ApiClient(context: Context) {

    private val prefManager = PreferenceManager(context)
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun transcribeAudio(audioFile: File): TranscriptionResponse? = withContext(Dispatchers.IO) {
        try {
            val serverUrl = prefManager.getServerUrl()
            val apiKey = prefManager.getApiKey()
            
            if (serverUrl.isBlank() || apiKey.isBlank()) {
                throw Exception("Server URL or API Key not configured")
            }
            
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    audioFile.name,
                    audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
                )
                .build()
            
            val request = Request.Builder()
                .url("$serverUrl/transcribe")
                .addHeader("x-api-key", apiKey)
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                
                TranscriptionResponse(
                    transcript = jsonResponse.optString("transcript", ""),
                    provider = jsonResponse.optString("provider", "unknown")
                )
            } else {
                throw Exception("Server error: ${response.code}")
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

data class TranscriptionResponse(
    val transcript: String,
    val provider: String
)
