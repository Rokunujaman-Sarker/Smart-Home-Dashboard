# Smart Home Dashboard

<div align="center">

**A comprehensive IoT solution for smart home automation**

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![ESP32](https://img.shields.io/badge/Hardware-ESP32-blue.svg)](https://www.espressif.com/en/products/socs/esp32)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

---

## 📋 Overview

**Smart Home Dashboard** is a full-stack IoT solution that combines an Android mobile application with ESP32 microcontroller firmware to provide real-time smart home device control and monitoring. The system enables users to control lights, fans, and smart plugs remotely through an intuitive mobile interface, with real-time synchronization via Firebase Realtime Database.

### Key Highlights

- 🏠 **Multi-Room Management** - Organize devices by rooms for better control
- 🔄 **Real-Time Sync** - Instant updates across all devices using Firebase
- 🔌 **ESP32 Integration** - Direct hardware control with 6-channel relay support
- 🎛️ **Physical Switch Support** - Manual override with physical switches
- 👥 **Multi-User Auth** - Secure Firebase Authentication (Email/Google Sign-In)
- 📱 **Modern Android UI** - Material Design 3 components with ViewBinding
- 🌐 **WiFi Connectivity** - Seamless ESP32-to-cloud communication

---

## ✨ Features

### Android Application

- **User Authentication**
  - Email/Password registration and login
  - Google Sign-In integration
  - Secure Firebase Authentication
  
- **Dashboard Interface**
  - Visual room-based organization
  - Quick access to all devices
  - Master switch for all devices
  - User profile management
  
- **Device Control**
  - Toggle individual devices (lights, fans, plugs)
  - Real-time status updates
  - Smooth UI transitions with Material Design
  
- **ESP32 Settings**
  - Configure ESP32 connection
  - View device status and IP address
  - Network diagnostics
  
- **Background Sync Service**
  - Automatic device state synchronization
  - Maintains consistency between app and hardware

### ESP32 Firmware

- **6-Channel Relay Control**
  - Support for both active-LOW and active-HIGH relay modules
  - Configurable GPIO pins
  - Safe initialization states
  
- **Physical Switch Integration**
  - 6 manual override switches
  - Debounce logic for reliable operation
  - Bidirectional sync with Firebase
  
- **Firebase Integration**
  - Real-time database streaming
  - Automatic reconnection handling
  - User-specific device paths
  
- **WiFi Management**
  - Auto-reconnect on connection loss
  - Signal strength monitoring
  - Connection status indicators
  
- **Debugging & Monitoring**
  - Serial console logging
  - Real-time status updates
  - LED status indicators

---

## 🛠️ Technologies Used

### Android Application

| Technology | Version | Purpose |
|------------|---------|---------|
| **Android SDK** | API 24-34 | Mobile platform |
| **Java** | 17 | Programming language |
| **Firebase BOM** | 33.4.0 | Backend services suite |
| **Firebase Auth** | Latest | User authentication |
| **Firebase Database** | Latest | Real-time data sync |
| **Material Design** | 1.11.0 | UI components |
| **AndroidX** | Latest | Modern Android libraries |
| **OkHttp** | 4.12.0 | ESP32 HTTP communication |
| **Google Play Services** | 21.2.0 | Google Sign-In |
| **ViewBinding** | Built-in | Type-safe view access |

### ESP32 Firmware

| Technology | Purpose |
|------------|---------|
| **ESP32** | Microcontroller platform |
| **Arduino Core** | Development framework |
| **Firebase_ESP_Client** | Firebase integration library |
| **WiFi.h** | Network connectivity |
| **6-Channel Relay Module** | Device switching |

### Development Tools

- **Android Studio** - Arctic Fox or later
- **Gradle** - 8.12.3
- **Arduino IDE** - 1.8.x or 2.x
- **Git** - Version control

---

## 📦 Installation and Setup

### Prerequisites

- **Android Development:**
  - Android Studio (Arctic Fox or later)
  - JDK 17
  - Android SDK (API 24-34)
  - Firebase account

- **ESP32 Development:**
  - Arduino IDE or PlatformIO
  - ESP32 board package
  - USB cable for programming
  - 6-channel relay module
  - Optional: 6 physical switches

### Android App Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/yourusername/smart-home-dashboard.git
   cd smart-home-dashboard
   ```

2. **Configure Firebase**
   - Create a new project in [Firebase Console](https://console.firebase.google.com/)
   - Enable Firebase Authentication (Email/Password and Google)
   - Enable Firebase Realtime Database
   - Download `google-services.json`
   - Place it in `app/google-services.json`

3. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the project directory
   - Wait for Gradle sync to complete

4. **Build and Run**
   ```bash
   # Command line (Windows)
   gradlew assembleDebug
   gradlew installDebug
   
   # Or use Android Studio's Run button
   ```

### ESP32 Firmware Setup

1. **Install Arduino IDE**
   - Download from [arduino.cc](https://www.arduino.cc/en/software)
   - Install ESP32 board support via Board Manager

2. **Install Required Libraries**
   - Open Arduino IDE
   - Go to **Tools → Manage Libraries**
   - Install:
     - `Firebase Arduino Client Library for ESP8266 and ESP32` by Mobizt

3. **Configure Credentials**
   - Open `esp32_smart_home/ESP32_Smart_Home_Controller.ino`
   - Update the following:
   ```cpp
   const char* ssid = "YOUR_WIFI_SSID";
   const char* password = "YOUR_WIFI_PASSWORD";
   #define API_KEY "YOUR_FIREBASE_API_KEY"
   #define DATABASE_URL "YOUR_FIREBASE_DATABASE_URL"
   #define USER_EMAIL "YOUR_FIREBASE_USER_EMAIL"
   #define USER_PASSWORD "YOUR_FIREBASE_USER_PASSWORD"
   ```

4. **Hardware Connections**
   ```
   ESP32 Pin    →    Device
   ─────────────────────────
   GPIO 4       →    Relay 1 (Light 1)
   GPIO 5       →    Relay 2 (Light 2)
   GPIO 18      →    Relay 3 (Light 3)
   GPIO 19      →    Relay 4 (Fan 1)
   GPIO 21      →    Relay 5 (Fan 2)
   GPIO 22      →    Relay 6 (Plug 1)
   GPIO 2       →    Status LED
   
   GPIO 12-15, 25-26 → Physical Switches (Optional)
   ```

5. **Upload Firmware**
   - Connect ESP32 via USB
   - Select **Tools → Board → ESP32 Dev Module**
   - Select the correct COM port
   - Click **Upload**
   - Open Serial Monitor (115200 baud) to verify

---

## 🚀 Usage Examples

### Mobile App Usage

1. **First Time Setup**
   ```
   1. Launch the app
   2. Register with email/password or Google Sign-In
   3. Create your first room (e.g., "Living Room")
   4. Add devices to the room
   5. Configure ESP32 settings with your device IP
   ```

2. **Controlling Devices**
   - Tap on a room card to view devices
   - Toggle switches to control devices
   - Changes sync instantly to ESP32
   - Use master switch for all devices at once

3. **Adding a New Room**
   - Tap the floating action button (+)
   - Enter room name
   - Add devices to the room

### ESP32 Operation

1. **Power On Sequence**
   - ESP32 connects to WiFi
   - Authenticates with Firebase
   - Initializes relay states to OFF
   - Starts listening for commands
   - Status LED blinks 5 times when ready

2. **Device Control Paths**
   Firebase database structure:
   ```
   /users/{userId}/deviceMapping/
     ├── light1/state: "ON" or "OFF"
     ├── light2/state: "ON" or "OFF"
     ├── light3/state: "ON" or "OFF"
     ├── fan1/state: "ON" or "OFF"
     ├── fan2/state: "ON" or "OFF"
     └── plug1/state: "ON" or "OFF"
   ```

3. **Monitoring**
   - Open Serial Monitor (115200 baud)
   - View WiFi connection status
   - Monitor Firebase commands
   - Check relay state changes

---

## 📂 Project Structure

```
Smart Home Dashboard/
├── app/                          # Android application module
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/smarthomefull/
│   │       │   ├── LoginActivity.java          # Authentication screen
│   │       │   ├── RegistrationActivity.java   # User registration
│   │       │   ├── DashboardActivity.java      # Main dashboard UI
│   │       │   ├── RoomActivity.java           # Room device control
│   │       │   ├── ESP32SettingsActivity.java  # ESP32 configuration
│   │       │   ├── ESP32Controller.java        # ESP32 communication
│   │       │   ├── DeviceSyncService.java      # Background sync service
│   │       │   └── MyApplication.java          # Application class
│   │       ├── res/                            # Resources (layouts, drawables)
│   │       └── AndroidManifest.xml
│   ├── build.gradle                            # Module build config
│   └── google-services.json                    # Firebase configuration
├── esp32_smart_home/
│   └── ESP32_Smart_Home_Controller.ino         # ESP32 firmware
├── build.gradle                                # Root build config
├── settings.gradle                             # Project settings
└── README.md                                   # This file
```

---

## 🔧 Configuration

### Firebase Database Rules

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

### Network Security Configuration

The app includes `network_security_config.xml` to allow cleartext traffic for local ESP32 HTTP communication (if used).

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork the Repository**
   ```bash
   git fork https://github.com/yourusername/smart-home-dashboard.git
   ```

2. **Create a Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Commit Your Changes**
   ```bash
   git commit -m "Add: description of your feature"
   ```

4. **Push to Your Fork**
   ```bash
   git push origin feature/your-feature-name
   ```

5. **Open a Pull Request**
   - Provide a clear description of changes
   - Reference any related issues
   - Include screenshots for UI changes

### Development Guidelines

- Follow existing code style and formatting
- Add comments for complex logic
- Test thoroughly before submitting
- Update documentation for new features
- Do not commit sensitive credentials

---

## 🐛 Troubleshooting

### Android App Issues

**Problem:** Firebase authentication fails
- **Solution:** Verify `google-services.json` is correctly placed in `app/` folder
- Enable Email/Password auth in Firebase Console

**Problem:** Device states not updating
- **Solution:** Check Firebase Realtime Database rules
- Verify internet connection
- Check Firebase Database URL in ESP32 code

**Problem:** Gradle sync fails
- **Solution:** Update Android Studio to latest version
- Check `local.properties` contains correct SDK path
- Run `gradlew clean` and rebuild

### ESP32 Issues

**Problem:** WiFi connection fails
- **Solution:** Verify SSID and password
- Check WiFi signal strength
- Ensure 2.4GHz network (ESP32 doesn't support 5GHz)

**Problem:** Firebase authentication fails
- **Solution:** Verify API_KEY and DATABASE_URL
- Check user email/password exists in Firebase Console
- Verify Firebase Realtime Database is enabled

**Problem:** Relays not responding
- **Solution:** Check relay module connections
- Verify GPIO pin assignments
- Set correct `RELAY_ACTIVE_LOW` value for your relay module
- Test relays with direct digitalWrite commands

**Problem:** Physical switches not working
- **Solution:** Enable pull-up resistors on switch pins
- Check debounce delay settings
- Verify switch wiring (connect to GND when pressed)

---

## 📄 License

This project is licensed under the **MIT License** - see below for details:

```
MIT License

Copyright (c) 2025 Smart Home Dashboard Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

### Support the Project

If you find this project helpful, please consider:
- ⭐ Starring the repository
- 🐛 Reporting bugs
- 💡 Suggesting new features
- 🤝 Contributing code
- 📢 Sharing with others

---

## 🗺️ Roadmap

### Planned Features

- [ ] Voice control integration (Google Assistant/Alexa)
- [ ] Scheduling and automation rules
- [ ] Energy consumption monitoring
- [ ] Temperature and humidity sensors
- [ ] Motion detection and alerts
- [ ] Dark mode support
- [ ] Widget for home screen
- [ ] Scene creation (preset configurations)
- [ ] Multi-language support
- [ ] Cloud backup and restore

### Version History

- **v1.0** (November 2025) - Initial release
  - Basic device control
  - Firebase integration
  - ESP32 firmware with 6-channel support
  - Android app with Material Design

---

## 🙏 Acknowledgments

- **Firebase** - Backend infrastructure and real-time database
- **Google Material Design** - UI/UX guidelines and components
- **ESP32 Community** - Arduino libraries and support
- **Mobizt** - Firebase_ESP_Client library
- **Contributors** - Everyone who has contributed to this project

---

<div align="center">

**Made with ❤️ for the IoT Community**

[⬆ Back to Top](#smart-home-dashboard)

</div>

