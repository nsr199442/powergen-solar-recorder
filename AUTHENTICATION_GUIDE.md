# Adding Authentication to Upload API

## Current Security Issue

**Problem:** The upload endpoint at `http://184.174.37.99:3500/api/upload-recording` is OPEN.
- ❌ No password required
- ❌ Anyone can upload if they know IP and port
- ❌ Not secure for production use

**Solution:** Add API token authentication.

---

## Step-by-Step Implementation

### Step 1: Choose Your Token

Generate a random, strong token. Use one of these:

**Option A - Generate Random Token:**
```bash
openssl rand -base64 32
# Example output: Powergen_XmK9pL2q8Yt5Zw3N4Rd7Fp2Hs6
```

**Option B - Create Your Own:**
```
Format: CompanyName_RandomLettersNumbers
Example: Powergen2025_SecureUpload_k9mXp2Lq8Yt5
```

**Save this token securely!**

---

### Step 2: Update VPS Server

**Connect to VPS:**
```bash
ssh root@184.174.37.99
```

**Edit server file:**
```bash
cd /var/www/call-recordings-api
nano server.js
```

**Add authentication code at the top (after `const PORT = 3500;`):**

```javascript
const express = require('express');
const multer = require('multer');
const cors = require('cors');
const path = require('path');
const fs = require('fs');

const app = express();
const PORT = 3500;

// ============================================
// ADD THIS SECTION - AUTHENTICATION
// ============================================

// Your secret API token - CHANGE THIS!
const API_TOKEN = "Powergen2025_SecureUpload_k9mXp2Lq8Yt5";

// Authentication middleware
app.use('/api/upload-recording', (req, res, next) => {
    const authHeader = req.headers['authorization'];
    
    if (!authHeader) {
        console.log('Upload attempt without authorization header');
        return res.status(401).json({ 
            success: false, 
            error: 'Authentication required' 
        });
    }
    
    // Expected format: "Bearer YOUR_TOKEN_HERE"
    const token = authHeader.replace('Bearer ', '');
    
    if (token !== API_TOKEN) {
        console.log('Upload attempt with invalid token:', token);
        return res.status(401).json({ 
            success: false, 
            error: 'Invalid authentication token' 
        });
    }
    
    // Token is valid, continue to upload endpoint
    next();
});

// ============================================
// END OF AUTHENTICATION SECTION
// ============================================

// Enable CORS for mobile app
app.use(cors());
app.use(express.json());

// ... rest of your existing code ...
```

**Save and exit:**
- Press `Ctrl + X`
- Press `Y`
- Press `Enter`

**Restart the service:**
```bash
systemctl restart call-recording-api
```

**Verify it's running:**
```bash
systemctl status call-recording-api
```

**Test authentication:**
```bash
# This should FAIL (no token)
curl http://localhost:3500/api/upload-recording
# Response: {"success":false,"error":"Authentication required"}

# This should SUCCEED
curl -H "Authorization: Bearer Powergen2025_SecureUpload_k9mXp2Lq8Yt5" \
     http://localhost:3500/api/upload-recording
# Response: {"success":false,"error":"No file uploaded"} ← This is correct! It means auth passed.
```

---

### Step 3: Update Android App

**Open your project in Android Studio**

**Edit `VPSUploader.kt`:**

Find this section:
```kotlin
val request = Request.Builder()
    .url(uploadUrl)
    .post(requestBody)
    .build()
```

**Replace it with:**
```kotlin
val request = Request.Builder()
    .url(uploadUrl)
    .post(requestBody)
    // ADD AUTHENTICATION HEADER
    .header("Authorization", "Bearer Powergen2025_SecureUpload_k9mXp2Lq8Yt5")
    .build()
```

**Complete updated VPSUploader.kt:**
```kotlin
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
        // Your API token - MUST MATCH the token on VPS server!
        private const val API_TOKEN = "Powergen2025_SecureUpload_k9mXp2Lq8Yt5"
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

        val serverUrl = SetupActivity.getServerUrl(context)
        
        if (serverUrl.isEmpty()) {
            Log.e(TAG, "Server URL not configured")
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
                // AUTHENTICATION HEADER
                .header("Authorization", "Bearer $API_TOKEN")
                .build()

            val response = client.newCall(request).execute()

            return if (response.isSuccessful) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Upload successful: $responseBody")
                true
            } else {
                Log.e(TAG, "Upload failed: ${response.code} - ${response.message}")
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
```

---

### Step 4: Rebuild and Deploy

**Rebuild APK:**
1. In Android Studio: `Build` → `Clean Project`
2. Then: `Build` → `Build APK`
3. Get new APK from: `app/build/outputs/apk/debug/app-debug.apk`

**Deploy to phones:**
- Uninstall old version (if installed)
- Install new APK
- Test a call
- Verify upload works

---

## Testing

### Test 1: Unauthorized Access (Should Fail)

```bash
curl -X POST http://184.174.37.99:3500/api/upload-recording
```

**Expected response:**
```json
{"success":false,"error":"Authentication required"}
```
✅ Good! Unauthorized access blocked.

### Test 2: Wrong Token (Should Fail)

```bash
curl -X POST http://184.174.37.99:3500/api/upload-recording \
     -H "Authorization: Bearer WrongToken123"
```

**Expected response:**
```json
{"success":false,"error":"Invalid authentication token"}
```
✅ Good! Invalid token rejected.

### Test 3: Correct Token (Should Work)

```bash
curl -X POST http://184.174.37.99:3500/api/upload-recording \
     -H "Authorization: Bearer Powergen2025_SecureUpload_k9mXp2Lq8Yt5" \
     -F "file=@test.mp3" \
     -F "phone_number=+94761234567" \
     -F "call_type=cellular" \
     -F "direction=outgoing"
```

**Expected response:**
```json
{"success":true,"message":"Recording uploaded successfully"}
```
✅ Good! Valid token works.

### Test 4: App Upload

- Make a phone call
- End the call
- Check VPS logs:

```bash
journalctl -u call-recording-api -f
```

**You should see:**
```
Recording uploaded: {...}
```

**Check files:**
```bash
ls -lh /var/www/call-recordings/cellular/outgoing/
```

✅ MP3 file should be there!

---

## Security Best Practices

### ✅ DO:
- Use a long, random token (32+ characters)
- Keep token secret (only in VPS and APK)
- Change token periodically (every 6 months)
- Monitor upload logs for suspicious activity
- Use HTTPS in production (requires SSL certificate)

### ❌ DON'T:
- Share token publicly
- Use simple tokens like "password123"
- Store token in plain text files
- Send token via unencrypted channels
- Forget to update token in both VPS and app

---

## Changing the Token Later

If you need to change the token:

**Step 1: Generate new token**
```bash
openssl rand -base64 32
```

**Step 2: Update VPS**
```bash
nano /var/www/call-recordings-api/server.js
# Change API_TOKEN value
systemctl restart call-recording-api
```

**Step 3: Update App**
- Edit `VPSUploader.kt`
- Change `API_TOKEN` value
- Rebuild APK
- Redeploy to all phones

---

## What This Protects Against

### ✅ Protected:
- Random people uploading files to your server
- Unauthorized access to upload endpoint
- Spam uploads
- Malicious file uploads

### ⚠️ Still Vulnerable To:
- Someone extracting token from APK (advanced)
- Man-in-the-middle attacks (use HTTPS to prevent)
- Lost/stolen phone (they can upload with that phone)

### 🔒 For Maximum Security:
- Add HTTPS (SSL certificate)
- Add device registration
- Add file size limits
- Add rate limiting
- Use VPN for uploads

---

## Token Storage Security

**Where the token is stored:**

1. **VPS Server:** `/var/www/call-recordings-api/server.js`
   - Only accessible via SSH (password protected)
   - ✅ Secure

2. **Android APK:** Inside compiled code
   - Can be extracted with tools (requires technical skill)
   - ⚠️ Moderately secure

3. **Not stored:**
   - No token in user-accessible files
   - No token in shared preferences
   - No token visible in app UI

**Risk Assessment:**
- Low risk for typical call center use
- Token extraction requires:
  - APK file
  - Decompilation tools
  - Technical knowledge
- Most attackers won't bother

**If very paranoid:**
- Use certificate pinning
- Add device registration
- Implement key rotation
- Use hardware security module

---

## Summary

**What we accomplished:**
✅ Upload endpoint now requires authentication  
✅ API token protects against unauthorized uploads  
✅ Simple to implement and maintain  
✅ Secure enough for production use  

**Security level:** 🔒🔒🔒 Good  
**Complexity:** ⭐ Low  
**Time to implement:** 15-20 minutes  

**Next steps:**
1. Choose your token
2. Update VPS server
3. Update Android app
4. Rebuild and deploy
5. Test thoroughly

---

**Your upload endpoint is now protected!** 🔒
