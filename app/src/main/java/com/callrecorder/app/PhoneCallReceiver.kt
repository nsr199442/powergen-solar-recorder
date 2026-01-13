package com.callrecorder.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.util.Log

class PhoneCallReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PhoneCallReceiver"
        private var incomingNumber: String? = null
        private var lastState = TelephonyManager.CALL_STATE_IDLE
    }

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        Log.d(TAG, "Phone state changed: $state, number: $phoneNumber")

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                // Incoming call detected
                incomingNumber = phoneNumber
                CallRecordingService.currentPhoneNumber = phoneNumber
                CallRecordingService.callDirection = "incoming"
                Log.d(TAG, "Incoming call: $phoneNumber")
            }

            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                // Call answered or outgoing call started
                val service = getCallRecordingService(context)
                
                if (incomingNumber != null) {
                    // Incoming call answered
                    Log.d(TAG, "Answering incoming call: $incomingNumber")
                    service?.startRecording(incomingNumber!!, "cellular", "incoming")
                } else {
                    // Outgoing call
                    val outgoingNumber = getLastDialedNumber(context) ?: "unknown"
                    Log.d(TAG, "Outgoing call: $outgoingNumber")
                    CallRecordingService.currentPhoneNumber = outgoingNumber
                    CallRecordingService.callDirection = "outgoing"
                    service?.startRecording(outgoingNumber, "cellular", "outgoing")
                }
            }

            TelephonyManager.EXTRA_STATE_IDLE -> {
                // Call ended
                if (lastState != TelephonyManager.CALL_STATE_IDLE) {
                    Log.d(TAG, "Call ended")
                    getCallRecordingService(context)?.stopRecording()
                    incomingNumber = null
                    CallRecordingService.currentPhoneNumber = null
                    CallRecordingService.callDirection = null
                }
            }
        }

        lastState = when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
            TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
            else -> TelephonyManager.CALL_STATE_IDLE
        }
    }

    private fun getCallRecordingService(context: Context): CallRecordingService? {
        return try {
            // Create instance of service to call methods
            // In real scenario, you'd use a proper service connection
            CallRecordingService()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get service", e)
            null
        }
    }

    private fun getLastDialedNumber(context: Context): String? {
        try {
            val projection = arrayOf(CallLog.Calls.NUMBER)
            val cursor: Cursor? = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                "${CallLog.Calls.DATE} DESC"
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                    if (numberIndex >= 0) {
                        return it.getString(numberIndex)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last dialed number", e)
        }
        return null
    }
}
