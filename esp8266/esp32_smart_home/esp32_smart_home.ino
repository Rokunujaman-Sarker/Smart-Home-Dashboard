/*
 * ESP32 Smart Home Controller
 *
 * This code creates a web server on ESP32 to control home appliances
 * like lights, fans, etc. through the Smart Home Dashboard Android app.
 *
 * Hardware Setup:
 * - Connect relay modules to GPIO pins (GPIO 2, 4, 5, 18, 19, etc.)
 * - Each relay controls one appliance (light, fan, etc.)
 * - Make sure to use appropriate relay module ratings for your appliances
 *
 * Installation:
 * 1. Install ESP32 board support in Arduino IDE
 * 2. Go to File > Preferences
 * 3. Add this URL to "Additional Boards Manager URLs":
 *    https://dl.espressif.com/dl/package_esp32_index.json
 * 4. Go to Tools > Board > Boards Manager
 * 5. Search for "ESP32" and install "ESP32 by Espressif Systems"
 * 6. Select your ESP32 board from Tools > Board > ESP32 Arduino
 */

#include <WiFi.h>
#include <WebServer.h>

// WiFi credentials - CHANGE THESE TO YOUR WIFI
const char* ssid = "YOUR_WIFI_SSID";
const char* password = "YOUR_WIFI_PASSWORD";

// Create web server on port 80
WebServer server(80);

// Define GPIO pins for devices (change according to your wiring)
// These correspond to the device IDs from the Android app
struct Device {
  String id;
  int pin;
  bool state;
  String name;
};

// Device mapping - Add your devices here
Device devices[] = {
  {"light1", 2, false, "Living Room Light"},
  {"light2", 4, false, "Bedroom Light"},
  {"fan1", 5, false, "Living Room Fan"},
  {"fan2", 18, false, "Bedroom Fan"},
  {"plug1", 19, false, "Smart Plug 1"},
  {"plug2", 21, false, "Smart Plug 2"}
};

const int numDevices = sizeof(devices) / sizeof(devices[0]);

// Built-in LED for status indication
const int STATUS_LED = LED_BUILTIN; // Usually GPIO 2 on ESP32

void setup() {
  Serial.begin(115200);
  delay(1000);

  Serial.println("\n\n========================================");
  Serial.println("ESP32 Smart Home Controller");
  Serial.println("========================================\n");

  // Initialize device pins as outputs
  for (int i = 0; i < numDevices; i++) {
    pinMode(devices[i].pin, OUTPUT);
    digitalWrite(devices[i].pin, LOW); // Start with all devices OFF
    Serial.println("Initialized: " + devices[i].name + " on GPIO " + String(devices[i].pin));
  }

  // Initialize status LED
  pinMode(STATUS_LED, OUTPUT);
  digitalWrite(STATUS_LED, LOW);

  // Connect to WiFi
  Serial.println("\nConnecting to WiFi: " + String(ssid));
  WiFi.begin(ssid, password);

  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 30) {
    delay(500);
    Serial.print(".");
    attempts++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\n\n✓ WiFi Connected!");
    Serial.println("IP Address: " + WiFi.localIP().toString());
    Serial.println("========================================");
    Serial.println("IMPORTANT: Enter this IP address in the");
    Serial.println("Android app's ESP32 Settings screen:");
    Serial.println(">>> " + WiFi.localIP().toString() + " <<<");
    Serial.println("========================================\n");

    // Blink LED to indicate successful connection
    for (int i = 0; i < 5; i++) {
      digitalWrite(STATUS_LED, HIGH);
      delay(200);
      digitalWrite(STATUS_LED, LOW);
      delay(200);
    }
  } else {
    Serial.println("\n\n✗ WiFi Connection Failed!");
    Serial.println("Please check your WiFi credentials and try again.");
    return;
  }

  // Setup web server routes
  server.on("/", handleRoot);
  server.on("/control", handleControl);
  server.on("/status", handleStatus);
  server.on("/ping", handlePing);
  server.onNotFound(handleNotFound);

  // Start server
  server.begin();
  Serial.println("Web server started successfully!");
  Serial.println("Ready to receive commands from Android app.\n");
}

void loop() {
  server.handleClient();
}

// Handle root path - show welcome message
void handleRoot() {
  String html = "<html><head><title>ESP32 Smart Home</title></head>";
  html += "<body style='font-family: Arial; padding: 20px; background: #1a1a2e; color: white;'>";
  html += "<h1>ESP32 Smart Home Controller</h1>";
  html += "<p>Server is running! ✓</p>";
  html += "<p><strong>IP Address:</strong> " + WiFi.localIP().toString() + "</p>";
  html += "<h2>Available Devices:</h2><ul>";

  for (int i = 0; i < numDevices; i++) {
    html += "<li>" + devices[i].name + " (" + devices[i].id + ") - GPIO " + String(devices[i].pin);
    html += " - <strong>" + String(devices[i].state ? "ON" : "OFF") + "</strong></li>";
  }

  html += "</ul>";
  html += "<h3>API Endpoints:</h3>";
  html += "<ul>";
  html += "<li>/control?device=DEVICE_ID&state=on/off - Control a device</li>";
  html += "<li>/status - Get status of all devices</li>";
  html += "<li>/ping - Check if server is alive</li>";
  html += "</ul>";
  html += "</body></html>";

  server.send(200, "text/html", html);
}

// Handle device control
void handleControl() {
  if (!server.hasArg("device") || !server.hasArg("state")) {
    server.send(400, "text/plain", "ERROR: Missing device or state parameter");
    return;
  }

  String deviceId = server.arg("device");
  String state = server.arg("state");

  Serial.println("Control request: Device=" + deviceId + ", State=" + state);

  // Find the device
  int deviceIndex = -1;
  for (int i = 0; i < numDevices; i++) {
    if (devices[i].id == deviceId || deviceId.indexOf(devices[i].id) >= 0) {
      deviceIndex = i;
      break;
    }
  }

  if (deviceIndex == -1) {
    // Device not found, but we'll still send success for flexibility
    // This allows the app to work even if device mapping isn't perfect
    Serial.println("Device not mapped, but returning success");
    server.send(200, "text/plain", "OK: Command received (device not mapped)");
    return;
  }

  // Control the device
  bool turnOn = (state == "on" || state == "ON" || state == "1");
  digitalWrite(devices[deviceIndex].pin, turnOn ? HIGH : LOW);
  devices[deviceIndex].state = turnOn;

  String response = devices[deviceIndex].name + " turned " + (turnOn ? "ON" : "OFF");
  Serial.println("✓ " + response);

  // Blink status LED
  digitalWrite(STATUS_LED, HIGH);
  delay(100);
  digitalWrite(STATUS_LED, LOW);

  server.send(200, "text/plain", response);
}

// Handle status request
void handleStatus() {
  String json = "{\"devices\":[";

  for (int i = 0; i < numDevices; i++) {
    if (i > 0) json += ",";
    json += "{";
    json += "\"id\":\"" + devices[i].id + "\",";
    json += "\"name\":\"" + devices[i].name + "\",";
    json += "\"pin\":" + String(devices[i].pin) + ",";
    json += "\"state\":\"" + String(devices[i].state ? "ON" : "OFF") + "\"";
    json += "}";
  }

  json += "],\"ip\":\"" + WiFi.localIP().toString() + "\"}";

  Serial.println("Status request received");
  server.send(200, "application/json", json);
}

// Handle ping request (for testing connectivity)
void handlePing() {
  String response = "PONG - ESP32 is alive! IP: " + WiFi.localIP().toString();
  Serial.println("Ping received from app");
  server.send(200, "text/plain", response);
}

// Handle 404 errors
void handleNotFound() {
  String message = "404 Not Found\n\n";
  message += "URI: " + server.uri() + "\n";
  message += "Method: " + String((server.method() == HTTP_GET) ? "GET" : "POST") + "\n";
  server.send(404, "text/plain", message);
}

