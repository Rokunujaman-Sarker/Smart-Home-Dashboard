package com.example.smarthomefull;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ESP32SettingsActivity extends AppCompatActivity {

    private EditText ipAddressInput;
    private Button saveButton;
    private Button testConnectionButton;
    private ESP32Controller esp32Controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esp32_settings);

        setTitle("ESP32 Settings");

        ipAddressInput = findViewById(R.id.ipAddressInput);
        saveButton = findViewById(R.id.saveButton);
        testConnectionButton = findViewById(R.id.testConnectionButton);

        esp32Controller = new ESP32Controller(this);

        // Load existing IP address
        String existingIP = esp32Controller.getESP32IP();
        if (existingIP != null && !existingIP.isEmpty()) {
            ipAddressInput.setText(existingIP);
        }

        saveButton.setOnClickListener(v -> saveIPAddress());
        testConnectionButton.setOnClickListener(v -> testConnection());
    }

    private void saveIPAddress() {
        String ipAddress = ipAddressInput.getText().toString().trim();

        if (ipAddress.isEmpty()) {
            Toast.makeText(this, "Please enter an IP address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Basic validation
        if (!isValidIP(ipAddress)) {
            Toast.makeText(this, "Invalid IP address format", Toast.LENGTH_SHORT).show();
            return;
        }

        esp32Controller.saveESP32IP(ipAddress);
        Toast.makeText(this, "ESP32 IP address saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void testConnection() {
        String ipAddress = ipAddressInput.getText().toString().trim();

        if (ipAddress.isEmpty()) {
            Toast.makeText(this, "Please enter an IP address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Temporarily save for testing
        esp32Controller.saveESP32IP(ipAddress);

        Toast.makeText(this, "Testing connection...", Toast.LENGTH_SHORT).show();

        esp32Controller.pingESP32(new ESP32Controller.ESP32Callback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() ->
                    Toast.makeText(ESP32SettingsActivity.this,
                        "✓ ESP32 connected successfully!", Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                    Toast.makeText(ESP32SettingsActivity.this,
                        "✗ Connection failed: " + error, Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private boolean isValidIP(String ip) {
        // Basic IP validation
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;

        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

