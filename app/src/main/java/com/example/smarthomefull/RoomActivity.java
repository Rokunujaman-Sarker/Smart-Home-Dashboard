package com.example.smarthomefull;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.HashMap;
import java.util.Map;

public class RoomActivity extends AppCompatActivity {

    LinearLayout devicesContainer;
    FloatingActionButton fabAddDevice;
    TextView roomTitleText;
    String roomName;
    String roomDisplayName;
    DatabaseReference roomRef;
    DatabaseReference userRef;
    DatabaseReference devicesRef; // Device reference for ESP32 sync
    String uid;
    boolean firebaseReady = false;
    boolean isMainSwitchOn = true;

    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        devicesContainer = findViewById(R.id.devicesContainer);
        fabAddDevice = findViewById(R.id.fabAddDevice);
        roomTitleText = findViewById(R.id.roomTitle);

        roomName = getIntent().getStringExtra("roomName");
        roomDisplayName = getIntent().getStringExtra("roomDisplayName");

        if (roomName == null || roomName.trim().isEmpty()) {
            roomName = "Room";
        }
        if (roomDisplayName == null || roomDisplayName.trim().isEmpty()) {
            roomDisplayName = roomName;
        }

        setTitle(roomDisplayName);
        roomTitleText.setText(roomDisplayName);

        // Init Firebase safely
        try {
            FirebaseApp app = FirebaseApp.initializeApp(this);
            firebaseReady = (app != null);
        } catch (Throwable t) {
            firebaseReady = false;
        }

        FirebaseUser user = null;
        if (firebaseReady) {
            user = FirebaseAuth.getInstance().getCurrentUser();
        }
        if (user != null) {
            uid = user.getUid();
            roomRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("rooms").child(roomName);
            userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

            // Device mapping reference - shared with ESP32 for synchronization
            devicesRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("deviceMapping");

            // Listen to main switch state
            listenToMainSwitch();
        }

        // FAB to add device
        fabAddDevice.setOnClickListener(v -> {
            if (!firebaseReady || roomRef == null) {
                Toast.makeText(this, R.string.no_data_offline, Toast.LENGTH_SHORT).show();
            } else {
                showAddDeviceDialog();
            }
        });

        loadDevices();
    }

    void showAddDeviceDialog(){
        View dlg = LayoutInflater.from(this).inflate(R.layout.dialog_add_device, null);
        AlertDialog ad = new AlertDialog.Builder(this)
            .setTitle("Add Device")
            .setView(dlg)
            .create();

        Button addBtn = dlg.findViewById(R.id.addBtn);
        final EditText nameEt = dlg.findViewById(R.id.deviceName);
        final android.widget.Spinner spinner = dlg.findViewById(R.id.deviceType);

        addBtn.setOnClickListener(v -> {
            if (!firebaseReady || roomRef == null) {
                ad.dismiss();
                return;
            }

            String name = nameEt.getText().toString().trim();
            String type = spinner.getSelectedItem().toString();

            if(name.isEmpty()){
                Toast.makeText(this, R.string.enter_name, Toast.LENGTH_SHORT).show();
                return;
            }

            String id = roomRef.push().getKey();
            Map<String,Object> data = new HashMap<>();
            data.put("name", name);
            data.put("type", type);
            data.put("state", "OFF");

            if(id!=null) {
                // Save to room
                roomRef.child(id).setValue(data)
                    .addOnSuccessListener(aVoid -> {
                        // Also initialize in global devices if not exists
                        String deviceKey = generateDeviceKey(name);
                        devicesRef.child(deviceKey).child("state").get().addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().exists()) {
                                Map<String, Object> globalData = new HashMap<>();
                                globalData.put("name", name);
                                globalData.put("type", type);
                                globalData.put("state", "OFF");
                                devicesRef.child(deviceKey).setValue(globalData);
                            }
                        });

                        Toast.makeText(this, "Device added successfully!", Toast.LENGTH_SHORT).show();
                        ad.dismiss();
                    })
                    .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to add device: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        ad.show();
    }

    // Generate a consistent key for device name (for global sync)
    private String generateDeviceKey(String deviceName) {
        if (deviceName == null) return "device";
        return deviceName.toLowerCase()
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    void loadDevices(){
        devicesContainer.removeAllViews();

        if (!firebaseReady || roomRef == null) {
            // Show offline placeholder
            TextView placeholder = new TextView(this);
            placeholder.setText(getString(R.string.no_data_offline));
            placeholder.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            placeholder.setPadding(16, 32, 16, 32);
            placeholder.setGravity(android.view.Gravity.CENTER);
            devicesContainer.addView(placeholder);
            return;
        }

        roomRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                devicesContainer.removeAllViews();

                boolean hasDevices = false;
                for(DataSnapshot child: snapshot.getChildren()){
                    // Skip the "name" field which stores room name
                    if ("name".equals(child.getKey())) continue;

                    hasDevices = true;
                    String id = child.getKey();
                    String name = child.child("name").getValue(String.class);
                    String type = child.child("type").getValue(String.class);
                    String state = child.child("state").getValue(String.class);

                    createDeviceCard(id, name, type, state);
                }

                if (!hasDevices) {
                    TextView placeholder = new TextView(RoomActivity.this);
                    placeholder.setText("No devices in this room yet.\nTap + to add a device!");
                    placeholder.setTextColor(ContextCompat.getColor(RoomActivity.this, android.R.color.white));
                    placeholder.setTextSize(16f);
                    placeholder.setPadding(16, 32, 16, 32);
                    placeholder.setGravity(android.view.Gravity.CENTER);
                    devicesContainer.addView(placeholder);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RoomActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void createDeviceCard(String id, String name, String type, String state) {
        // Create a nice card layout for each device
        LinearLayout deviceCard = new LinearLayout(this);
        deviceCard.setOrientation(LinearLayout.HORIZONTAL);
        deviceCard.setPadding(24, 20, 24, 20);
        deviceCard.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_blue_light));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 12);
        deviceCard.setLayoutParams(cardParams);

        // Left side - device info
        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        );
        infoLayout.setLayoutParams(infoParams);

        TextView nameText = new TextView(this);
        String displayName = name != null ? name : getString(R.string.device_default_name);
        nameText.setText(displayName);
        nameText.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        nameText.setTextSize(18f);
        nameText.setTextColor(ContextCompat.getColor(this, android.R.color.black));

        TextView typeText = new TextView(this);
        String typeDisplay = type != null ? type : "";
        typeText.setText(typeDisplay);
        typeText.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        typeText.setTextSize(14f);

        infoLayout.addView(nameText);
        infoLayout.addView(typeText);

        // Right side - switch
        SwitchCompat deviceSwitch = new SwitchCompat(this);
        deviceSwitch.setChecked("ON".equals(state));

        // Flag to prevent infinite loops when syncing
        final boolean[] isUpdatingFromFirebase = {false};

        // Listen to the ROOM state (this is what DeviceSyncService updates!)
        if (id != null && roomRef != null) {
            // Listen to THIS room's device state (the source of truth)
            roomRef.child(id).child("state").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String roomState = snapshot.getValue(String.class);
                    boolean isOn = "ON".equals(roomState);

                    android.util.Log.d("RoomActivity", "Firebase state changed for " + name + ": " + roomState);

                    // Update UI without triggering the listener
                    isUpdatingFromFirebase[0] = true;
                    deviceSwitch.setChecked(isOn);
                    isUpdatingFromFirebase[0] = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    android.util.Log.e("RoomActivity", "Error reading room device state: " + error.getMessage());
                }
            });

            // Handle user interactions with the switch
            deviceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Ignore if this change came from Firebase update
                if (isUpdatingFromFirebase[0]) {
                    return;
                }

                // Check if main switch allows this operation
                if (!isMainSwitchOn) {
                    Toast.makeText(RoomActivity.this, "Main switch is OFF. Please turn it ON first.", Toast.LENGTH_SHORT).show();
                    // Revert the switch state
                    isUpdatingFromFirebase[0] = true;
                    buttonView.setChecked(!isChecked);
                    isUpdatingFromFirebase[0] = false;
                    return;
                }

                // User manually toggled the switch
                String newState = isChecked ? "ON" : "OFF";

                android.util.Log.d("RoomActivity", "User toggled " + name + " to " + newState);

                // Update THIS room's device state (source of truth)
                roomRef.child(id).child("state").setValue(newState);

                // ALSO update deviceMapping so ESP32 receives the command
                if (devicesRef != null && name != null) {
                    String esp32DeviceId = mapDeviceNameToESP32Id(name);
                    if (esp32DeviceId != null) {
                        devicesRef.child(esp32DeviceId).child("state").setValue(newState);
                        android.util.Log.d("RoomActivity", "Updated deviceMapping/" + esp32DeviceId + " to " + newState);
                    }
                }
            });
        }

        deviceCard.addView(infoLayout);
        deviceCard.addView(deviceSwitch);

        // Long click to edit/delete device
        deviceCard.setOnLongClickListener(v -> {
            showDeviceOptions(id, displayName);
            return true;
        });

        devicesContainer.addView(deviceCard);
    }

    /**
     * Control ESP32 device - NOW USES FIREBASE FOR REMOTE CONTROL!
     * Updated to use /users/{uid}/globalDevices/ path to match ESP32
     */
    private void controlESP32Device(String deviceId, String deviceName, boolean isOn) {
        String esp32DeviceId = mapDeviceNameToESP32Id(deviceName);
        if (esp32DeviceId == null) {
            return;
        }

        String stateStr = isOn ? "ON" : "OFF";

        // FIXED: Use the same path that ESP32 is listening to
        // Path: /users/{uid}/globalDevices/{deviceId}/state
        if (devicesRef != null) {
            DatabaseReference esp32DeviceRef = devicesRef
                    .child(esp32DeviceId)
                    .child("state");

            esp32DeviceRef.setValue(stateStr)
                .addOnSuccessListener(aVoid -> {
                    runOnUiThread(() -> Toast.makeText(RoomActivity.this,
                        deviceName + " " + stateStr,
                        Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> {
                    runOnUiThread(() -> Toast.makeText(RoomActivity.this,
                        "Failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
                });
        }
    }

    /**
     * Map device names to ESP32 device IDs
     * Updated to handle Light-1, Light-2, Light-3 naming convention
     */
    private String mapDeviceNameToESP32Id(String deviceName) {
        if (deviceName == null) return null;

        String lowerName = deviceName.toLowerCase();

        // Exact match for your specific device names
        if (lowerName.contains("bed") && lowerName.contains("light") && lowerName.contains("1")) {
            return "light1";
        }
        if (lowerName.contains("dining") && lowerName.contains("light") && lowerName.contains("2")) {
            return "light2";
        }
        if (lowerName.contains("washroom") && lowerName.contains("light") && lowerName.contains("3")) {
            return "light3";
        }

        // Pattern matching for "Light-1", "Light-2", "Light-3"
        if (lowerName.matches(".*light[-\\s]*1.*")) {
            return "light1";
        }
        if (lowerName.matches(".*light[-\\s]*2.*")) {
            return "light2";
        }
        if (lowerName.matches(".*light[-\\s]*3.*")) {
            return "light3";
        }

        // Generic room-based mappings
        if (lowerName.contains("living") && lowerName.contains("light")) {
            return "light1";
        } else if (lowerName.contains("bedroom") && lowerName.contains("light")) {
            return "light2";
        } else if (lowerName.contains("living") && lowerName.contains("fan")) {
            return "fan1";
        } else if (lowerName.contains("bedroom") && lowerName.contains("fan")) {
            return "fan2";
        } else if (lowerName.contains("plug") && lowerName.contains("1")) {
            return "plug1";
        } else if (lowerName.contains("plug") && lowerName.contains("2")) {
            return "plug2";
        }

        // Generic fallback mappings
        if (lowerName.contains("light")) {
            return "light1";
        } else if (lowerName.contains("fan")) {
            return "fan1";
        } else if (lowerName.contains("plug")) {
            return "plug1";
        }

        return null;
    }

    void showDeviceOptions(String deviceId, String deviceName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(deviceName);
        builder.setItems(new String[]{"Rename Device", "Delete Device"}, (dialog, which) -> {
            if (which == 0) {
                showRenameDeviceDialog(deviceId, deviceName);
            } else {
                showDeleteDeviceDialog(deviceId, deviceName);
            }
        });
        builder.show();
    }

    void listenToMainSwitch() {
        if (!firebaseReady || userRef == null) {
            return;
        }

        userRef.child("mainSwitch").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String val = snapshot.getValue(String.class);
                isMainSwitchOn = "ON".equals(val);
                android.util.Log.d("RoomActivity", "Main switch state: " + (isMainSwitchOn ? "ON" : "OFF"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e("RoomActivity", "Error reading main switch: " + error.getMessage());
            }
        });
    }

    void showRenameDeviceDialog(String deviceId, String oldName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Device");

        final EditText input = new EditText(this);
        input.setText(oldName);
        input.setHint("New device name");
        input.setPadding(32, 16, 32, 16);
        input.selectAll();
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "Please enter a device name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (roomRef != null) {
                roomRef.child(deviceId).child("name").setValue(newName)
                    .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Device renamed successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to rename device: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    void showDeleteDeviceDialog(String deviceId, String deviceName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Device?");
        builder.setMessage("Are you sure you want to delete '" + deviceName + "'?");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            if (roomRef != null) {
                roomRef.child(deviceId).removeValue()
                    .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Device deleted successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete device: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
