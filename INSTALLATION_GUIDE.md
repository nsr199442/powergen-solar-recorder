# QUICK INSTALLATION GUIDE

## For Call Center Managers

### Step 1: Build the APK

1. Install Android Studio: https://developer.android.com/studio
2. Open the `CallRecorder` folder in Android Studio
3. Wait for Gradle sync to complete (5-10 minutes)
4. Click: `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
5. Find APK at: `app/build/outputs/apk/debug/app-debug.apk`

### Step 2: Install on Each Phone

1. Transfer `app-debug.apk` to the phone (USB/WhatsApp/Drive)
2. Open the APK file on the phone
3. Allow "Install from Unknown Sources" if prompted
4. Install

### Step 3: Setup Each Phone (Do Once Per Phone)

**Grant Permissions:**
1. Open the Call Recorder app
2. Allow ALL permissions when prompted:
   - ✅ Microphone
   - ✅ Phone
   - ✅ Call Log
   - ✅ Storage
   - ✅ Notifications

**Enable WhatsApp Recording:**
1. Tap "WhatsApp Setup" button in the app
2. In Accessibility Settings, find "Call Recorder"
3. Turn it ON
4. Press Back to return to app

**Start Service:**
1. Tap "Start Service" button
2. You should see notification: "Call Recorder Active"

**Prevent Battery Optimization:**
1. Go to: Settings → Apps → Call Recorder → Battery
2. Select "Unrestricted"

### Step 4: Verify It's Working

**Test Regular Call:**
1. Make or receive a phone call
2. After call ends, tap "Refresh Recordings" in app
3. You should see the recording listed

**Test WhatsApp Call:**
1. Make or receive a WhatsApp call
2. After call ends, tap "Refresh Recordings" in app
3. You should see the recording listed

**Check Upload:**
1. SSH to VPS: `ssh root@184.174.37.99`
2. List recordings: `ls -lh /var/www/call-recordings/cellular/`
3. You should see the MP3 files

### Troubleshooting

**Service Keeps Stopping:**
- Settings → Apps → Call Recorder → Battery → Unrestricted
- Lock the app in Recent Apps (tap lock icon)

**No Recordings Appearing:**
- Make sure all permissions are granted
- Restart the app and tap "Start Service" again
- Check if accessibility service is enabled for WhatsApp recording

**Recordings Not Uploading:**
- Check internet connection
- Test VPS: `curl http://184.174.37.99:3500/health`
- Check VPS service: `systemctl status call-recording-api`

### Daily Monitoring

**Check VPS Logs:**
```bash
journalctl -u call-recording-api -f
```

**Check Recordings Count:**
```bash
find /var/www/call-recordings/ -name "*.mp3" | wc -l
```

**Check Disk Space:**
```bash
df -h /var/www/
```

### Contact

If you encounter issues:
1. Check the full README.md file
2. Review Android logs: `adb logcat | grep CallRecorder`
3. Review VPS logs: `journalctl -u call-recording-api -f`

## Important Reminders

⚠️ **Legal**: Inform callers that calls are being recorded  
⚠️ **Privacy**: Recordings contain sensitive information - keep secure  
⚠️ **Storage**: Monitor VPS disk space regularly (~ 450 MB/day for 100 calls)  
