# 🔒 SECURITY GUIDE - READ CAREFULLY!

## ⚠️ CRITICAL: What NEVER to Share

### VPS SSH Credentials
**NEVER share these publicly:**
- ❌ SSH password
- ❌ SSH private keys
- ❌ Root password
- ❌ Database passwords

### Where Credentials Should NEVER Appear:
- ❌ App code
- ❌ APK file
- ❌ Documentation shared with users
- ❌ GitHub/public repositories
- ❌ Screenshots
- ❌ Training materials
- ❌ User manuals

---

## What Users SHOULD Know

### ✅ Safe to Share:
- Server IP address: `184.174.37.99`
- Server port: `3500`
- App features and usage

### ❌ NEVER Share with Users:
- SSH login password
- Root access
- VPS control panel credentials
- Database credentials

---

## How to Enter Server IP in App

### Two Separate Fields:

```
┌─────────────────────────────────┐
│  Server IP Address:             │
│  ┌───────────────────────────┐  │
│  │ 184.174.37.99            │  │ ← IP ONLY, no port!
│  └───────────────────────────┘  │
│                                 │
│  Server Port:                   │
│  ┌───────────────────────────┐  │
│  │ 3500                     │  │ ← Port ONLY, no http://
│  └───────────────────────────┘  │
└─────────────────────────────────┘
```

### ✅ Correct Entry:
- **Server IP**: `184.174.37.99`
- **Port**: `3500`

### ❌ Wrong Entry:
- ~~`http://184.174.37.99:3500`~~ (No!)
- ~~`184.174.37.99:3500`~~ (No!)
- ~~`http://184.174.37.99`~~ (No!)

**Just the numbers!**

---

## Security Best Practices

### For VPS Access:

1. **Keep SSH Credentials Secret**
   - Only IT administrators should have SSH access
   - Use strong passwords (20+ characters)
   - Consider using SSH keys instead of passwords

2. **Firewall Configuration**
   - Port 3500: Open (for app uploads)
   - Port 22: Restrict to specific IPs (SSH access)
   - Port 3306: Close or restrict (MySQL)

3. **Regular Security Updates**
   ```bash
   apt update
   apt upgrade -y
   ```

4. **Monitor Access Logs**
   ```bash
   tail -f /var/log/auth.log
   journalctl -u call-recording-api -f
   ```

### For App Deployment:

1. **IP Address Security**
   - ✅ Users enter IP in app setup screen
   - ✅ IP stored locally on device only
   - ✅ No IP hardcoded in APK
   - ✅ Can change IP via app settings

2. **Recording Security**
   - Recordings contain sensitive customer data
   - Only authorized staff should access VPS
   - Consider encrypting recordings at rest
   - Regular backups to secure location

3. **Network Security**
   - Use HTTPS if possible (requires SSL certificate)
   - Consider VPN for extra security
   - Monitor unusual upload patterns

---

## Who Needs What Access?

### Call Center Agents:
- ✅ Can use the app
- ✅ Can view recordings in app
- ✅ Know server IP: `184.174.37.99`
- ❌ NO SSH access
- ❌ NO VPS access

### IT Administrators:
- ✅ Full SSH access to VPS
- ✅ Can configure server
- ✅ Can access all recordings
- ✅ Can change server settings
- ✅ Maintain backups

### Managers:
- ✅ Can access recordings via web interface (if set up)
- ✅ Can view reports
- ❌ NO SSH access
- ❌ NO app configuration

---

## Setting Up Additional Security (Optional)

### 1. Add HTTPS (Recommended)

**Why?** Encrypts uploads between phone and server

**How to add:**
```bash
# Install certbot
apt install certbot

# Get SSL certificate (requires domain name)
certbot certonly --standalone -d recordings.yourcompany.com

# Update nginx to use HTTPS
# Then users enter: recordings.yourcompany.com
```

### 2. Add Authentication (Advanced)

**Why?** Prevents unauthorized uploads

**Add to server.js:**
```javascript
const AUTH_TOKEN = "your-secret-token-here";

app.post('/api/upload-recording', upload.single('file'), (req, res) => {
    const token = req.headers['authorization'];
    
    if (token !== `Bearer ${AUTH_TOKEN}`) {
        return res.status(401).json({ error: 'Unauthorized' });
    }
    
    // ... rest of upload code
});
```

Then update app to send token in headers.

### 3. Restrict Access by IP (Advanced)

**Why?** Only allow uploads from known IPs

```javascript
const ALLOWED_IPS = ['203.94.x.x', '192.168.x.x'];

app.use((req, res, next) => {
    const clientIP = req.ip;
    if (!ALLOWED_IPS.includes(clientIP)) {
        return res.status(403).json({ error: 'Forbidden' });
    }
    next();
});
```

---

## Data Protection Guidelines

### Recording Storage:
1. **Retention Policy**
   - Keep recordings for X months only
   - Delete old recordings regularly
   - Document retention policy

2. **Access Control**
   - Only authorized personnel can access
   - Log who accesses what recordings
   - Regular access audits

3. **Backup Strategy**
   - Daily backups to secure location
   - Test restore procedures
   - Encrypt backup files

### Privacy Compliance:
1. **Inform Callers**
   - "This call may be recorded"
   - Required by law in most countries

2. **Staff Training**
   - How to handle sensitive data
   - When to delete recordings
   - Privacy policies

3. **Data Breach Plan**
   - What to do if data is compromised
   - Who to notify
   - How to prevent future breaches

---

## Emergency Procedures

### If VPS Password is Compromised:

1. **Immediately Change Password**
   ```bash
   passwd root
   ```

2. **Check for Unauthorized Access**
   ```bash
   last -f /var/log/wtmp
   cat /var/log/auth.log | grep "Accepted password"
   ```

3. **Review Uploaded Files**
   ```bash
   ls -ltr /var/www/call-recordings/
   ```

4. **Update Password on All Secure Systems**

### If App is Compromised:

1. **Change Server Port**
   ```bash
   # Edit server.js, change PORT to different number
   # Restart service
   systemctl restart call-recording-api
   ```

2. **Update All Phones**
   - Update server port in app settings
   - Verify uploads work

---

## Security Checklist

### Initial Setup:
- [ ] Strong VPS password set
- [ ] SSH key-based authentication (optional)
- [ ] Firewall configured properly
- [ ] Only port 3500 open to internet
- [ ] Regular backups configured
- [ ] Monitoring set up

### Regular Maintenance:
- [ ] Review access logs weekly
- [ ] Update system monthly
- [ ] Check disk space weekly
- [ ] Test backups monthly
- [ ] Review uploaded recordings

### For Each Phone:
- [ ] App installed
- [ ] Server IP entered correctly
- [ ] Connection tested
- [ ] Permissions granted
- [ ] Test recording made
- [ ] Upload verified

---

## Questions & Answers

**Q: Can users see the VPS password in the app?**
A: No! The password is NEVER in the app. Users only enter IP and port.

**Q: What if I need to change the VPS IP?**
A: Just update it in app settings on each phone. No need to rebuild APK.

**Q: Is the server IP visible in the APK?**
A: No! The IP is entered by users and stored locally on their device only.

**Q: Can call center agents access the VPS?**
A: No! Only IT administrators should have SSH access.

**Q: What happens if someone steals a phone?**
A: They can only see recordings on that phone. They cannot access the VPS or other phones' recordings.

**Q: Should we use HTTPS?**
A: Recommended for extra security, but requires a domain name and SSL certificate.

---

## Summary

### ✅ Safe to Share:
- Server IP address
- Server port number
- App download link
- User documentation

### ❌ NEVER Share:
- SSH password
- Root password
- Database credentials
- Private keys
- API tokens (if you add them)

### 🔒 Best Practices:
1. Keep VPS credentials secret
2. Only IT admin has SSH access
3. Regular security updates
4. Monitor access logs
5. Strong passwords
6. Regular backups
7. Inform callers about recording

---

**Remember: Security is not a one-time setup, it's an ongoing process!** 🔒

For questions about security, consult with your IT security team.
