package com.callrecorder.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.callrecorder.app.databinding.ActivitySetupBinding
import java.net.URL

class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding

    companion object {
        private const val PREFS_NAME = "PowergenSolarPrefs"
        private const val KEY_SERVER_IP = "server_ip"
        private const val KEY_SERVER_PORT = "server_port"
        private const val KEY_SETUP_COMPLETED = "setup_completed"

        fun isSetupCompleted(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_SETUP_COMPLETED, false)
        }

        fun getServerUrl(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val ip = prefs.getString(KEY_SERVER_IP, "")
            val port = prefs.getString(KEY_SERVER_PORT, "3500")
            return if (ip.isNullOrEmpty()) {
                "" // Will fail uploads if not configured
            } else {
                "http://$ip:$port"
            }
        }

        fun clearSetup(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.tvWelcome.text = "Welcome to Powergen Solar\nCall Recording System"
        
        // Pre-fill with example
        binding.etServerIp.hint = "Example: 184.174.37.99"
        binding.etServerPort.setText("3500")

        binding.btnTestConnection.setOnClickListener {
            testConnection()
        }

        binding.btnSave.setOnClickListener {
            saveConfiguration()
        }
    }

    private fun testConnection() {
        val ip = binding.etServerIp.text.toString().trim()
        val port = binding.etServerPort.text.toString().trim()

        if (ip.isEmpty()) {
            Toast.makeText(this, "Please enter server IP address", Toast.LENGTH_SHORT).show()
            return
        }

        if (port.isEmpty()) {
            Toast.makeText(this, "Please enter server port", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidIP(ip)) {
            Toast.makeText(this, "Invalid IP address format", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidPort(port)) {
            Toast.makeText(this, "Invalid port number", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnTestConnection.isEnabled = false
        binding.tvStatus.text = "Testing connection..."

        // Test connection in background
        Thread {
            try {
                val url = "http://$ip:$port/health"
                val connection = URL(url).openConnection()
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.connect()
                
                val response = connection.getInputStream().bufferedReader().readText()
                
                runOnUiThread {
                    binding.tvStatus.text = "✓ Connection successful!"
                    binding.tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
                    Toast.makeText(this, "Server is reachable!", Toast.LENGTH_SHORT).show()
                    binding.btnSave.isEnabled = true
                }
                
            } catch (e: Exception) {
                runOnUiThread {
                    binding.tvStatus.text = "✗ Connection failed: ${e.message}"
                    binding.tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                    Toast.makeText(this, "Cannot reach server. Check IP and port.", Toast.LENGTH_LONG).show()
                    binding.btnSave.isEnabled = true // Allow saving even if test fails
                }
            } finally {
                runOnUiThread {
                    binding.btnTestConnection.isEnabled = true
                }
            }
        }.start()
    }

    private fun saveConfiguration() {
        val ip = binding.etServerIp.text.toString().trim()
        val port = binding.etServerPort.text.toString().trim()

        if (ip.isEmpty()) {
            Toast.makeText(this, "Please enter server IP address", Toast.LENGTH_SHORT).show()
            return
        }

        if (port.isEmpty()) {
            Toast.makeText(this, "Please enter server port", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidIP(ip)) {
            Toast.makeText(this, "Invalid IP address format", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidPort(port)) {
            Toast.makeText(this, "Invalid port number", Toast.LENGTH_SHORT).show()
            return
        }

        // Save to SharedPreferences
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_SERVER_IP, ip)
            putString(KEY_SERVER_PORT, port)
            putBoolean(KEY_SETUP_COMPLETED, true)
            apply()
        }

        Toast.makeText(this, "Configuration saved!", Toast.LENGTH_SHORT).show()

        // Go to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun isValidIP(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        
        return parts.all { part ->
            try {
                val num = part.toInt()
                num in 0..255
            } catch (e: NumberFormatException) {
                false
            }
        }
    }

    private fun isValidPort(port: String): Boolean {
        return try {
            val portNum = port.toInt()
            portNum in 1..65535
        } catch (e: NumberFormatException) {
            false
        }
    }

    override fun onBackPressed() {
        // Prevent going back without setup
        Toast.makeText(this, "Please complete the setup to continue", Toast.LENGTH_SHORT).show()
    }
}
