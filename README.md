Smart Home Dashboard - Full Project
==================================

What's included:
- Android Studio project skeleton (Java) with gradient-blue UI matching provided template
- 6 Room pages (LivingRoom, Bedroom, Kitchen, Bathroom, Garage, Office)
- Main switch support
- Firebase Auth (Email/Google) integration placeholders
- ESP8266 sketch that listens to user's Firebase path and controls relays
- Instructions for setup (place google-services.json, set Firebase credentials, add SHA-1 etc.)

How to use:
1. Open the Android project in Android Studio (folder: app).
2. Add your google-services.json to app/ and replace placeholder strings in app/src/main/res/values/strings.xml.
3. Configure Firebase Realtime Database and Authentication (Email/Password + Google Sign-In).
4. For ESP: open esp8266/esp8266_smart_home.ino in Arduino IDE, set WIFI_SSID, WIFI_PASSWORD, FIREBASE_HOST, FIREBASE_AUTH, and USER_UID.
