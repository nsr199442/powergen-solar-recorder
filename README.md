# Call Recorder App for Call Centers

Complete Android application for recording both regular cellular calls and WhatsApp calls, with automatic upload to VPS.

## Features

✅ **Records Regular Phone Calls** (incoming and outgoing)  
✅ **Records WhatsApp Calls** (incoming and outgoing)  
✅ **Automatic Call Direction Detection**  
✅ **Structured File Naming**: `+94761234567_cellular_incoming_2025-01-13_14-30-45.mp3`  
✅ **Auto-Upload to VPS**: Uploads to `http://184.174.37.99:3500/api/upload-recording`  
✅ **List View with Playback**: View all recordings with play functionality  
✅ **No Delete Option**: Recordings cannot be deleted from the app  
✅ **Background Service**: Runs continuously in the background  

## File Naming Format

```
{phone_number}_{call_type}_{direction}_{date}_{time}.mp3

Examples:
+94761234567_cellular_incoming_2025-01-13_14-30-45.mp3
+94771234567_cellular_outgoing_2025-01-13_15-20-10.mp3
+94761234567_whatsapp_incoming_2025-01-13_16-10-30.mp3
+94771234567_whatsapp_outgoing_2025-01-13_17-05-22.mp3
```

## VPS Setup (Already Done)

✅ Server running on `184.174.37.99:3500`  
✅ Upload endpoint: `/api/upload-recording`  
✅ Folder structure created  
✅ Automatic logging for n8n integration  

## Building the APK

### Option 1: Using Android Studio (Recommended)

1. **Install Android Studio**
   - Download from: https://developer.android.com/studio
   - Install with default settings

2. **Open Project**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the `CallRecorder` folder

3. **Wait for Gradle Sync**
   - Android Studio will automatically download dependencies
   - This may take 5-10 minutes on first run

4. **Build APK**
   - Click `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
   - APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

5. **Transfer APK to Phone**
   - Connect phone via USB or use Google Drive/WhatsApp to transfer
   - Install the APK on all call center phones

### Option 2: Using Command Line

```bash
cd CallRecorder
./gradlew assembleDebug

# APK location: app/build/outputs/apk/debug/app-debug.apk
```

## Installation on Phones

### Step 1: Install APK

1. Transfer `app-debug.apk` to phone
2. Open the APK file
3. Allow "Install from Unknown Sources" if prompted
4. Install the app

### Step 2: Grant Permissions

When you open the app for the first time, grant these permissions:

- ✅ **Microphone** - Required for recording audio
- ✅ **Phone** - Required to detect phone calls
- ✅ **Call Log** - Required to get phone numbers
- ✅ **Storage** - Required to save recordings
- ✅ **Notifications** - For service notifications

### Step 3: Enable WhatsApp Call Recording

1. In the app, tap **"WhatsApp Setup"** button
2. This will open Accessibility Settings
3. Find **"Call Recorder"** in the list
4. Turn it **ON**
5. Confirm the permission

### Step 4: Start the Service

1. Tap **"Start Service"** in the app
2. The service will now run in the background
3. You should see a persistent notification: "Call Recorder Active"

## Using the App

### Main Screen

- **Start Service Button**: Starts the background recording service
- **WhatsApp Setup Button**: Opens accessibility settings
- **Refresh Recordings Button**: Reloads the list of recordings
- **Total Recordings Counter**: Shows number of recorded calls
- **Recordings List**: Displays all recorded calls with details

### Playing Recordings

1. Tap the **Play button** (▶) on any recording
2. Audio player screen will open
3. Use Play/Pause and Stop buttons to control playback
4. Tap "Close" to return to main screen

### What Gets Recorded

**Automatically Recorded:**
- All incoming cellular calls (when answered)
- All outgoing cellular calls
- All incoming WhatsApp calls (when answered)
- All outgoing WhatsApp calls

**File Information Stored:**
- Phone number or contact name
- Call type (cellular or whatsapp)
- Direction (incoming or outgoing)
- Date and time
- Duration
- File size

## Troubleshooting

### Regular Calls Not Recording

1. Check if microphone permission is granted
2. Check if phone permission is granted
3. Restart the app and tap "Start Service" again
4. On some phones, recording cellular calls may not work due to manufacturer restrictions

### WhatsApp Calls Not Recording

1. Make sure Accessibility Service is enabled:
   - Settings → Accessibility → Call Recorder → ON
2. Make sure WhatsApp permissions are granted
3. Restart the phone after enabling accessibility service

### Recordings Not Uploading

1. Check internet connection (WiFi or mobile data)
2. Make sure the VPS server is running:
   ```bash
   systemctl status call-recording-api
   ```
3. Test the VPS endpoint manually:
   ```bash
   curl http://184.174.37.99:3500/health
   ```
4. Check app logs for upload errors

### Service Stops Running

Some Android phones aggressively kill background services to save battery. To prevent this:

1. **Disable Battery Optimization**:
   - Settings → Apps → Call Recorder → Battery → Unrestricted

2. **Allow Background Activity**:
   - Settings → Apps → Call Recorder → Mobile data → Allow background data usage

3. **Lock App in Recent Apps**:
   - Open Recent Apps
   - Find Call Recorder
   - Tap the lock icon to prevent it from being killed

## VPS File Structure

Recordings are organized on the VPS as follows:

```
/var/www/call-recordings/
├── cellular/
│   ├── incoming/
│   │   ├── +94761234567_cellular_incoming_2025-01-13_14-30-45.mp3
│   │   └── +94771234567_cellular_incoming_2025-01-13_15-20-10.mp3
│   └── outgoing/
│       ├── +94761234567_cellular_outgoing_2025-01-13_16-10-30.mp3
│       └── +94771234567_cellular_outgoing_2025-01-13_17-05-22.mp3
└── whatsapp/
    ├── incoming/
    │   ├── +94761234567_whatsapp_incoming_2025-01-13_18-15-20.mp3
    │   └── +94771234567_whatsapp_incoming_2025-01-13_19-25-35.mp3
    └── outgoing/
        ├── +94761234567_whatsapp_outgoing_2025-01-13_20-30-40.mp3
        └── +94771234567_whatsapp_outgoing_2025-01-13_21-40-50.mp3
```

## Upload Metadata

Each upload includes:

```json
{
  "filename": "+94761234567_cellular_incoming_2025-01-13_14-30-45.mp3",
  "phone_number": "+94761234567",
  "call_type": "cellular",
  "direction": "incoming",
  "duration": 180,
  "timestamp": "2025-01-13_14-30-45",
  "file_path": "/var/www/call-recordings/cellular/incoming/+94761234567_cellular_incoming_2025-01-13_14-30-45.mp3",
  "file_size": 2457600,
  "uploaded_at": "2025-01-13T14:33:45.123Z"
}
```

## n8n Integration

All uploads are logged to: `/var/www/call-recordings/upload-log.json`

You can create an n8n workflow to:
1. Monitor this log file
2. Extract metadata
3. Update your Google Sheet with recording information

## Important Notes

### Legal Compliance

⚠️ **Sri Lanka Law**: Inform all callers that calls are being recorded. Add a voice announcement at the beginning of each call.

### Privacy

- Recordings contain sensitive customer information
- Ensure VPS and phones are secure
- Use strong passwords
- Consider encrypting recordings in transit

### Storage Planning

- Average call: ~1-2 MB per minute
- 100 calls/day × 3 minutes avg × 1.5 MB = ~450 MB/day
- Monthly storage needed: ~13.5 GB
- Plan VPS storage accordingly

### Performance

- Recordings use phone storage temporarily before upload
- Clean up old local recordings periodically
- Monitor VPS disk space regularly

## Support

For issues or questions:
1. Check the Troubleshooting section
2. Review Android logs: `adb logcat | grep CallRecorder`
3. Check VPS logs: `journalctl -u call-recording-api -f`

## Version History

**v1.0** (2025-01-13)
- Initial release
- Cellular call recording
- WhatsApp call recording
- Auto-upload to VPS
- Playback functionality
