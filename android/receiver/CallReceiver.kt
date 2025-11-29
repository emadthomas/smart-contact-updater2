package com.example.smartcontactupdater.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.example.smartcontactupdater.service.CallDetectionService
import com.example.smartcontactupdater.utils.PreferenceManager

class CallReceiver : BroadcastReceiver() {

    companion object {
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var currentPhoneNumber: String? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prefManager = PreferenceManager(context)
        
        if (!prefManager.isServiceEnabled()) {
            return
        }

        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    currentPhoneNumber = incomingNumber
                }
                
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    if (lastState != TelephonyManager.CALL_STATE_OFFHOOK) {
                        currentPhoneNumber?.let { phoneNumber ->
                            // Start recording (note: this is simplified)
                            // In real implementation, you'd send command to service
                        }
                    }
                }
                
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    if (lastState == TelephonyManager.CALL_STATE_OFFHOOK) {
                        // Call ended (note: simplified)
                    }
                    currentPhoneNumber = null
                }
            }

            lastState = when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
                TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
                else -> TelephonyManager.CALL_STATE_IDLE
            }
        }
    }
}
