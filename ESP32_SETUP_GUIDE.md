# ESP32 Smart Home Integration Guide

## Overview
This guide will help you set up ESP32 to control lights, fans, and other appliances in real-time through your Smart Home Dashboard Android app.

## What Changed in the App

### 1. **New Files Added**
- `ESP32Controller.java` - Handles HTTP communication with ESP32
- `ESP32SettingsActivity.java` - Configuration screen for ESP32 IP address
- `activity_esp32_settings.xml` - Layout for settings screen

### 2. **Modified Files**
- `build.gradle` - Added OkHttp library for network communication
- `AndroidManifest.xml` - Added ESP32SettingsActivity
- `RoomActivity.java` - Integrated real-time ESP32 control
- `DashboardActivity.java` - Added ESP32 Settings menu option

### 3. **How It Works**
- When you toggle a device switch in the app:
  1. State is saved to Firebase (for cloud sync)
  2. HTTP request is sent to ESP32 (for real-time control)
  3. ESP32 controls the relay module connected to the appliance

## Hardware Requirements

### ESP32 Board
- Any ESP32 development board (ESP32-WROOM, ESP32-DevKit, etc.)
- Better than ESP8266: Faster, more GPIO pins, dual-core processor

### Relay Modules
- 4-channel or 8-channel relay module (5V)
- Relay ratings should match your appliance power requirements
- Example: 10A/250VAC relays for household appliances

### Other Components
- Jumper wires
- Breadboard (optional)
- Power supply (5V for relays, USB for ESP32)
- Appliances to control (lights, fans, etc.)

## Hardware Wiring

### ESP32 Pin Connections (as per the code):
```
Device          | ESP32 GPIO | Relay Module
----------------|------------|---------------
Living Room Light | GPIO 2   | Relay 1
Bedroom Light    | GPIO 4    | Relay 2
Living Room Fan  | GPIO 5    | Relay 3
Bedroom Fan      | GPIO 18   | Relay 4
Smart Plug 1     | GPIO 19   | Relay 5
Smart Plug 2     | GPIO 21   | Relay 6
```

### Relay Module Connections:
```
ESP32 5V    -> Relay VCC
ESP32 GND   -> Relay GND
ESP32 GPIOx -> Relay INx (Signal pin)
```

### Safety Warning ⚠️
- **HIGH VOLTAGE ALERT**: Relays will control mains voltage (110V/220V)
- Only proceed if you understand electrical safety
- Consider using a qualified electrician for mains wiring
- Test with low-voltage devices (12V LED strips) first
- Never touch relay output terminals while powered

## Software Setup

### Step 1: Install ESP32 Board Support in Arduino IDE

1. Open Arduino IDE
2. Go to **File → Preferences**
3. In "Additional Boards Manager URLs", add:
   ```
   https://dl.espressif.com/dl/package_esp32_index.json
   ```
4. Go to **Tools → Board → Boards Manager**
5. Search for "ESP32"
6. Install **"ESP32 by Espressif Systems"**
7. Select your board: **Tools → Board → ESP32 Arduino → ESP32 Dev Module**

### Step 2: Configure ESP32 Code

1. Open the file: `esp8266\esp32_smart_home\esp32_smart_home.ino`
2. **Update WiFi credentials** (line 30-31):
   ```cpp
   const char* ssid = "YOUR_WIFI_SSID";      // Your WiFi name
   const char* password = "YOUR_WIFI_PASSWORD"; // Your WiFi password
   ```

3. **Customize device mappings** (optional, lines 39-46):
   ```cpp
   Device devices[] = {
     {"light1", 2, false, "Living Room Light"},
     {"fan1", 5, false, "Living Room Fan"},
     // Add or modify devices as needed
   };
   ```

4. **Match device IDs with your app**:
   - The `id` field (e.g., "light1") should match or be contained in the device ID from Firebase
   - When you add a device in the app, Firebase generates an ID like "-O1xYz2ABC..."
   - The ESP32 code will check if the device ID contains your mapped ID

### Step 3: Upload Code to ESP32

1. Connect ESP32 to your computer via USB
2. Select the correct **COM Port**: Tools → Port → COMx
3. Click **Upload** button (→)
4. Wait for "Done uploading" message
5. Open **Serial Monitor** (Tools → Serial Monitor)
6. Set baud rate to **115200**
7. Press **Reset button** on ESP32

### Step 4: Note the IP Address

In the Serial Monitor, you'll see:
```
========================================
ESP32 Smart Home Controller
========================================

Connecting to WiFi: YourWiFiName
..........

✓ WiFi Connected!
IP Address: 192.168.1.100
========================================
IMPORTANT: Enter this IP address in the
Android app's ESP32 Settings screen:
>>> 192.168.1.100 <<<
========================================
```

**Write down this IP address!** You'll need it for the Android app.

## Android App Setup

### Step 1: Sync Gradle
1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. If prompted, accept any dependency updates

### Step 2: Configure ESP32 IP Address
1. Build and run the app
2. Login to your account
3. Tap the **Profile icon** (top-right)
4. Select **"ESP32 Settings"**
5. Enter the IP address from Step 4 above (e.g., `192.168.1.100`)
6. Tap **"Test Connection"** to verify
7. If successful, tap **"Save Settings"**

### Step 3: Test Device Control
1. Go to any room
2. Add a device (tap + button)
3. Toggle the device switch
4. You should see:
   - Firebase state updates
   - ESP32 control confirmation toast
   - Physical relay clicking (if wired)
   - Appliance turning on/off

## Device ID Mapping

### How Device IDs Work

**Firebase** generates unique IDs like:
- `-O1xYz2ABC123def456` (random, unique per device)

**ESP32** uses simple IDs like:
- `light1`, `fan1`, `plug1`

The ESP32 code checks if the Firebase ID **contains** your simple ID. For better matching:

### Option 1: Use Simple Device Names in App
When adding devices in the app, use names that match your ESP32 code:
- Name a device "light1" in the app
- The Firebase ID will contain "light1"
- ESP32 will recognize it

### Option 2: Modify ESP32 Code to Match Firebase IDs
After adding devices in the app, check Firebase to see the actual IDs:
1. Go to Firebase Console → Realtime Database
2. Navigate to: `users → [your-uid] → rooms → [room-name]`
3. Copy the device IDs (e.g., `-O1xYz2ABC...`)
4. Update ESP32 code:
   ```cpp
   Device devices[] = {
     {"-O1xYz2ABC", 2, false, "Living Room Light"},
     {"-O1xYz3DEF", 5, false, "Bedroom Fan"},
   };
   ```

### Option 3: Flexible Mapping (Recommended)
Keep the simple IDs in ESP32, and the code will work as long as the device type or name contains the ID substring.

## Troubleshooting

### App Cannot Connect to ESP32
- **Check WiFi**: Ensure phone and ESP32 are on the same WiFi network
- **Check IP**: IP address might change after ESP32 restarts
- **Firewall**: Some routers block device-to-device communication
- **Ping Test**: Use the "Test Connection" button in ESP32 Settings

### Devices Toggle in App but Don't Work Physically
- **Check Wiring**: Verify GPIO pin connections to relay
- **Check Device Mapping**: Ensure device IDs match
- **Check Serial Monitor**: See if ESP32 receives commands
- **Check Power**: Relays need 5V power supply

### ESP32 Won't Connect to WiFi
- **Check Credentials**: SSID and password must be exact (case-sensitive)
- **2.4GHz WiFi**: ESP32 only supports 2.4GHz, not 5GHz
- **Signal Strength**: Move ESP32 closer to router

### Firebase Works but ESP32 Doesn't
- App will always sync to Firebase
- ESP32 control is optional/additional
- If ESP32 is offline, devices still sync to cloud
- When ESP32 comes online, you can control manually

## Network Configuration

### Static IP (Recommended for Stability)
Add to your ESP32 code in `setup()` function, after `WiFi.begin()`:
```cpp
// Configure static IP (optional but recommended)
IPAddress local_IP(192, 168, 1, 100);  // Your desired IP
IPAddress gateway(192, 168, 1, 1);     // Your router IP
IPAddress subnet(255, 255, 255, 0);
IPAddress primaryDNS(8, 8, 8, 8);
IPAddress secondaryDNS(8, 8, 4, 4);

if (!WiFi.config(local_IP, gateway, subnet, primaryDNS, secondaryDNS)) {
  Serial.println("Static IP configuration failed");
}

WiFi.begin(ssid, password);
```

### Port Forwarding (For Remote Access)
If you want to control devices from outside your home network:
1. Setup port forwarding on your router (port 80 → ESP32 IP)
2. Use your public IP address in the app
3. **Security Warning**: This exposes your ESP32 to the internet
4. Consider using a VPN or secure authentication instead

## Key Differences: ESP32 vs ESP8266

| Feature | ESP8266 | ESP32 |
|---------|---------|-------|
| CPU | Single-core 80MHz | Dual-core 240MHz |
| WiFi | 802.11 b/g/n | 802.11 b/g/n |
| Bluetooth | ❌ No | ✅ Yes (Classic + BLE) |
| GPIO Pins | 17 | 36+ |
| ADC | 1x 10-bit | 2x 12-bit |
| DAC | ❌ No | ✅ 2x 8-bit |
| Touch Sensors | ❌ No | ✅ 10 pins |
| Code Changes | ESP8266WebServer | WebServer |
| Library | #include <ESP8266WiFi.h> | #include <WiFi.h> |
| Performance | Good | Excellent |

## Code Differences

### ESP8266 Code:
```cpp
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
ESP8266WebServer server(80);
```

### ESP32 Code (Current):
```cpp
#include <WiFi.h>
#include <WebServer.h>
WebServer server(80);
```

**Everything else remains the same!** The API and logic are identical.

## Advanced Features

### Add More Devices
1. Add more entries to the `devices[]` array in ESP32 code
2. Connect relays to available GPIO pins
3. Upload updated code to ESP32

### PWM Control (Dimming)
For dimmable lights or variable speed fans:
```cpp
// In handleControl() function
int brightness = server.arg("brightness").toInt(); // 0-255
ledcWrite(channel, brightness);
```

### Temperature/Humidity Monitoring
Add DHT22 sensor:
```cpp
#include <DHT.h>
DHT dht(DHTPIN, DHT22);
// Add /temperature endpoint
```

### Voice Control Integration
- Works with Google Assistant via Firebase
- Works with Alexa using custom skill
- ESP32 responds to app commands regardless of source

## Security Considerations

1. **Network Security**: Keep ESP32 on a separate VLAN if possible
2. **Authentication**: Current implementation has no authentication
3. **HTTPS**: Consider adding HTTPS for encrypted communication
4. **Firewall Rules**: Block external access to ESP32
5. **OTA Updates**: Implement secure over-the-air updates

## Support & Debugging

### Enable Verbose Logging
ESP32 Serial Monitor shows all commands received and executed.

### Test Endpoints Manually
Open browser and visit:
- `http://192.168.1.100/` - Status page
- `http://192.168.1.100/ping` - Test connectivity
- `http://192.168.1.100/status` - JSON device status
- `http://192.168.1.100/control?device=light1&state=on` - Control device

### Common Error Messages

**"ESP32 not configured. Firebase sync only."**
- You haven't set the ESP32 IP address in app settings

**"ESP32 control failed: Failed to connect"**
- ESP32 is offline or IP address changed
- Check if ESP32 is powered on and connected to WiFi

**"Device not mapped, but returning success"**
- Device ID doesn't match any ESP32 devices
- This is normal - Firebase sync still works

## Summary

You now have a complete ESP32-integrated smart home system with:
✅ Real-time device control via WiFi
✅ Cloud sync via Firebase
✅ Android app with ESP32 settings
✅ Configurable device mapping
✅ Easy to expand with more devices

The system works in hybrid mode:
- **Firebase**: Always syncs device states (works anywhere with internet)
- **ESP32**: Provides real-time local control (works on same WiFi network)

If ESP32 is offline, the app still works with Firebase sync only!

