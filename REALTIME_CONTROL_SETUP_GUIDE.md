# Real-Time Device Control Setup Guide
## ESP8266 + Android App via WiFi

Your app is **already configured** for real-time control! Here's what you need to do:

## 🎯 Current System Overview

**What You Have:**
- ✅ Android app with Firebase Realtime Database integration
- ✅ ESP8266 code that reads device states from Firebase
- ✅ Real-time synchronization between app and hardware

**How It Works:**
1. User toggles a device switch in the Android app
2. App writes "ON" or "OFF" to Firebase Realtime Database
3. ESP8266 reads Firebase every 1 second
4. ESP8266 controls physical relays based on device states
5. Relays control lights, fans, and other appliances

---

## 📋 Setup Steps

### Step 1: Hardware Setup (ESP8266)

**Required Components:**
- ESP8266 board (NodeMCU, Wemos D1 Mini, etc.)
- Relay module (6-channel recommended, supports up to 6 devices)
- Power supply for relays (usually 5V)
- Lights, fans, or other 220V/110V appliances

**Wiring:**
```
ESP8266 Pin → Relay Module
D1 → Relay 1 (First device)
D2 → Relay 2 (Second device)
D3 → Relay 3 (Third device)
D4 → Relay 4 (Fourth device)
D5 → Relay 5 (Fifth device)
D6 → Relay 6 (Sixth device)
GND → GND
```

**Relay to Appliance:**
- Connect AC appliances to relay's COM (common) and NO (normally open) terminals
- ⚠️ **WARNING**: Working with AC voltage is dangerous. If unsure, hire a professional electrician.

---

### Step 2: Configure ESP8266 Code

**Edit the file:** `esp8266/esp8266_smart_home.ino`

**Required Changes:**

1. **WiFi Credentials** (Line 13-14):
```cpp
#define WIFI_SSID "YourActualWiFiName"
#define WIFI_PASSWORD "YourActualWiFiPassword"
```

2. **Firebase Configuration** (Line 16-17):
   - Open Firebase Console: https://console.firebase.google.com
   - Select your project
   - Go to "Realtime Database" → Get the database URL
   - Example: `your-project-12345.firebaseio.com` or `your-project-12345-default-rtdb.firebaseio.com`

```cpp
#define FIREBASE_HOST "your-project-12345-default-rtdb.firebaseio.com"
```

3. **Firebase Authentication Token**:
   - Option A (Legacy Database Secret - Easier):
     - Firebase Console → Project Settings → Service Accounts → Database Secrets
     - Copy the secret key
   
   - Option B (Database Token - More Secure):
     - Create a custom token or use auth token
     
```cpp
#define FIREBASE_AUTH "your_database_secret_or_token_here"
```

4. **User UID** (Line 19):
   - Run your Android app and login
   - Check Firebase Console → Authentication → Users
   - Copy your User UID

```cpp
String USER_UID = "paste_your_user_uid_here";
```

---

### Step 3: Install Required Libraries

**Arduino IDE Setup:**

1. **Install ESP8266 Board Support:**
   - File → Preferences
   - Additional Boards Manager URLs: `http://arduino.esp8266.com/stable/package_esp8266com_index.json`
   - Tools → Board → Boards Manager → Search "ESP8266" → Install

2. **Install Libraries:**
   - Sketch → Include Library → Manage Libraries
   - Search and install:
     - ✅ `FirebaseESP8266` by Mobizt
     - ✅ `ArduinoJson` by Benoit Blanchon (v6.x)

---

### Step 4: Upload Code to ESP8266

1. Connect ESP8266 to computer via USB
2. Tools → Board → Select your ESP8266 board (e.g., "NodeMCU 1.0")
3. Tools → Port → Select the correct COM port
4. Click Upload button
5. Open Serial Monitor (115200 baud) to see connection status

**Expected Serial Output:**
```
Connecting to WiFi....
WiFi connected
[Device states updating every second]
```

---

### Step 5: Using the Android App

**The app is already configured!** No code changes needed.

**How to Control Devices:**

1. **Launch the app** and login
2. **Add a room** (tap the + button)
3. **Enter the room** and add devices
4. **Toggle device switches** - ESP8266 will respond within 1 second!

**Main Switch:**
- The main switch on the dashboard turns OFF all relays when disabled
- Turn it ON to allow individual device control

---

## 🔧 How Device Mapping Works

The ESP8266 maps devices to relays in the order they appear in Firebase:
- **First device** found → Relay on D1 (Index 0)
- **Second device** found → Relay on D2 (Index 1)
- **Third device** found → Relay on D3 (Index 2)
- And so on...

**Example:**
```
Room: Living Room
├── Device 1: Ceiling Light → D1 (Relay 1)
├── Device 2: Table Lamp → D2 (Relay 2)

Room: Bedroom
├── Device 3: Fan → D3 (Relay 3)
├── Device 4: LED Strip → D4 (Relay 4)
```

---

## 📱 Firebase Database Structure

Your database structure looks like this:

```
users/
  └── {userUID}/
      ├── mainSwitch: "ON" or "OFF"
      └── rooms/
          ├── living_room/
          │   ├── device_id_1/
          │   │   ├── name: "Ceiling Light"
          │   │   ├── type: "Light"
          │   │   └── state: "ON" or "OFF"
          │   └── device_id_2/
          │       ├── name: "Table Lamp"
          │       ├── type: "Light"
          │       └── state: "OFF"
          └── bedroom/
              └── device_id_3/
                  ├── name: "Fan"
                  ├── type: "Fan"
                  └── state: "ON"
```

---

## ⚡ Response Time

- **App to Firebase:** Instant (< 100ms)
- **Firebase to ESP8266:** 1 second (polling interval)
- **Total latency:** ~1 second from switch toggle to relay activation

**To improve response time**, you can:
- Reduce the `delay(1000)` in ESP8266 code to `delay(500)` for 0.5s updates
- Or use Firebase streaming (more complex, requires different library approach)

---

## 🐛 Troubleshooting

### ESP8266 won't connect to WiFi
- Check WiFi credentials are correct
- Ensure WiFi is 2.4GHz (ESP8266 doesn't support 5GHz)
- Check WiFi signal strength

### ESP8266 connects but devices don't respond
- Verify Firebase URL is correct (check for https:// - don't include it!)
- Verify Firebase AUTH token is valid
- Check USER_UID matches your logged-in user
- Open Serial Monitor to see error messages

### Devices turn on/off randomly
- Check Firebase security rules allow read/write for authenticated users
- Verify only one ESP8266 is running with same USER_UID

### App shows "Firebase not ready"
- Check `google-services.json` is in the app folder
- Verify internet connection
- Check Firebase project is active

### Relay doesn't control appliance
- Test relay with a multimeter or LED
- Some relays are active-LOW (change `HIGH` to `LOW` and vice versa in code)
- Check relay power supply

---

## 🔐 Security Recommendations

1. **Firebase Security Rules:**
   - Currently your rules likely allow authenticated users to read/write their own data
   - Don't share your Firebase credentials

2. **Physical Security:**
   - ESP8266 should be in a protected enclosure
   - Use proper wire gauges for AC current
   - Include fuses for safety

3. **Network Security:**
   - Use WPA2 or WPA3 WiFi encryption
   - Consider a separate IoT network

---

## 🎉 You're Ready!

Your system is complete! The Android app controls devices in real-time via Firebase, and the ESP8266 listens for changes and controls physical relays.

**Quick Start Checklist:**
- [ ] Configure WiFi credentials in ESP8266 code
- [ ] Configure Firebase URL in ESP8266 code
- [ ] Get and set User UID in ESP8266 code
- [ ] Install Arduino libraries (FirebaseESP8266, ArduinoJson)
- [ ] Upload code to ESP8266
- [ ] Wire relays to ESP8266
- [ ] Connect appliances to relays (⚠️ be careful with AC!)
- [ ] Open app, add rooms and devices
- [ ] Toggle switches and watch devices respond!

**Questions? Check the Serial Monitor output for debugging information.**

