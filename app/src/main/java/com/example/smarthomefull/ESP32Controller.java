package com.example.smarthomefull;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import okhttp3.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ESP32Controller {
    private static final String TAG = "ESP32Controller";
    private static final String PREFS_NAME = "ESP32Prefs";
    private static final String KEY_ESP32_IP = "esp32_ip";
    private static final int TIMEOUT_SECONDS = 15;  // Increased from 5 to 15 seconds

    private OkHttpClient client;
    private String esp32IpAddress;
    private Context context;

    public ESP32Controller(Context context) {
        this.context = context;

        // Configure OkHttp to allow cleartext (HTTP) traffic
        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.CLEARTEXT)
                .build();

        this.client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .connectionSpecs(Arrays.asList(spec, ConnectionSpec.COMPATIBLE_TLS))
                .retryOnConnectionFailure(true)
                .build();

        // Load saved IP address
        loadESP32IP();
    }

    private void loadESP32IP() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        esp32IpAddress = prefs.getString(KEY_ESP32_IP, "");
    }

    public void saveESP32IP(String ipAddress) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ESP32_IP, ipAddress).apply();
        this.esp32IpAddress = ipAddress;
    }

    public String getESP32IP() {
        return esp32IpAddress;
    }

    public boolean isConfigured() {
        return esp32IpAddress != null && !esp32IpAddress.trim().isEmpty();
    }

    /**
     * Control a device on the ESP32
     * @param deviceId Device identifier (e.g., "light1", "fan1")
     * @param state true for ON, false for OFF
     * @param callback Callback for result
     */
    public void controlDevice(String deviceId, boolean state, ESP32Callback callback) {
        if (!isConfigured()) {
            callback.onFailure("ESP32 IP address not configured");
            return;
        }

        String stateStr = state ? "on" : "off";
        String url = "http://" + esp32IpAddress + "/control?device=" + deviceId + "&state=" + stateStr;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to control device: " + e.getMessage());
                callback.onFailure("Failed to connect to ESP32: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "Device controlled successfully: " + responseBody);
                    callback.onSuccess(responseBody);
                } else {
                    Log.e(TAG, "ESP32 returned error: " + response.code());
                    callback.onFailure("ESP32 error: " + response.code());
                }
                response.close();
            }
        });
    }

    /**
     * Get status of all devices from ESP32
     * @param callback Callback for result
     */
    public void getDeviceStatus(ESP32Callback callback) {
        if (!isConfigured()) {
            callback.onFailure("ESP32 IP address not configured");
            return;
        }

        String url = "http://" + esp32IpAddress + "/status";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to get status: " + e.getMessage());
                callback.onFailure("Failed to connect to ESP32: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "Status retrieved: " + responseBody);
                    callback.onSuccess(responseBody);
                } else {
                    Log.e(TAG, "ESP32 returned error: " + response.code());
                    callback.onFailure("ESP32 error: " + response.code());
                }
                response.close();
            }
        });
    }

    /**
     * Ping ESP32 to check if it's reachable
     * @param callback Callback for result
     */
    public void pingESP32(ESP32Callback callback) {
        if (!isConfigured()) {
            callback.onFailure("ESP32 IP address not configured");
            return;
        }

        String url = "http://" + esp32IpAddress + "/ping";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Ping failed: " + e.getMessage());
                callback.onFailure("ESP32 not reachable: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "Ping successful: " + responseBody);
                    callback.onSuccess(responseBody);
                } else {
                    callback.onFailure("ESP32 error: " + response.code());
                }
                response.close();
            }
        });
    }

     public static interface ESP32Callback {
        void onSuccess(String response);
        void onFailure(String error);
    }
}
