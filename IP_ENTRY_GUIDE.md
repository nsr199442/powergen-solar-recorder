# How to Enter Server IP - Simple Guide

## The Setup Screen

When you first open the Powergen Solar app, you'll see this:

```
╔═══════════════════════════════════╗
║                                   ║
║    Welcome to Powergen Solar      ║
║    Call Recording System          ║
║                                   ║
╠═══════════════════════════════════╣
║                                   ║
║  Server IP Address:               ║
║  ┌─────────────────────────────┐  ║
║  │ Type IP here                │  ║
║  └─────────────────────────────┘  ║
║                                   ║
║  Server Port:                     ║
║  ┌─────────────────────────────┐  ║
║  │ 3500                        │  ║ (Already filled)
║  └─────────────────────────────┘  ║
║                                   ║
║   [ Test Connection ]             ║
║                                   ║
║   [ Save & Continue ]             ║
║                                   ║
╚═══════════════════════════════════╝
```

---

## Step-by-Step: What to Type

### Step 1: Server IP Address Field
**Click the first empty box**

### Step 2: Type ONLY the IP
```
Type this: 184.174.37.99
```

**✅ Correct - Just the numbers and dots:**
- `184.174.37.99`

**❌ Wrong - Don't include these:**
- ~~`http://184.174.37.99`~~
- ~~`184.174.37.99:3500`~~
- ~~`http://184.174.37.99:3500`~~
- ~~`https://184.174.37.99`~~

### Step 3: Port Field
**The port field already says `3500`**
- Leave it as is!
- Or if empty, just type: `3500`

---

## Visual Example

### ✅ CORRECT:

```
Server IP Address:
┌─────────────────────────────┐
│ 184.174.37.99              │  ← Just IP, nothing else!
└─────────────────────────────┘

Server Port:
┌─────────────────────────────┐
│ 3500                        │  ← Just port number!
└─────────────────────────────┘
```

### ❌ WRONG:

```
Server IP Address:
┌─────────────────────────────┐
│ http://184.174.37.99:3500  │  ← NO! Too much!
└─────────────────────────────┘
```

```
Server IP Address:
┌─────────────────────────────┐
│ 184.174.37.99:3500         │  ← NO! Don't include port!
└─────────────────────────────┘
```

---

## After Entering

### Step 4: Test Connection (Optional but Recommended)
1. Tap **"Test Connection"** button
2. Wait 2-3 seconds
3. You'll see one of these:

**✅ Success:**
```
✓ Connection successful!
```
→ Good! Your phone can reach the server.

**❌ Failed:**
```
✗ Connection failed: [error message]
```
→ Check the IP address or ask IT support.

### Step 5: Save
1. Tap **"Save & Continue"**
2. The app will save your settings
3. You'll go to the main screen

---

## What Each Part Means

### Server IP Address
- This is WHERE your VPS server is located
- Like a phone number for computers
- Format: `XXX.XXX.XXX.XXX` (4 numbers separated by dots)
- Example: `184.174.37.99`

### Server Port
- This is WHICH service on the server to connect to
- Like an extension number
- Usually: `3500`
- Just a number, no dots

### http:// or https://
- **DON'T type this!**
- The app adds it automatically
- You only type the IP and port

---

## Common Mistakes

### Mistake 1: Including "http://"
```
❌ http://184.174.37.99
✅ 184.174.37.99
```

### Mistake 2: Including port with IP
```
❌ 184.174.37.99:3500
✅ 184.174.37.99  (in IP field)
✅ 3500           (in Port field)
```

### Mistake 3: Using full URL
```
❌ http://184.174.37.99:3500/api/upload
✅ 184.174.37.99  (IP)
✅ 3500           (Port)
```

### Mistake 4: Spaces
```
❌ 184. 174. 37. 99
✅ 184.174.37.99
```

---

## Quick Reference Card

Print this and give to each agent:

```
┌───────────────────────────────────┐
│ POWERGEN SOLAR - SERVER SETUP     │
├───────────────────────────────────┤
│                                   │
│ When app asks for server info:   │
│                                   │
│ Server IP:  184.174.37.99        │
│                                   │
│ Port:       3500                  │
│                                   │
│ (Type ONLY the numbers above)     │
│                                   │
│ Then tap "Test Connection"        │
│ Then tap "Save & Continue"        │
│                                   │
└───────────────────────────────────┘
```

---

## Troubleshooting IP Entry

### "Invalid IP address format"
**Cause:** You typed something wrong

**Check:**
- Are there exactly 4 numbers?
- Are they separated by dots (`.`)?
- Is each number between 0-255?
- No spaces or extra characters?

**Fix:**
```
Retype: 184.174.37.99
```

### "Connection failed"
**Possible causes:**
1. Wrong IP address → Check with IT
2. Wrong port → Should be 3500
3. No internet connection → Check WiFi/data
4. Server is down → Ask IT to check

**What to do:**
1. Double-check IP: `184.174.37.99`
2. Double-check port: `3500`
3. Try tapping "Test Connection" again
4. If still fails, you can still tap "Save & Continue" - the app will work but uploads might fail until server is fixed

---

## Important Security Notes

### ✅ Safe to Share:
- Server IP: `184.174.37.99`
- Port: `3500`

### ❌ NEVER Share:
- VPS password
- SSH login details
- Root access
- Database passwords

**Users only need IP and port. Nothing else!**

---

## Need Help?

If you're stuck on the setup screen:

1. **Check you typed correctly:**
   - IP: `184.174.37.99`
   - Port: `3500`

2. **Try Test Connection**
   - If it works → Great! Tap "Save & Continue"
   - If it fails → Check with IT support

3. **Still stuck?**
   - Take a screenshot
   - Send to IT support
   - They will help you

---

## Summary

**Just remember:**
1. **Server IP field** → Type: `184.174.37.99`
2. **Port field** → Type: `3500`
3. **Nothing else!**

That's it! 🎉
