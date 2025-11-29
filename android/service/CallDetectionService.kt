package com.example.smartcontactupdater.service

import android.app.*
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.smartcontactupdater.MainActivity
import com.example.smartcontactupdater.R
import com.example.smartcontactupdater.api.ApiClient
import com.example.smartcontactupdater.detector.TitleDetector
import com.example.smartcontactupdater.utils.ContactManager
import kotlinx.coroutines.*
import java.io.File

class CallDetectionService : Service() {

    private val CHANNEL_ID = "CallDetectionChannel"
    private val NOTIFICATION_ID = 1
    
    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var recordingJob: Job? = null
    private var currentPhoneNumber: String? = null
    
    private lateinit var apiClient: ApiClient
    private lateinit var titleDetector: TitleDetector
    private lateinit var contactManager: ContactManager
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        
        apiClient = ApiClient(this)
        titleDetector = TitleDetector()
        contactManager = ContactManager(this)
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    fun startCallRecording(phoneNumber: String) {
        currentPhoneNumber = phoneNumber
        
        try {
            val outputDir = getExternalFilesDir(null)
            recordingFile = File.createTempFile("call_", ".3gp", outputDir)
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(recordingFile?.absolutePath)
                
                try {
                    prepare()
                    start()
                    
                    recordingJob = serviceScope.launch {
                        delay(10000)
                        stopRecording()
                    }
                    
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            recordingJob?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun endCall() {
        stopRecording()
        
        val phoneNumber = currentPhoneNumber
        val file = recordingFile
        
        if (phoneNumber != null && file != null && file.exists()) {
            processRecording(phoneNumber, file)
        }
        
        currentPhoneNumber = null
        recordingFile = null
    }

    private fun processRecording(phoneNumber: String, file: File) {
        serviceScope.launch {
            try {
                val response = apiClient.transcribeAudio(file)
                
                if (response != null) {
                    val transcript = response.transcript
                    val detectedTitle = titleDetector.detectTitle(transcript)
                    
                    if (detectedTitle != null) {
                        val contactName = contactManager.getContactName(phoneNumber)
                        
                        if (contactName != null) {
                            showUpdateNotification(phoneNumber, contactName, detectedTitle)
                        }
                    }
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                file.delete()
            }
        }
    }

    private fun showUpdateNotification(phoneNumber: String, contactName: String, title: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Title Detected: $title")
            .setContentText("Update contact '$contactName'?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(phoneNumber.hashCode(), notification)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Smart Contact Updater")
            .setContentText("Monitoring calls...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Call Detection",
                NotificationManager.IMPORTANCE_LOW
            )
            
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
        serviceScope.cancel()
    }
}
