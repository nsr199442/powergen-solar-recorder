package com.callrecorder.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class SetupActivity : AppCompatActivity() {

    private lateinit var agentNameInput: EditText
    private lateinit var serverIpInput: EditText
    private lateinit var serverPortInput: EditText
    private lateinit var testConnectionButton: Button
    private lateinit var saveButton: Button

    companion object {
        private const val PREFS_NAME = "PowergenSolarPrefs"
        private const val KEY_AGENT_NAME = "agent_name"
        private const val KEY_SERVER_IP = "server_ip"
        private const val KEY_SERVER_PORT = "server_port"
        private const val KEY_SETUP_COMPLETE = "setup_complete"

        fun isSetupComplete(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_SETUP_COMPLETE, false)
        }

        fun getAgentName(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_AGENT_NAME, "") ?: ""
        }

        fun getServerUrl(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val ip = prefs.getString(KEY_SERVER_IP, "") ?: ""
            val port = prefs.getString(KEY_SERVER_PORT, "") ?: ""
            
            return if (ip.isNotEmpty() && port.isNotEmpty()) {
                "http://$ip:$port"
            } else {
                ""
            }
        }

        fun resetSetup(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_SETUP_COMPLETE, false).apply()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        agentNameInput = findViewById(R.id.agentNameInput)
        serverIpInput = findViewById(R.id.serverIpInput)
        serverPortInput = findViewById(R.id.serverPortInput)
        testConnectionButton = findViewById(R.id.testConnectionButton)
        saveButton = findViewById(R.id.saveButton)

        // Load saved values if any
        loadSavedValues()

        testConnectionButton.setOnClickListener {
            testConnection()
        }

        saveButton.setOnClickListener {
            saveConfiguration()
        }
    }

    private fun loadSavedValues() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        agentNameInput.setText(prefs.getString(KEY_AGENT_NAME, ""))
        serverIpInput.setText(prefs.getString(KEY_SERVER_IP, ""))
        serverPortInput.setText(prefs.getString(KEY_SERVER_PORT, ""))
    }

    private fun testConnection() {
        val agentName = agentNameInput.text.toString().trim()
        val ip = serverIpInput.text.toString().trim()
        val port = serverPortInput.text.toString().trim()

        if (agentName.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        if (ip.isEmpty() || port.isEmpty()) {
            Toast.makeText(this, "Please enter server IP and port", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidIpAddress(ip)) {
            Toast.makeText(this, "Invalid IP address format", Toast.LENGTH_SHORT).show()
            return
        }

        testConnectionButton.isEnabled = false
        testConnectionButton.text = "Testing..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("http://$ip:$port/health")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode

                withContext(Dispatchers.Main) {
                    if (responseCode == 200) {
                        Toast.makeText(
                            this@SetupActivity,
                            "✓ Connection successful!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@SetupActivity,
                            "Connection failed: HTTP $responseCode",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    testConnectionButton.isEnabled = true
                    testConnectionButton.text = "Test Connection"
                }

                connection.disconnect()

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SetupActivity,
                        "Connection failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    testConnectionButton.isEnabled = true
                    testConnectionButton.text = "Test Connection"
                }
            }
        }
    }

    private fun saveConfiguration() {
        val agentName = agentNameInput.text.toString().trim()
        val ip = serverIpInput.text.toString().trim()
        val port = serverPortInput.text.toString().trim()

        if (agentName.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        if (ip.isEmpty() || port.isEmpty()) {
            Toast.makeText(this, "Please enter server IP and port", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidIpAddress(ip)) {
            Toast.makeText(this, "Invalid IP address format", Toast.LENGTH_SHORT).show()
            return
        }

        // Save configuration
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_AGENT_NAME, agentName)
            .putString(KEY_SERVER_IP, ip)
            .putString(KEY_SERVER_PORT, port)
            .putBoolean(KEY_SETUP_COMPLETE, true)
            .apply()

        Toast.makeText(this, "Configuration saved!", Toast.LENGTH_SHORT).show()

        // Navigate to MainActivity
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun isValidIpAddress(ip: String): Boolean {
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
}
