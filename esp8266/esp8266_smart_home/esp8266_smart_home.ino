/*
 ESP8266 Smart Home - Firebase listener (per-user)
 - Install FirebaseESP8266 and ArduinoJson libraries
 - Replace WIFI_SSID, WIFI_PASSWORD, FIREBASE_HOST, FIREBASE_AUTH, and USER_UID
 - This sketch reads users/<USER_UID>/mainSwitch and users/<USER_UID>/rooms/*/state
 - Map devices to relays based on discovered order (first N devices)
*/

#include <ESP8266WiFi.h>
#include <FirebaseESP8266.h>
#include <ArduinoJson.h>

#define WIFI_SSID "Bundia vaja"
#define WIFI_PASSWORD "Shishirvai69"

#define FIREBASE_HOST "https://smart-home-dashboard-407-8db57-default-rtdb.firebaseio.com/"
#define FIREBASE_AUTH "MHxeuFLvm5qa5mCS7OSsg9CFpLNnUvphTBz6dITG"

String USER_UID = "vtCQcpNONZhtKmne4UMNq8hOOG02";

FirebaseData fbdo;

const int RELAYS[] = {D1, D2, D3, D4, D5, D6};
const int RELAY_COUNT = sizeof(RELAYS)/sizeof(RELAYS[0]);

void setup(){
  Serial.begin(115200);
  for(int i=0;i<RELAY_COUNT;i++){ pinMode(RELAYS[i], OUTPUT); digitalWrite(RELAYS[i], LOW); }
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to WiFi");
  while(WiFi.status()!=WL_CONNECTED){ delay(500); Serial.print("."); }
  Serial.println("\nWiFi connected");
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);
}

void loop(){
  String mainPath = "/users/" + USER_UID + "/mainSwitch";
  if(Firebase.getString(fbdo, mainPath)){
    String ms = fbdo.stringData();
    if(ms == "OFF"){
      for(int i=0;i<RELAY_COUNT;i++) digitalWrite(RELAYS[i], LOW);
      delay(1000);
      return;
    }
  }

  String path = "/users/" + USER_UID + "/rooms";
  if(Firebase.getJSON(fbdo, path)){
    String payload = fbdo.jsonData();
    DynamicJsonDocument doc(8192);
    DeserializationError err = deserializeJson(doc, payload);
    if(err){ Serial.println("JSON parse error"); } else {
      int idx = 0;
      JsonObject root = doc.as<JsonObject>();
      for (JsonPair roomPair : root) {
        JsonObject roomObj = roomPair.value().as<JsonObject>();
        for (JsonPair devicePair : roomObj) {
          if(idx >= RELAY_COUNT) break;
          JsonObject dev = devicePair.value().as<JsonObject>();
          const char* state = dev["state"];
          bool on = (state && strcmp(state, "ON") == 0);
          digitalWrite(RELAYS[idx], on ? HIGH : LOW);
          idx++;
        }
        if(idx >= RELAY_COUNT) break;
      }
    }
  } else {
    Serial.println("Firebase getJSON failed: "); Serial.println(fbdo.errorReason());
  }
  delay(1000);
}
