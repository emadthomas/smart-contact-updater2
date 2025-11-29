package com.example.smartcontactupdater

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.smartcontactupdater.service.CallDetectionService
import com.example.smartcontactupdater.utils.PreferenceManager

class MainActivity : AppCompatActivity() {

    private lateinit var switchEnable: Switch
    private lateinit var btnSettings: Button
    private lateinit var prefManager: PreferenceManager

    private val PERMISSION_REQUEST_CODE = 100

    private val requiredPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_PHONE_STATE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefManager = PreferenceManager(this)

        initViews()
        setupListeners()
        checkPermissions()
    }

    private fun initViews() {
        switchEnable = findViewById(R.id.switchEnable)
        btnSettings = findViewById(R.id.btnSettings)

        switchEnable.isChecked = prefManager.isServiceEnabled()
    }

    private fun setupListeners() {
        switchEnable.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (hasAllPermissions()) {
                    enableService()
                } else {
                    switchEnable.isChecked = false
                    requestPermissions()
                }
            } else {
                disableService()
            }
        }

        btnSettings.setOnClickListener {
            showSettingsDialog()
        }
    }

    private fun enableService() {
        prefManager.setServiceEnabled(true)
        val intent = Intent(this, CallDetectionService::class.java)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        
        Toast.makeText(this, "Smart Contact Updater enabled", Toast.LENGTH_SHORT).show()
    }

    private fun disableService() {
        prefManager.setServiceEnabled(false)
        val intent = Intent(this, CallDetectionService::class.java)
        stopService(intent)
        
        Toast.makeText(this, "Smart Contact Updater disabled", Toast.LENGTH_SHORT).show()
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            if (!hasPermissions(notificationPermissions)) {
                ActivityCompat.requestPermissions(this, notificationPermissions, PERMISSION_REQUEST_CODE)
            }
        }
    }

    private fun hasAllPermissions(): Boolean {
        return hasPermissions(requiredPermissions)
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all { 
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSION_REQUEST_CODE)
    }

    private fun showSettingsDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        val input = android.widget.EditText(this)
        input.setText(prefManager.getServerUrl())
        
        builder.setTitle("Server URL")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val url = input.text.toString()
                prefManager.setServerUrl(url)
                Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions required for this app", Toast.LENGTH_LONG).show()
                switchEnable.isChecked = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        switchEnable.isChecked = prefManager.isServiceEnabled()
    }
}
