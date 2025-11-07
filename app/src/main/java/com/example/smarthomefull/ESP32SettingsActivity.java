package com.example.smarthomefull;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

public class ESP32SettingsActivity extends AppCompatActivity {

    private Button testConnectionButton;
    private TextView statusText;
    private TextView helpText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esp32_settings);

        setTitle("Firebase Connection Test");

        testConnectionButton = findViewById(R.id.testConnectionButton);
        statusText = findViewById(R.id.statusText);
        helpText = findViewById(R.id.helpText);

        showHelpMessage();

        testConnectionButton.setOnClickListener(v -> testFirebaseConnection());

        updateStatus("Ready to test Firebase connectivity", false);
    }

    private void showHelpMessage() {
        if (helpText != null) {
            helpText.setText("🔥 Firebase-Based Remote Control\n\n" +
                    "Your system uses Firebase Realtime Database.\n" +
                    "This works from ANYWHERE:\n" +
                    "• Same WiFi network ✓\n" +
                    "• Different WiFi network ✓\n" +
                    "• Mobile data ✓\n" +
                    "• Remote location ✓\n\n" +
                    "Make sure:\n" +
                    "1. ESP32 is powered ON and connected to WiFi\n" +
                    "2. Firebase is configured correctly\n" +
                    "3. Database rules allow read/write access");
        }
    }

    private void testFirebaseConnection() {
        updateStatus("🔄 Testing Firebase connection...", false);
        testConnectionButton.setEnabled(false);

        DatabaseReference testRef = FirebaseDatabase.getInstance()
                .getReference("connectionTest")
                .child("timestamp");

        long timestamp = System.currentTimeMillis();

        testRef.setValue(timestamp)
            .addOnSuccessListener(aVoid -> {
                testRef.get().addOnSuccessListener(snapshot -> {
                    Long receivedValue = snapshot.getValue(Long.class);
                    if (receivedValue != null && receivedValue.equals(timestamp)) {
                        runOnUiThread(() -> {
                            updateStatus("✅ Firebase connected successfully!\n\n" +
                                       "Your app can control ESP32 from anywhere.\n" +
                                       "Device control works on any network.", false);
                            testConnectionButton.setEnabled(true);
                            Toast.makeText(ESP32SettingsActivity.this,
                                "✓ Firebase connection verified!", Toast.LENGTH_LONG).show();
                        });
                    }
                }).addOnFailureListener(e -> showError("Read failed: " + e.getMessage()));
            })
            .addOnFailureListener(e -> showError("Write failed: " + e.getMessage()));
    }

    private void showError(String error) {
        runOnUiThread(() -> {
            String troubleshoot = "❌ Firebase connection failed!\n\n" +
                    "Error: " + error + "\n\n" +
                    "Troubleshooting:\n" +
                    "• Check internet connection\n" +
                    "• Verify google-services.json is configured\n" +
                    "• Check Firebase Database Rules\n" +
                    "• Ensure Firebase project is active";

            updateStatus(troubleshoot, true);
            testConnectionButton.setEnabled(true);
            Toast.makeText(ESP32SettingsActivity.this,
                "✗ Failed: " + error, Toast.LENGTH_LONG).show();
        });
    }

    private void updateStatus(String message, boolean isError) {
        if (statusText != null) {
            statusText.setText(message);
            statusText.setTextColor(getResources().getColor(
                isError ? android.R.color.holo_red_light : android.R.color.white));
        }
    }
}
