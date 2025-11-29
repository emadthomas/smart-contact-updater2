package com.example.smartcontactupdater.utils

import android.content.ContentProviderOperation
import android.content.Context
import android.provider.ContactsContract
import android.database.Cursor

class ContactManager(private val context: Context) {

    fun getContactName(phoneNumber: String): String? {
        val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon()
            .appendPath(phoneNumber)
            .build()
        
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            
            if (cursor?.moveToFirst() == true) {
                val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    return cursor.getString(nameIndex)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        
        return null
    }

    fun updateContactWithTitle(phoneNumber: String, title: String): Boolean {
        try {
            val contactId = getContactId(phoneNumber) ?: return false
            val currentName = getContactName(phoneNumber) ?: return false
            
            val newName = if (hasTitle(currentName)) {
                replaceTitle(currentName, title)
            } else {
                "$title $currentName"
            }
            
            val ops = ArrayList<ContentProviderOperation>()
            
            ops.add(
                ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                        "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                        arrayOf(
                            contactId.toString(),
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                        )
                    )
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, newName)
                    .build()
            )
            
            val results = context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            return results.isNotEmpty()
            
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun getContactId(phoneNumber: String): Long? {
        val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon()
            .appendPath(phoneNumber)
            .build()
        
        val projection = arrayOf(ContactsContract.PhoneLookup._ID)
        
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            
            if (cursor?.moveToFirst() == true) {
                val idIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID)
                if (idIndex >= 0) {
                    return cursor.getLong(idIndex)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        
        return null
    }

    private fun hasTitle(name: String): Boolean {
        val titles = listOf("Dr", "Eng", "Prof", "Mr", "Mrs", "Ms", "Miss")
        val firstWord = name.split(" ").firstOrNull() ?: return false
        return titles.any { it.equals(firstWord.trim('.'), ignoreCase = true) }
    }

    private fun replaceTitle(name: String, newTitle: String): String {
        val parts = name.split(" ", limit = 2)
        return if (parts.size > 1) {
            "$newTitle ${parts[1]}"
        } else {
            "$newTitle $name"
        }
    }
}
