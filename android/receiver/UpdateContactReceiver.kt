package com.example.smartcontactupdater.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.smartcontactupdater.utils.ContactManager

class UpdateContactReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: return
        val title = intent.getStringExtra("TITLE") ?: return
        
        val contactManager = ContactManager(context)
        val success = contactManager.updateContactWithTitle(phoneNumber, title)
        
        if (success) {
            Toast.makeText(context, "Contact updated with $title", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to update contact", Toast.LENGTH_SHORT).show()
        }
    }
}
