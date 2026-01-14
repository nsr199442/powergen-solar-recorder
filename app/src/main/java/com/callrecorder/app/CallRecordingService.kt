package com.callrecorder.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CallRecordingService : Service() {

    private var mediaRecorder: MediaRecorder? = null
    private var currentFilePath: String? = null
    private var currentCallMetadata: CallMetadata? = null
    private var recordingStartTime: Long = 0
    
    companion object {
        private const val TAG = "CallRecordingService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "call_recording_channel"
        
        var isRecording = false
        var currentPhoneNumber: String? = null
        var callDirection: String? = null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Call Recorder Active"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    fun startRecording(phoneNumber: String, callType: String, direction: String) {
        if (isRecording) {
            Log.w(TAG, "Already recording, stopping previous recording")
            stopRecording()
        }

        try {
            val metadata = CallMetadata(
                phoneNumber = phoneNumber,
                callType = callType,
                direction = direction,
                timestamp = getCurrentTimestamp()
            )

            val fileName = generateFileName(metadata)
            val filePath = "${getExternalFilesDir(null)}/$fileName"

            currentCallMetadata = metadata
            currentFilePath = filePath
            recordingStartTime = System.currentTimeMillis()

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(
                    if (callType == "cellular")
                        MediaRecorder.AudioSource.VOICE_CALL
                    else
                        MediaRecorder.AudioSource.VOICE_COMMUNICATION
                )
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(filePath)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                
                prepare()
                start()
                
                isRecording = true
                Log.d(TAG, "Recording started: $fileName")
                
                updateNotification("Recording: $phoneNumber")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            isRecording = false
            stopRecording()
        }
    }

    fun stopRecording() {
        if (!isRecording || mediaRecorder == null) {
            return
        }

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            
            val duration = (System.currentTimeMillis() - recordingStartTime) / 1000
            currentCallMetadata?.duration = duration
            
            Log.d(TAG, "Recording stopped. Duration: ${duration}s")
            
            // Upload to VPS
            currentFilePath?.let { path ->
                currentCallMetadata?.let { metadata ->
                    uploadToVPS(path, metadata)
                }
            }
            
            isRecording = false
            updateNotification("Call Recorder Active")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
        } finally {
            mediaRecorder = null
            currentFilePath = null
            currentCallMetadata = null
        }
    }

    private fun generateFileName(metadata: CallMetadata): String {
        val cleanNumber = metadata.phoneNumber.replace(Regex("[^0-9+]"), "")
        val agentName = SetupActivity.getAgentName(this).replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "")
        return "${agentName}_${cleanNumber}_${metadata.callType}_${metadata.direction}_${metadata.timestamp}.mp3"
    }

    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
    }

    private fun uploadToVPS(filePath: String, metadata: CallMetadata) {
        Thread {
            try {
                val uploader = VPSUploader(this)
                val success = uploader.uploadRecording(filePath, metadata)
                
                if (success) {
                    Log.d(TAG, "Upload successful: ${File(filePath).name}")
                    // Optionally delete file after successful upload
                    // File(filePath).delete()
                } else {
                    Log.e(TAG, "Upload failed: ${File(filePath).name}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Upload error", e)
            }
        }.start()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Call Recording Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the call recording service running"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Call Recorder")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(text))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }
}

data class CallMetadata(
    val phoneNumber: String,
    val callType: String,
    val direction: String,
    val timestamp: String,
    var duration: Long = 0
)
