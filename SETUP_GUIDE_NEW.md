# Powergen Solar - Call Recording System

## What's New in This Version

✅ **App Name**: Changed to "Powergen Solar"  
✅ **Server IP Configuration**: Enter your VPS server IP on first launch  
✅ **Security**: Same APK works for multiple clients with different servers  
✅ **Settings Menu**: Change server IP anytime from settings  

---

## First-Time Setup Screen

When you first open the app, you'll see:

### Server Configuration Screen

```
┌─────────────────────────────────┐
│     Welcome to Powergen Solar   │
│     Call Recording System       │
│                                 │
│  Server Configuration           │
│                                 │
│  Server IP Address:             │
│  ┌───────────────────────────┐  │
│  │ 184.174.37.99            │  │
│  └───────────────────────────┘  │
│                                 │
│  Server Port:                   │
│  ┌───────────────────────────┐  │
│  │ 3500                     │  │
│  └───────────────────────────┘  │
│                                 │
│  [ Test Connection ]            │
│                                 │
│  [ Save & Continue ]            │
└─────────────────────────────────┘
```

### Steps:
1. **Enter Server IP**: Type your VPS IP address (e.g., `184.174.37.99`)
2. **Enter Port**: Usually `3500` (pre-filled)
3. **Test Connection**: Optional but recommended - verifies server is reachable
4. **Save & Continue**: Saves settings and continues to main app

---

## Installation Instructions

### For Call Center Managers

**Step 1: Install APK**
1. Transfer `Powergen-Solar.apk` to each phone
2. Install the APK
3. Allow "Install from Unknown Sources"

**Step 2: Configure Server (First Launch Only)**
1. Open the app
2. You'll see "Server Configuration" screen
3. Enter your VPS IP: `184.174.37.99`
4. Port is already set to: `3500`
5. Tap "Test Connection" (optional but recommended)
6. If test succeeds, you'll see: ✓ Connection successful!
7. Tap "Save & Continue"

**Step 3: Grant Permissions**
(Same as before - allow all permissions)

**Step 4: Enable WhatsApp Recording**
(Same as before - enable accessibility service)

**Step 5: Start Service**
Tap "Start Service" button

---

## Changing Server Settings

If you need to change the server IP later:

1. Open app
2. Tap **menu icon** (3 dots) in top right
3. Select **"Server Settings"**
4. Tap **"Change"**
5. Enter new server IP
6. Save

---

## Security Benefits

### Before (Hardcoded IP):
❌ IP address visible in APK code  
❌ Need different APK for each client  
❌ Can't change server without rebuilding  

### Now (Dynamic IP):
✅ No IP address in APK code  
✅ Same APK for all clients  
✅ Easy to change server anytime  
✅ Test connection before saving  

---

## For Multiple Clients

You can now use **the same APK** for different clients:

**Client A (Powergen Solar):**
- Server IP: `184.174.37.99`
- Port: `3500`

**Client B (Another Company):**
- Server IP: `192.168.1.100`
- Port: `8080`

**Same APK, different configurations!** 🎉

---

## Installation Flow

```
Install APK
    ↓
First Launch
    ↓
Server Configuration Screen
    ↓
Enter IP Address → Test → Save
    ↓
Grant Permissions
    ↓
Enable Accessibility (for WhatsApp)
    ↓
Start Service
    ↓
✅ Ready to Record!
```

---

## Troubleshooting Server Configuration

### "Connection Failed" Error

**Possible Causes:**
1. **Wrong IP Address**: Double-check the IP
2. **Wrong Port**: Make sure it's `3500`
3. **Server Not Running**: Check VPS status
4. **Firewall**: Port 3500 must be open
5. **No Internet**: Phone needs internet connection

**Check Server Status:**
```bash
ssh root@YOUR_VPS_IP
systemctl status call-recording-api
curl http://localhost:3500/health
```

### Can't Save Without Testing

**Note**: You can save without testing, but testing is recommended to verify the server is reachable before proceeding.

### Forgot Server IP

Go to: **Menu (3 dots) → Server Settings** to view current configuration

---

## For IT Administrators

### Pre-Configure Multiple Phones

**Option 1: Manual Entry** (Recommended)
- Let each phone operator enter the IP during first launch
- More secure as IP is not stored anywhere before installation

**Option 2: Automation** (Advanced)
If you want to pre-configure many phones, you can:
1. Install and configure one phone
2. Copy SharedPreferences file: `/data/data/com.callrecorder.app/shared_prefs/PowergenSolarPrefs.xml`
3. Push to other phones using ADB (requires USB debugging)

```bash
adb push PowergenSolarPrefs.xml /data/data/com.callrecorder.app/shared_prefs/
```

---

## Testing the Setup

### Test 1: Connection Test
In setup screen, tap "Test Connection"
- ✅ Should show: "Connection successful!"
- ❌ If fails: Check server status and IP

### Test 2: Make a Call
1. Make a regular phone call
2. End the call
3. Tap "Refresh Recordings" in main app
4. Recording should appear in list

### Test 3: Check Upload
```bash
ssh root@YOUR_VPS_IP
ls /var/www/call-recordings/cellular/outgoing/
# You should see your test recording
```

---

## App Screens

### 1. Setup Screen (First Launch)
- Server IP input
- Port input
- Test connection button
- Save & continue button

### 2. Main Screen (After Setup)
- Start Service button
- WhatsApp Setup button
- Refresh button
- Recordings list
- Menu (3 dots) with:
  - Server Settings
  - About

### 3. Settings Menu
- View current server
- Change server option
- About information

---

## Important Notes

1. **First Launch Only**: Setup screen appears only on first launch
2. **Can't Skip**: Must configure server before using app
3. **Can Change Later**: Server settings can be changed anytime
4. **Test Connection**: Optional but recommended
5. **Internet Required**: Need internet for both testing and uploads

---

## Summary

**What Changed:**
- ✅ App name: "Powergen Solar"
- ✅ Server IP: User-configurable
- ✅ Setup screen: Shows on first launch
- ✅ Settings menu: Change server anytime
- ✅ Security: No hardcoded IP in code

**What Stayed the Same:**
- ✅ Records cellular + WhatsApp calls
- ✅ Auto-detects direction (incoming/outgoing)
- ✅ Auto-uploads to VPS
- ✅ List view with playback
- ✅ No delete option
- ✅ Background service

---

**Your call recording system is now more secure and flexible!** 🔒🎉
