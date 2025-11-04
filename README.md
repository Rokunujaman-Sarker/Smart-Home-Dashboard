# Smart Home Dashboard

A comprehensive IoT smart home control system featuring an Android mobile application integrated with ESP32 microcontrollers and Firebase real-time database for seamless home automation.

## 📱 Project Overview

Smart Home Dashboard is an end-to-end IoT solution that enables users to control home appliances (lights, fans, plugs) remotely through a mobile application. The system uses ESP32 microcontrollers to interface with relay modules, Firebase for real-time data synchronization, and provides both manual (physical switches) and remote control capabilities.

## ✨ Features

### Mobile Application (Android)
- **User Authentication**
  - Email/Password login and registration
  - Google Sign-In integration
  - Secure Firebase Authentication
  
- **Smart Dashboard**
  - Multi-room management system
  - Real-time device status updates
  - Master switch for all devices
  - Intuitive room-based organization
  
- **Device Control**
  - Individual control of lights, fans, and smart plugs
  - Real-time synchronization with ESP32 controllers
  - Visual feedback with toggle switches
  
- **ESP32 Configuration**
  - Direct IP configuration for ESP32 devices
  - HTTP-based local control (optional)
  - Device ID management

### ESP32 Controller
- **6-Channel Relay Control**
  - Support for 3 lights, 2 fans, and 1 smart plug
  - Configurable active-low/active-high relay logic
  - GPIO pin customization
  
- **Dual Control Mode**
  - Remote control via Firebase
  - Local physical switch control with debouncing
  - Automatic state synchronization
  
- **Real-time Connectivity**
  - WiFi connectivity with auto-reconnect
  - Firebase real-time database streaming
  - Automatic state updates to cloud

- **Reliability Features**
  - Connection status monitoring
  - Auto-restart on critical failures
  - Signal strength reporting

## 🛠️ Technologies Used

### Android Application
- **Language**: Java
- **Build System**: Gradle 8.12.3
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)

**Key Libraries:**
- Firebase Authentication & Realtime Database (BOM 33.4.0)
- Google Play Services Auth 21.2.0
- Material Design Components 1.11.0
- AndroidX AppCompat & ConstraintLayout
- OkHttp 4.12.0 (for ESP32 HTTP communication)
- View Binding

### ESP32 Firmware
- **Platform**: ESP32 (Arduino Framework)
- **Language**: C++
- **Libraries**:
  - WiFi (ESP32 core)
  - Firebase_ESP_Client
  - TokenHelper & RTDBHelper

### Backend
- **Firebase Realtime Database** - Cloud data storage and synchronization
- **Firebase Authentication** - User management and security

## 📋 Prerequisites

### For Android Development
- Android Studio (latest version recommended)
- JDK 17 or higher
- Android SDK with API 24-34
- Google Services JSON configuration file

### For ESP32 Development
- Arduino IDE or PlatformIO
- ESP32 board support package
- Firebase_ESP_Client library

### General Requirements
- Firebase project with:
  - Realtime Database enabled
  - Authentication enabled (Email/Password and Google)
  - Database rules configured
- WiFi network (2.4GHz for ESP32)

## 🚀 Installation and Setup

### 1. Firebase Configuration

1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Enable Authentication:
   - Navigate to Authentication → Sign-in method
   - Enable Email/Password
   - Enable Google Sign-In
3. Create Realtime Database:
   - Navigate to Realtime Database → Create Database
   - Start in test mode or configure security rules
4. Download `google-services.json` and place it in the `app/` directory

**Recommended Database Rules:**
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

### 2. Android App Setup

```bash
# Clone the repository
cd "C:\Users\User\Desktop\Smart Home Dashboard"

# Open the project in Android Studio
# File → Open → Select project directory

# Sync Gradle files
# Android Studio will automatically sync dependencies

# Configure google-services.json
# Place your Firebase configuration file in app/ directory

# Build and run
# Click "Run" or use Shift + F10
```

**Manual Build:**
```bash
# Build debug APK
gradlew assembleDebug

# Build release APK
gradlew assembleRelease

# Install on connected device
gradlew installDebug
```

### 3. ESP32 Setup

1. **Install Required Libraries:**
   - Open Arduino IDE
   - Go to Sketch → Include Library → Manage Libraries
   - Install `Firebase ESP Client` by Mobizt

2. **Configure Firmware:**
   Edit `esp32_smart_home/esp32_firebase_relay_WORKING.ino`:
   
   ```cpp
   // WiFi Credentials
   const char* ssid = "YOUR_WIFI_SSID";
   const char* password = "YOUR_WIFI_PASSWORD";
   
   // Firebase Configuration
   #define API_KEY "YOUR_FIREBASE_API_KEY"
   #define DATABASE_URL "YOUR_DATABASE_URL"
   #define USER_EMAIL "YOUR_FIREBASE_USER_EMAIL"
   #define USER_PASSWORD "YOUR_FIREBASE_USER_PASSWORD"
   ```

3. **Hardware Connection:**
   ```
   ESP32 Pin → Relay Module
   GPIO 4  → Relay 1 (Light 1)
   GPIO 5  → Relay 2 (Light 2)
   GPIO 18 → Relay 3 (Light 3)
   GPIO 19 → Relay 4 (Fan 1)
   GPIO 21 → Relay 5 (Fan 2)
   GPIO 22 → Relay 6 (Plug 1)
   
   Physical Switches (optional):
   GPIO 12 → Switch 1
   GPIO 13 → Switch 2
   GPIO 14 → Switch 3
   GPIO 15 → Switch 4
   GPIO 25 → Switch 5
   GPIO 26 → Switch 6
   ```

4. **Upload Firmware:**
   - Select ESP32 board (Tools → Board → ESP32 Dev Module)
   - Select correct COM port
   - Click Upload

## 📖 Usage Examples

### Mobile App Usage

1. **First-Time Setup:**
   ```
   - Launch the app
   - Register a new account with email/password or Google
   - Access the dashboard
   ```

2. **Adding Rooms:**
   ```
   - Tap the "+" floating action button
   - Enter room name (e.g., "Living Room", "Bedroom")
   - Room appears in the dashboard
   ```

3. **Controlling Devices:**
   ```
   - Tap on a room card
   - Toggle individual device switches (lights, fans, plugs)
   - Changes sync instantly with ESP32
   ```

4. **ESP32 Configuration:**
   ```
   - Tap "ESP32 Settings" button
   - Enter ESP32 IP address (displayed on Serial Monitor)
   - Save configuration for local HTTP control
   ```

### ESP32 Operation

1. **Monitor Status:**
   ```
   - Open Serial Monitor (115200 baud)
   - Check WiFi connection status
   - Verify Firebase authentication
   - Monitor device state changes
   ```

2. **Physical Switch Control:**
   ```
   - Press physical switches connected to ESP32
   - State changes automatically sync to Firebase
   - Mobile app reflects changes in real-time
   ```

### Firebase Database Structure

```
users/
  └── {userId}/
      ├── email: "user@example.com"
      ├── name: "User Name"
      └── rooms/
          └── {roomId}/
              ├── name: "Living Room"
              └── devices/
                  ├── light1: true/false
                  ├── light2: true/false
                  ├── light3: true/false
                  ├── fan1: true/false
                  ├── fan2: true/false
                  └── plug1: true/false
```

## 🏗️ Project Structure

```
Smart Home Dashboard/
├── app/                          # Android application
│   ├── src/main/
│   │   ├── java/com/example/smarthomefull/
│   │   │   ├── LoginActivity.java
│   │   │   ├── RegistrationActivity.java
│   │   │   ├── DashboardActivity.java
│   │   │   ├── RoomActivity.java
│   │   │   ├── ESP32SettingsActivity.java
│   │   │   ├── ESP32Controller.java
│   │   │   └── MyApplication.java
│   │   ├── res/                  # Resources (layouts, drawables, etc.)
│   │   └── AndroidManifest.xml
│   ├── build.gradle              # App-level Gradle config
│   └── google-services.json      # Firebase configuration
├── esp32_smart_home/             # ESP32 firmware
│   └── esp32_firebase_relay_WORKING.ino
├── build.gradle                  # Project-level Gradle config
├── settings.gradle
└── README.md                     # This file
```

## 🔧 Configuration

### Customizing Relay Pins

Edit the ESP32 firmware to change GPIO pins:
```cpp
#define RELAY1_PIN 4   // Change to your preferred pin
#define RELAY2_PIN 5
// ... etc
```

### Relay Logic Mode

For 5V relay modules (common ground), use active-low:
```cpp
#define RELAY_ACTIVE_LOW true
```

For 3.3V or active-high modules:
```cpp
#define RELAY_ACTIVE_LOW false
```

### Adding More Devices

1. Update the relay array in ESP32 firmware
2. Add new device IDs to Firebase database structure
3. Update Android app UI to include new device controls

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

1. **Fork the Repository**
   ```bash
   # Fork on GitHub, then clone
   git clone https://github.com/yourusername/smart-home-dashboard.git
   ```

2. **Create a Feature Branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```

3. **Make Your Changes**
   - Follow existing code style
   - Test thoroughly on both Android and ESP32
   - Update documentation as needed

4. **Commit and Push**
   ```bash
   git commit -m "Add amazing feature"
   git push origin feature/amazing-feature
   ```

5. **Open a Pull Request**
   - Describe your changes clearly
   - Reference any related issues

### Development Guidelines

- Use meaningful variable and function names
- Comment complex logic
- Test on real hardware before submitting
- Ensure Firebase security rules are not compromised
- Follow Material Design guidelines for UI changes

## 🐛 Troubleshooting

### Common Issues

**ESP32 Won't Connect to WiFi:**
- Verify SSID and password
- Ensure 2.4GHz network (ESP32 doesn't support 5GHz)
- Check WiFi signal strength

**Firebase Authentication Fails:**
- Verify API key and database URL
- Check user credentials in Firebase Console
- Ensure Email/Password authentication is enabled

**App Crashes on Launch:**
- Verify `google-services.json` is properly configured
- Check Firebase project settings
- Review logcat for specific errors

**Devices Not Responding:**
- Verify Firebase database rules allow read/write
- Check ESP32 serial monitor for connection status
- Ensure user ID matches in app and database path

## 📄 License

This project is provided as-is for educational and personal use. Please ensure you comply with the licenses of all third-party libraries used:

- Firebase (Google) - [Terms of Service](https://firebase.google.com/terms)
- OkHttp - Apache License 2.0
- Material Design Components - Apache License 2.0
- Firebase_ESP_Client - MIT License

## 📞 Contact & Support

### Getting Help

- **Issues**: For bug reports or feature requests, please create an issue in the repository
- **Documentation**: Refer to the inline code comments for detailed implementation notes
- **Firebase**: [Firebase Documentation](https://firebase.google.com/docs)
- **ESP32**: [ESP32 Documentation](https://docs.espressif.com/projects/esp-idf/en/latest/esp32/)

### Project Information

- **Version**: 1.0
- **Last Updated**: November 2025
- **Application ID**: com.example.smarthomefull

---

## 🎯 Future Enhancements

- [ ] Voice control integration (Google Assistant/Alexa)
- [ ] Scheduling and automation rules
- [ ] Energy monitoring and statistics
- [ ] PWM dimming for lights
- [ ] Temperature and humidity sensor integration
- [ ] Multiple ESP32 device support per user
- [ ] Dark mode support
- [ ] Widget support for quick access
- [ ] Backup and restore settings

---

**Made with ❤️ for Smart Home Automation**

*For questions or feedback, please open an issue on the repository.*

