package com.callrecorder.app

import android.content.Context
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class VPSUploader(private val context: Context) {

    companion object {
        private const val TAG = "VPSUploader"
        private const val TIMEOUT_SECONDS = 60L
        
        // ============================================
        // AUTHENTICATION TOKEN - MUST MATCH VPS SERVER!
        // Token: Powergen2025_SecureUpload_k9mXp2Lq8Yt5Zw3N
        // ============================================
        private const val API_TOKEN = "Powergen2025_SecureUpload_k9mXp2Lq8Yt5Zw3N"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    fun uploadRecording(filePath: String, metadata: CallMetadata): Boolean {
        val file = File(filePath)
        
        if (!file.exists()) {
            Log.e(TAG, "File not found: $filePath")
            return false
        }

        // Get server URL from settings
        val serverUrl = SetupActivity.getServerUrl(context)
        
        if (serverUrl.isEmpty()) {
            Log.e(TAG, "Server URL not configured. Please complete setup first.")
            return false
        }

        val uploadUrl = "$serverUrl/api/upload-recording"
        
        Log.d(TAG, "Uploading to: $uploadUrl")
        Log.d(TAG, "File: ${file.name} (${file.length() / 1024}KB)")

        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    file.name,
                    file.asRequestBody("audio/mpeg".toMediaType())
                )
                .addFormDataPart("phone_number", metadata.phoneNumber)
                .addFormDataPart("call_type", metadata.callType)
                .addFormDataPart("direction", metadata.direction)
                .addFormDataPart("duration", metadata.duration.toString())
                .addFormDataPart("timestamp", metadata.timestamp)
                .build()

            val request = Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                // ============================================
                // AUTHENTICATION HEADER - SENDS TOKEN TO SERVER
                // ============================================
                .header("Authorization", "Bearer $API_TOKEN")
                .build()

            val response = client.newCall(request).execute()

            return if (response.isSuccessful) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Upload successful: $responseBody")
                true
            } else {
                Log.e(TAG, "Upload failed: ${response.code} - ${response.message}")
                if (response.code == 401) {
                    Log.e(TAG, "Authentication failed! Check if token matches VPS server.")
                }
                false
            }

        } catch (e: IOException) {
            Log.e(TAG, "Upload error: ${e.message}", e)
            return false
        }
    }

    fun uploadRecordingAsync(filePath: String, metadata: CallMetadata, callback: (Boolean) -> Unit) {
        Thread {
            val success = uploadRecording(filePath, metadata)
            callback(success)
        }.start()
    }
}
