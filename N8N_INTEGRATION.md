# n8n Integration Guide

## Overview

The call recording system automatically logs all uploads to a JSON file on your VPS. You can use n8n to monitor this file and update your Google Sheets.

## Upload Log Location

**File Path:** `/var/www/call-recordings/upload-log.json`

**Format:**
```json
[
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
]
```

## n8n Workflow Setup

### Option 1: Webhook-Based (Recommended)

Modify the VPS server to send webhooks to n8n on each upload:

**Step 1: Update server.js**

Add this after line `logs.push(metadata);`:

```javascript
// Send to n8n webhook
const webhookUrl = 'https://your-n8n-instance.com/webhook/call-recording';
fetch(webhookUrl, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(metadata)
}).catch(err => console.error('Webhook error:', err));
```

**Step 2: Restart service**
```bash
systemctl restart call-recording-api
```

**Step 3: Create n8n Workflow**

1. **Webhook Trigger Node**
   - Path: `/webhook/call-recording`
   - Method: POST

2. **Google Sheets Node - Append Row**
   - Sheet: Your recordings sheet
   - Columns:
     - Phone Number: `{{$json.phone_number}}`
     - Call Type: `{{$json.call_type}}`
     - Direction: `{{$json.direction}}`
     - Duration: `{{$json.duration}}`
     - Date: `{{$json.timestamp}}`
     - File Path: `{{$json.file_path}}`
     - File Size: `{{$json.file_size}}`
     - Uploaded At: `{{$json.uploaded_at}}`

### Option 2: File Monitoring (Polling)

**n8n Workflow:**

1. **Schedule Trigger Node**
   - Run every: 5 minutes

2. **Read Binary File Node**
   - File Path: `/var/www/call-recordings/upload-log.json`
   - (Requires n8n to have access to VPS file system)

3. **Code Node** - Process new entries:
```javascript
// Track last processed entry
const lastProcessed = $('LastProcessed').first().json.lastTimestamp || 0;
const logs = JSON.parse($input.item.binary.data);

const newLogs = logs.filter(log => 
  new Date(log.uploaded_at).getTime() > lastProcessed
);

return newLogs.map(log => ({ json: log }));
```

4. **Google Sheets Node** - Append each new entry

5. **Set Node** - Update last processed timestamp

### Option 3: SSH Execution

**n8n Workflow:**

1. **Schedule Trigger** - Every 5 minutes

2. **SSH Node** - Read log file:
```bash
cat /var/www/call-recordings/upload-log.json
```

3. **Code Node** - Parse and filter new entries

4. **Google Sheets Node** - Append rows

## Google Sheet Structure

**Recommended Columns:**

| Column | Example | Source |
|--------|---------|--------|
| Phone Number | +94761234567 | `phone_number` |
| Call Type | Cellular / WhatsApp | `call_type` |
| Direction | Incoming / Outgoing | `direction` |
| Duration (seconds) | 180 | `duration` |
| Date | 2025-01-13 | Parse from `timestamp` |
| Time | 14:30:45 | Parse from `timestamp` |
| File Name | +94761234567_cellular_incoming_2025-01-13_14-30-45.mp3 | `filename` |
| File Size (MB) | 2.34 | `file_size / 1024 / 1024` |
| File Path | /var/www/call-recordings/cellular/incoming/... | `file_path` |
| Uploaded At | 2025-01-13T14:33:45.123Z | `uploaded_at` |
| Download Link | http://184.174.37.99/recordings/... | Constructed |

## Download Links

To make recordings downloadable from Google Sheets, you need to expose them via nginx:

**Step 1: Configure nginx**

```bash
cat > /etc/nginx/sites-available/recordings << 'EOF'
server {
    listen 8888;
    server_name 184.174.37.99;
    
    location /recordings/ {
        alias /var/www/call-recordings/;
        autoindex on;
        auth_basic "Recordings";
        auth_basic_user_file /etc/nginx/.htpasswd;
    }
}
EOF

ln -s /etc/nginx/sites-available/recordings /etc/nginx/sites-enabled/
```

**Step 2: Create password**
```bash
apt-get install apache2-utils
htpasswd -c /etc/nginx/.htpasswd admin
# Enter password when prompted
```

**Step 3: Restart nginx**
```bash
nginx -t
systemctl restart nginx
```

**Step 4: Access recordings**
```
http://184.174.37.99:8888/recordings/cellular/incoming/+94761234567_cellular_incoming_2025-01-13_14-30-45.mp3
```

## Example n8n Workflow (Webhook-Based)

```json
{
  "nodes": [
    {
      "name": "Webhook",
      "type": "n8n-nodes-base.webhook",
      "parameters": {
        "path": "call-recording",
        "responseMode": "responseNode",
        "httpMethod": "POST"
      }
    },
    {
      "name": "Format Data",
      "type": "n8n-nodes-base.code",
      "parameters": {
        "jsCode": "const data = $input.item.json;\n\nreturn {\n  json: {\n    phoneNumber: data.phone_number,\n    callType: data.call_type,\n    direction: data.direction,\n    duration: Math.floor(data.duration / 60) + 'm ' + (data.duration % 60) + 's',\n    date: data.timestamp.split('_')[0],\n    time: data.timestamp.split('_')[1].replace(/-/g, ':'),\n    fileName: data.filename,\n    fileSizeMB: (data.file_size / 1024 / 1024).toFixed(2),\n    downloadLink: 'http://184.174.37.99:8888' + data.file_path.replace('/var/www/call-recordings', '/recordings')\n  }\n};"
      }
    },
    {
      "name": "Google Sheets",
      "type": "n8n-nodes-base.googleSheets",
      "parameters": {
        "operation": "append",
        "sheetId": "YOUR_SHEET_ID",
        "range": "A:J",
        "options": {
          "valueInputMode": "USER_ENTERED"
        }
      }
    },
    {
      "name": "Respond to Webhook",
      "type": "n8n-nodes-base.respondToWebhook",
      "parameters": {
        "respondWith": "json",
        "responseBody": "{\"success\": true}"
      }
    }
  ],
  "connections": {
    "Webhook": {
      "main": [[{ "node": "Format Data", "type": "main", "index": 0 }]]
    },
    "Format Data": {
      "main": [[{ "node": "Google Sheets", "type": "main", "index": 0 }]]
    },
    "Google Sheets": {
      "main": [[{ "node": "Respond to Webhook", "type": "main", "index": 0 }]]
    }
  }
}
```

## Testing

**Test webhook manually:**
```bash
curl -X POST http://your-n8n-instance.com/webhook/call-recording \
  -H "Content-Type: application/json" \
  -d '{
    "filename": "test.mp3",
    "phone_number": "+94761234567",
    "call_type": "cellular",
    "direction": "incoming",
    "duration": 180,
    "timestamp": "2025-01-13_14-30-45",
    "file_path": "/var/www/call-recordings/cellular/incoming/test.mp3",
    "file_size": 2457600,
    "uploaded_at": "2025-01-13T14:33:45.123Z"
  }'
```

## Monitoring

**Check upload log:**
```bash
tail -f /var/www/call-recordings/upload-log.json
```

**Check last 10 uploads:**
```bash
cat /var/www/call-recordings/upload-log.json | jq '.[-10:]'
```

**Count today's recordings:**
```bash
cat /var/www/call-recordings/upload-log.json | jq '.[] | select(.uploaded_at | startswith("2025-01-13"))' | jq -s 'length'
```
