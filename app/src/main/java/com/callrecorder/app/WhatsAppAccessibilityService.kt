package com.callrecorder.app

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class WhatsAppAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "WhatsAppAccessibility"
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private var isWhatsAppCallActive = false
        private var currentContactName: String? = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.packageName != WHATSAPP_PACKAGE) return

        try {
            val rootNode = rootInActiveWindow ?: return
            
            // Detect if WhatsApp call screen is active
            val isCallActive = detectWhatsAppCall(rootNode)
            
            if (isCallActive && !isWhatsAppCallActive) {
                // Call started
                isWhatsAppCallActive = true
                val contactInfo = extractContactInfo(rootNode)
                val direction = detectCallDirection(rootNode)
                
                Log.d(TAG, "WhatsApp call started: $contactInfo, direction: $direction")
                
                // Start recording
                val service = CallRecordingService()
                service.startRecording(
                    contactInfo ?: "unknown",
                    "whatsapp",
                    direction
                )
                
            } else if (!isCallActive && isWhatsAppCallActive) {
                // Call ended
                isWhatsAppCallActive = false
                Log.d(TAG, "WhatsApp call ended")
                
                // Stop recording
                val service = CallRecordingService()
                service.stopRecording()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing accessibility event", e)
        }
    }

    private fun detectWhatsAppCall(node: AccessibilityNodeInfo): Boolean {
        // Look for WhatsApp call indicators
        return findNodeByText(node, "End call") != null ||
               findNodeByText(node, "Mute") != null ||
               findNodeByText(node, "Speaker") != null ||
               findNodeByText(node, "Video call") != null ||
               findNodeByText(node, "Voice call") != null
    }

    private fun detectCallDirection(node: AccessibilityNodeInfo): String {
        return when {
            findNodeByText(node, "Calling...") != null -> "outgoing"
            findNodeByText(node, "Ringing...") != null -> "outgoing"
            findNodeByText(node, "Incoming call") != null -> "incoming"
            findNodeByText(node, "Answer") != null -> "incoming"
            findNodeByText(node, "Decline") != null -> "incoming"
            else -> "unknown"
        }
    }

    private fun extractContactInfo(node: AccessibilityNodeInfo): String? {
        // Try to find contact name or number
        val textNodes = mutableListOf<String>()
        collectAllTexts(node, textNodes)
        
        // Filter out common UI texts
        val filtered = textNodes.filter { text ->
            text.isNotEmpty() && 
            !text.contains("End call", ignoreCase = true) &&
            !text.contains("Mute", ignoreCase = true) &&
            !text.contains("Speaker", ignoreCase = true) &&
            !text.contains("Video", ignoreCase = true) &&
            text.length > 2
        }
        
        return filtered.firstOrNull()
    }

    private fun findNodeByText(node: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
        if (node == null) return null
        
        if (node.text?.contains(text, ignoreCase = true) == true) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val result = findNodeByText(child, text)
            if (result != null) return result
        }
        
        return null
    }

    private fun collectAllTexts(node: AccessibilityNodeInfo?, texts: MutableList<String>) {
        if (node == null) return
        
        node.text?.toString()?.let { text ->
            if (text.isNotBlank()) texts.add(text)
        }
        
        for (i in 0 until node.childCount) {
            collectAllTexts(node.getChild(i), texts)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "WhatsApp accessibility service connected")
    }
}
