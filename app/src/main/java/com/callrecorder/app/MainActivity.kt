package com.callrecorder.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.callrecorder.app.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recordingsAdapter: RecordingsAdapter
    private val recordings = mutableListOf<RecordingFile>()
    
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if setup is completed
        if (!SetupActivity.isSetupComplete(this)) {
            val intent = Intent(this, SetupActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupButtons()
        
        // Request permissions
        checkAndRequestPermissions()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                showServerSettings()
                true
            }
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showServerSettings() {
        val serverUrl = SetupActivity.getServerUrl(this)
        
        AlertDialog.Builder(this)
            .setTitle("Server Configuration")
            .setMessage("Current Server:\n$serverUrl\n\nWould you like to change the server settings?")
            .setPositiveButton("Change") { _, _ ->
                SetupActivity.resetSetup(this)
                val intent = Intent(this, SetupActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About Powergen Solar")
            .setMessage("Call Recording System\nVersion 1.0\n\nRecords both cellular and WhatsApp calls automatically.\n\nDeveloped for Powergen Solar Pvt Ltd")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun setupRecyclerView() {
        recordingsAdapter = RecordingsAdapter(recordings) { recording ->
            // Play recording
            playRecording(recording)
        }
        
        binding.recyclerViewRecordings.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = recordingsAdapter
        }
    }

    private fun setupButtons() {
        binding.btnRefresh.setOnClickListener {
            loadRecordings()
        }
        
        binding.btnStartService.setOnClickListener {
            if (hasAllPermissions()) {
                startRecordingService()
            } else {
                Toast.makeText(this, "Please grant all permissions first", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.btnAccessibility.setOnClickListener {
            openAccessibilitySettings()
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.INTERNET
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            loadRecordings()
            startRecordingService()
        }
    }

    private fun hasAllPermissions(): Boolean {
        val permissions = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG
        )
        
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                loadRecordings()
                startRecordingService()
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Permissions Required")
                    .setMessage("This app needs all permissions to record calls. Please grant them in settings.")
                    .setPositiveButton("Open Settings") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = android.net.Uri.parse("package:$packageName")
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun loadRecordings() {
        recordings.clear()
        
        val recordingsDir = getExternalFilesDir(null)
        recordingsDir?.listFiles()?.filter { it.extension == "mp3" }?.forEach { file ->
            recordings.add(RecordingFile(file))
        }
        
        recordings.sortByDescending { it.file.lastModified() }
        recordingsAdapter.notifyDataSetChanged()
        
        binding.tvRecordingsCount.text = "Total Recordings: ${recordings.size}"
    }

    private fun playRecording(recording: RecordingFile) {
        val intent = Intent(this, AudioPlayerActivity::class.java)
        intent.putExtra("file_path", recording.file.absolutePath)
        intent.putExtra("file_name", recording.file.name)
        startActivity(intent)
    }

    private fun startRecordingService() {
        val intent = Intent(this, CallRecordingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "Recording service started", Toast.LENGTH_SHORT).show()
    }

    private fun openAccessibilitySettings() {
        AlertDialog.Builder(this)
            .setTitle("Enable WhatsApp Call Detection")
            .setMessage("To record WhatsApp calls, please enable the Powergen Solar accessibility service:\n\n" +
                    "1. Go to Accessibility Settings\n" +
                    "2. Find 'Powergen Solar'\n" +
                    "3. Turn it ON")
            .setPositiveButton("Open Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadRecordings()
    }
}

data class RecordingFile(val file: File)
