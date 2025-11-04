#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

const char* ssid = "Bundia vaja";
const char* password = "Shishirvai69";

#define API_KEY "AIzaSyC3Wl4GQKJKlLEeXn8mBd_UXwHutnnse6Y"
#define DATABASE_URL "https://smart-home-dashboard-407-8db57-default-rtdb.firebaseio.com/"
#define USER_EMAIL "rokunujaman100@gmail.com"
#define USER_PASSWORD "123456"

#define RELAY1_PIN 4
#define RELAY2_PIN 5
#define RELAY3_PIN 18
#define RELAY4_PIN 19
#define RELAY5_PIN 21
#define RELAY6_PIN 22
#define LED_PIN 2

// Physical switch input pins (connect switches between these pins and GND)
#define SWITCH1_PIN 12
#define SWITCH2_PIN 13
#define SWITCH3_PIN 14
#define SWITCH4_PIN 15
#define SWITCH5_PIN 25
#define SWITCH6_PIN 26

#define RELAY_ACTIVE_LOW false

FirebaseData fbdo;
FirebaseData stream;
FirebaseAuth auth;
FirebaseConfig config;

bool signupOK = false;
unsigned long lastStreamCheck = 0;

struct Relay {
  const char* id;
  int pin;
  bool currentState;
};

Relay relays[] = {
  {"light1", RELAY1_PIN, false},
  {"light2", RELAY2_PIN, false},
  {"light3", RELAY3_PIN, false},
  {"fan1", RELAY4_PIN, false},
  {"fan2", RELAY5_PIN, false},
  {"plug1", RELAY6_PIN, false}
};
const int numRelays = 6;

struct Switch {
  int pin;
  bool lastState;
  unsigned long lastDebounceTime;
};

Switch switches[] = {
  {SWITCH1_PIN, HIGH, 0},
  {SWITCH2_PIN, HIGH, 0},
  {SWITCH3_PIN, HIGH, 0},
  {SWITCH4_PIN, HIGH, 0},
  {SWITCH5_PIN, HIGH, 0},
  {SWITCH6_PIN, HIGH, 0}
};
const unsigned long debounceDelay = 50;

void streamCallback(FirebaseStream data);
void streamTimeoutCallback(bool timeout);
void initRelays();
void controlRelay(int index, bool state);
void processDeviceUpdate(String deviceId, String value);

void setup() {
  Serial.begin(115200);
  delay(1000);
  Serial.println("\n=== ESP32 Firebase Relay Controller ===");
  Serial.println("Version: November 2025");
  Serial.printf("Relay Logic: %s\n\n", RELAY_ACTIVE_LOW ? "ACTIVE LOW (5V Module)" : "ACTIVE HIGH");

  initRelays();

  Serial.print("Connecting to WiFi: ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);

  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 20) {
    delay(500);
    Serial.print(".");
    attempts++;
  }

  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("\nWiFi connection FAILED! Restarting...");
    delay(5000);
    ESP.restart();
  }

  Serial.println("\nWiFi CONNECTED!");
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());
  Serial.print("Signal Strength: ");
  Serial.print(WiFi.RSSI());
  Serial.println(" dBm\n");

  Serial.println("Configuring Firebase...");
  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;

  // Set token status callback BEFORE Firebase.begin()
  config.token_status_callback = tokenStatusCallback;

  // Assign user credentials for authentication
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;

  Serial.println("Signing in to Firebase with user credentials...");
  Serial.printf("Email: %s\n", USER_EMAIL);

  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  fbdo.setBSSLBufferSize(1024, 1024);
  stream.setBSSLBufferSize(2048, 512);

  Serial.println("Waiting for Firebase authentication...");
  unsigned long startAttempt = millis();

  while (!Firebase.ready() && millis() - startAttempt < 30000) {
    delay(1000);
    Serial.print(".");
  }
  Serial.println();

  if (!Firebase.ready()) {
    Serial.println("\n✗ Firebase authentication FAILED!");
    Serial.println("\nPossible issues:");
    Serial.println("1. Email/password incorrect");
    Serial.println("2. Email authentication not enabled in Firebase Console");
    Serial.println("3. User not registered in Firebase Authentication");
    Serial.println("4. Check Firebase Database Rules allow authenticated access");
    Serial.println("\nContinuing anyway - will retry in background\n");
  } else {
    Serial.println("\n✓ Firebase AUTHENTICATED & READY!");
    Serial.println("User signed in successfully!");
  }

  signupOK = true;

  Serial.println("Initializing device states in Firebase...");
  for (int i = 0; i < numRelays; i++) {
    String path = String("/devices/") + relays[i].id + "/state";
    if (Firebase.RTDB.setString(&fbdo, path.c_str(), "OFF")) {
      Serial.printf("  %s: initialized to OFF\n", relays[i].id);
    } else {
      Serial.printf("  %s: pending initialization\n", relays[i].id);
    }
    delay(200);
  }

  Serial.println("\nStarting Firebase Stream on path: /devices");
  bool streamStarted = Firebase.RTDB.beginStream(&stream, "/devices");

  if (!streamStarted) {
    Serial.println("⚠ Stream will start when Firebase is ready");
  } else {
    Serial.println("✓ Firebase Stream STARTED!");
    Firebase.RTDB.setStreamCallback(&stream, streamCallback, streamTimeoutCallback);
  }

  Serial.println("\n=== SYSTEM READY ===");
  Serial.println("Listening for Firebase commands...");
  Serial.println("Try changing: /devices/light1/state = ON or OFF\n");

  for (int i = 0; i < 5; i++) {
    digitalWrite(LED_PIN, HIGH);
    delay(100);
    digitalWrite(LED_PIN, LOW);
    delay(100);
  }
}

void loop() {
  static bool wasReady = false;
  static unsigned long lastRetry = 0;

  // Monitor physical switches with debouncing
  for (int i = 0; i < numRelays; i++) {
    int reading = digitalRead(switches[i].pin);

    // If switch state changed (button pressed or released)
    if (reading != switches[i].lastState) {
      switches[i].lastDebounceTime = millis();
    }

    // Check if enough time has passed since last change (debounce)
    if ((millis() - switches[i].lastDebounceTime) > debounceDelay) {
      // If button is pressed (LOW because of INPUT_PULLUP)
      if (reading == LOW && switches[i].lastState == HIGH) {
        // Toggle relay state
        bool newState = !relays[i].currentState;

        Serial.printf("\n[SWITCH %d PRESSED] Toggling %s to %s\n",
                     i+1, relays[i].id, newState ? "ON" : "OFF");

        // Control relay locally
        controlRelay(i, newState);

        // Update Firebase so app stays in sync
        if (Firebase.ready()) {
          String path = String("/devices/") + relays[i].id + "/state";
          String stateStr = newState ? "ON" : "OFF";
          Firebase.RTDB.setString(&fbdo, path.c_str(), stateStr.c_str());
        }

        // Flash LED to indicate switch press
        for (int k = 0; k < 2; k++) {
          digitalWrite(LED_PIN, HIGH);
          delay(100);
          digitalWrite(LED_PIN, LOW);
          delay(100);
        }
      }
    }

    switches[i].lastState = reading;
  }

  // Firebase stream monitoring
  if (Firebase.ready() && signupOK) {
    if (!wasReady) {
      Serial.println("\n✓ Firebase connection established!");
      wasReady = true;

      if (!stream.httpConnected()) {
        Serial.println("Starting Firebase Stream...");
        if (Firebase.RTDB.beginStream(&stream, "/devices")) {
          Serial.println("✓ Stream started successfully!");
          Firebase.RTDB.setStreamCallback(&stream, streamCallback, streamTimeoutCallback);
        }
      }
    }

    if (!Firebase.RTDB.readStream(&stream)) {
      // Silent retry
    }

    if (stream.streamAvailable()) {
      Serial.println("[DEBUG] Stream data available");
    }
  } else if (millis() - lastRetry > 5000) {
    lastRetry = millis();
    if (!wasReady) {
      Serial.println("[INFO] Attempting Firebase connection...");
    }
  }

  // Heartbeat LED
  static unsigned long lastBlink = 0;
  if (millis() - lastBlink > 2000) {
    lastBlink = millis();
    digitalWrite(LED_PIN, HIGH);
    delay(10);
    digitalWrite(LED_PIN, LOW);

    if (millis() - lastStreamCheck > 30000) {
      lastStreamCheck = millis();
      Serial.println("[STATUS] System running");
      Serial.printf("WiFi: %s | Firebase: %s | Stream: %s\n",
                   WiFi.status() == WL_CONNECTED ? "OK" : "DISCONNECTED",
                   Firebase.ready() ? "READY" : "NOT READY",
                   stream.httpConnected() ? "CONNECTED" : "DISCONNECTED");
    }
  }

  // WiFi reconnection
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("WiFi disconnected! Reconnecting...");
    WiFi.reconnect();
    delay(100);
  }

  delay(10);
}

void streamCallback(FirebaseStream data) {
  Serial.println("\n╔═══════════════════════���═══════���═══════╗");
  Serial.println("║   FIREBASE COMMAND RECEIVED!          ║");
  Serial.println("╚═══════════════════════���═══════���═══════╝");

  String path = data.dataPath();
  String value = data.stringData();
  String dataType = data.dataType();

  Serial.printf("Data Type: %s\n", dataType.c_str());
  Serial.printf("Full Path: %s\n", path.c_str());
  Serial.printf("Value: %s\n", value.c_str());

  if (path == "/" || path.isEmpty()) {
    Serial.println("Root path update - checking all devices");

    if (data.dataType() == "json") {
      FirebaseJson *json = data.to<FirebaseJson *>();
      size_t count = json->iteratorBegin();
      String key, val;
      int type = 0;

      for (size_t i = 0; i < count; i++) {
        json->iteratorGet(i, type, key, val);
        Serial.printf("  Checking device: %s\n", key.c_str());

        FirebaseJsonData result;
        if (json->get(result, key + "/state")) {
          String state = result.to<String>();
          processDeviceUpdate(key, state);
        }
      }
      json->iteratorEnd();
    }
    return;
  }

  String deviceId = "";
  String tempPath = path;
  tempPath.replace("/", "");

  int statePos = tempPath.indexOf("state");
  if (statePos > 0) {
    deviceId = tempPath.substring(0, statePos);
  } else {
    deviceId = tempPath;
  }

  Serial.printf("Extracted Device ID: '%s'\n", deviceId.c_str());
  processDeviceUpdate(deviceId, value);

  Serial.println("╚═══════════════════════���═══════════════╝\n");
}

void processDeviceUpdate(String deviceId, String value) {
  bool found = false;

  for (int j = 0; j < numRelays; j++) {
    if (deviceId.equals(relays[j].id)) {
      found = true;

      bool newState = false;
      if (value.equalsIgnoreCase("ON") || value == "1" || value.equalsIgnoreCase("true")) {
        newState = true;
      }

      Serial.printf("\n→ Controlling Relay:\n");
      Serial.printf("   Device: %s\n", relays[j].id);
      Serial.printf("   GPIO Pin: %d\n", relays[j].pin);
      Serial.printf("   Command: %s\n", newState ? "ON" : "OFF");

      controlRelay(j, newState);

      Serial.printf("✓ SUCCESS: %s is now %s\n", relays[j].id, newState ? "ON" : "OFF");

      for (int k = 0; k < 3; k++) {
        digitalWrite(LED_PIN, HIGH);
        delay(50);
        digitalWrite(LED_PIN, LOW);
        delay(50);
      }
      break;
    }
  }

  if (!found) {
    Serial.printf("⚠ WARNING: No relay found for '%s'\n", deviceId.c_str());
    Serial.println("Available devices:");
    for (int j = 0; j < numRelays; j++) {
      Serial.printf("  - %s (GPIO %d)\n", relays[j].id, relays[j].pin);
    }
  }
}

void streamTimeoutCallback(bool timeout) {
  if (timeout) {
    Serial.println("⚠ Stream timeout");
  }

  if (!stream.httpConnected()) {
    Serial.printf("✗ Stream disconnected: %s\n", stream.errorReason().c_str());
    Serial.println("→ Reconnecting stream...");

    if (Firebase.RTDB.beginStream(&stream, "/devices")) {
      Serial.println("✓ Stream reconnected");
      Firebase.RTDB.setStreamCallback(&stream, streamCallback, streamTimeoutCallback);
    }
  }
}

void initRelays() {
  Serial.println("Initializing GPIO pins...");
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, LOW);

  for (int i = 0; i < numRelays; i++) {
    pinMode(relays[i].pin, OUTPUT);

    if (RELAY_ACTIVE_LOW) {
      digitalWrite(relays[i].pin, HIGH);
    } else {
      digitalWrite(relays[i].pin, LOW);
    }

    relays[i].currentState = false;
    Serial.printf("  GPIO %d (%s): OUTPUT - Initial: OFF\n", relays[i].pin, relays[i].id);
  }

  Serial.println("Initializing switch input pins...");
  for (int i = 0; i < numRelays; i++) {
    pinMode(switches[i].pin, INPUT_PULLUP);
    Serial.printf("  GPIO %d (Switch %d): INPUT_PULLUP\n", switches[i].pin, i+1);
  }

  Serial.println("All pins initialized\n");
}

void controlRelay(int index, bool state) {
  if (index < 0 || index >= numRelays) {
    Serial.printf("ERROR: Invalid relay index %d\n", index);
    return;
  }

  // Inverted logic for 5V relay module (Active LOW)
  // state = true (ON) should output LOW to energize relay
  // state = false (OFF) should output HIGH to de-energize relay
  if (RELAY_ACTIVE_LOW) {
    // For active LOW relays: invert the logic
    digitalWrite(relays[index].pin, state ? LOW : HIGH);
  } else {
    // For active HIGH relays: normal logic
    digitalWrite(relays[index].pin, state ? HIGH : LOW);
  }

  relays[index].currentState = state;

  int actualPinState = digitalRead(relays[index].pin);
  Serial.printf("   Pin State: GPIO %d = %s\n",
               relays[index].pin,
               actualPinState == HIGH ? "HIGH" : "LOW");

  if (RELAY_ACTIVE_LOW) {
    Serial.printf("   Relay: %s (Active LOW)\n",
                 actualPinState == LOW ? "ENERGIZED (ON)" : "DE-ENERGIZED (OFF)");
  } else {
    Serial.printf("   Relay: %s (Active HIGH)\n",
                 actualPinState == HIGH ? "ENERGIZED (ON)" : "DE-ENERGIZED (OFF)");
  }
}
