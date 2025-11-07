package com.example.smarthomefull;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

/**
 * Background service to sync deviceMapping changes to all room structures
 * This ensures ESP32 physical switch changes update room data even when app is in background
 */
public class DeviceSyncService extends Service {

    private static final String TAG = "DeviceSyncService";
    private DatabaseReference deviceMappingRef;
    private DatabaseReference roomsRef;
    private String uid;
    private ValueEventListener syncListener;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "DeviceSyncService created");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
            deviceMappingRef = FirebaseDatabase.getInstance()
                    .getReference("users").child(uid).child("deviceMapping");
            roomsRef = FirebaseDatabase.getInstance()
                    .getReference("users").child(uid).child("rooms");

            startSync();
        }
    }

    private void startSync() {
        Log.d(TAG, "=== STARTING SYNC SERVICE ===");
        Log.d(TAG, "Listening to path: " + deviceMappingRef.toString());

        // Listen to individual device changes instead of entire mapping
        deviceMappingRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String deviceKey = snapshot.getKey();
                String state = snapshot.child("state").getValue(String.class);
                Log.d(TAG, "=== DEVICE ADDED: " + deviceKey + " = " + state + " ===");
                if (deviceKey != null && state != null) {
                    syncToAllRooms(deviceKey, state);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String deviceKey = snapshot.getKey();
                String state = snapshot.child("state").getValue(String.class);
                Log.d(TAG, "=== DEVICE CHANGED: " + deviceKey + " = " + state + " ===");
                if (deviceKey != null && state != null) {
                    syncToAllRooms(deviceKey, state);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Device removed: " + snapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Not used
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "ChildEventListener error: " + error.getMessage());
            }
        });

        Log.d(TAG, "=== SYNC LISTENER STARTED (ChildEventListener) ===");
    }

    private void syncToAllRooms(String deviceKey, String state) {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Log.d(TAG, "SYNC REQUEST: " + deviceKey + " → " + state);
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (roomsRef == null) {
            Log.e(TAG, "ERROR: roomsRef is null! Cannot sync.");
            return;
        }

        if (uid == null || uid.isEmpty()) {
            Log.e(TAG, "ERROR: uid is null or empty! Cannot sync.");
            return;
        }

        Log.d(TAG, "Querying rooms from path: " + roomsRef.toString());

        // Query rooms every time with fresh data
        roomsRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "FAILED to query rooms!");
                if (task.getException() != null) {
                    Log.e(TAG, "Exception: " + task.getException().getMessage());
                    task.getException().printStackTrace();
                }
                return;
            }

            DataSnapshot roomsSnapshot = task.getResult();
            if (roomsSnapshot == null) {
                Log.e(TAG, "roomsSnapshot is null!");
                return;
            }

            if (!roomsSnapshot.exists()) {
                Log.e(TAG, "No rooms exist in database at path: " + roomsRef.toString());
                return;
            }

            long roomCount = roomsSnapshot.getChildrenCount();
            Log.d(TAG, "Found " + roomCount + " rooms to check");

            if (roomCount == 0) {
                Log.e(TAG, "Room count is 0 - no rooms to sync!");
                return;
            }

            int devicesChecked = 0;
            int matchesFound = 0;

            for (DataSnapshot roomSnapshot : roomsSnapshot.getChildren()) {
                String roomId = roomSnapshot.getKey();
                if (roomId == null) {
                    Log.e(TAG, "Room ID is null, skipping");
                    continue;
                }

                Log.d(TAG, "┌─ Room: " + roomId);
                long deviceCount = roomSnapshot.getChildrenCount();
                Log.d(TAG, "│  Room has " + deviceCount + " children");

                for (DataSnapshot deviceSnapshot : roomSnapshot.getChildren()) {
                    String deviceId = deviceSnapshot.getKey();

                    // Skip room metadata
                    if ("name".equals(deviceId)) {
                        Log.d(TAG, "│  Skipping 'name' metadata field");
                        continue;
                    }

                    devicesChecked++;
                    String deviceName = deviceSnapshot.child("name").getValue(String.class);
                    String currentState = deviceSnapshot.child("state").getValue(String.class);

                    Log.d(TAG, "│  ├─ Device ID: " + deviceId);
                    Log.d(TAG, "│  │   Name: " + deviceName);
                    Log.d(TAG, "│  │   Current State: " + currentState);

                    if (deviceName == null || deviceName.isEmpty()) {
                        Log.d(TAG, "│  │   ✗ Device name is null/empty, skipping");
                        continue;
                    }

                    String mappedKey = mapDeviceNameToESP32Id(deviceName);
                    Log.d(TAG, "│  │   Mapped to: '" + mappedKey + "'");
                    Log.d(TAG, "│  │   Comparing: '" + mappedKey + "' == '" + deviceKey + "'");

                    if (deviceKey.equals(mappedKey)) {
                        matchesFound++;
                        Log.d(TAG, "│  │   ✓✓✓ MATCH FOUND!");
                        Log.d(TAG, "│  │   Need to update: " + currentState + " → " + state);

                        // Check if update is actually needed
                        if (state.equals(currentState)) {
                            Log.d(TAG, "│  │   State already matches, skipping write");
                            continue;
                        }

                        // Build the exact Firebase path
                        String fullPath = "users/" + uid + "/rooms/" + roomId + "/" + deviceId + "/state";
                        Log.d(TAG, "│  │   Full path: " + fullPath);
                        Log.d(TAG, "│  │   Attempting Firebase write...");

                        // Perform the write with detailed callbacks
                        FirebaseDatabase.getInstance()
                                .getReference()
                                .child("users")
                                .child(uid)
                                .child("rooms")
                                .child(roomId)
                                .child(deviceId)
                                .child("state")
                                .setValue(state)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "│  │   ✓✓✓✓ WRITE SUCCESS!");
                                    Log.d(TAG, "│  │   Successfully wrote '" + state + "' to " + fullPath);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "│  │   ✗✗✗✗ WRITE FAILED!");
                                    Log.e(TAG, "│  │   Path: " + fullPath);
                                    Log.e(TAG, "│  │   Error: " + e.getMessage());
                                    Log.e(TAG, "│  │   Exception class: " + e.getClass().getName());
                                    e.printStackTrace();
                                })
                                .addOnCompleteListener(writeTask -> {
                                    if (writeTask.isSuccessful()) {
                                        Log.d(TAG, "│  │   Write task completed successfully");
                                    } else {
                                        Log.e(TAG, "│  │   Write task completed with errors");
                                    }
                                });

                        Log.d(TAG, "│  │   Write command sent to Firebase");

                    } else {
                        Log.d(TAG, "│  │   ✗ No match: '" + mappedKey + "' ≠ '" + deviceKey + "'");
                    }
                }
                Log.d(TAG, "└─ End Room: " + roomId);
            }

            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            Log.d(TAG, "SYNC COMPLETE:");
            Log.d(TAG, "  Devices checked: " + devicesChecked);
            Log.d(TAG, "  Matches found: " + matchesFound);
            Log.d(TAG, "  (Write callbacks will appear above)");
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Critical error querying rooms!");
            Log.e(TAG, "Error: " + e.getMessage());
            e.printStackTrace();
        });
    }

    /**
     * Map device names to ESP32 device IDs - MUST MATCH RoomActivity mapping
     * This is the critical function that determines which room devices match ESP32 IDs
     */
    private String mapDeviceNameToESP32Id(String deviceName) {
        if (deviceName == null) return "unknown";

        String lowerName = deviceName.toLowerCase().trim();
        Log.d(TAG, "      Mapping device name: '" + deviceName + "' (lowercase: '" + lowerName + "')");

        // Direct ESP32 ID format matches (e.g., device already named "light1")
        if (lowerName.matches("^(light|fan|plug)[0-9]+$")) {
            Log.d(TAG, "      → Direct match: " + lowerName);
            return lowerName;
        }

        // Pattern matching for "Light-1", "Light 1", "Light1"
        if (lowerName.matches(".*light[-\\s]*1.*")) {
            Log.d(TAG, "      → Matched pattern light-1 → light1");
            return "light1";
        }
        if (lowerName.matches(".*light[-\\s]*2.*")) {
            Log.d(TAG, "      → Matched pattern light-2 → light2");
            return "light2";
        }
        if (lowerName.matches(".*light[-\\s]*3.*")) {
            Log.d(TAG, "      → Matched pattern light-3 → light3");
            return "light3";
        }

        // Room-specific name matching
        if (lowerName.contains("bed") && lowerName.contains("light")) {
            Log.d(TAG, "      → Bed room light → light1");
            return "light1";
        }
        if (lowerName.contains("dining") && lowerName.contains("light")) {
            Log.d(TAG, "      → Dining room light → light2");
            return "light2";
        }
        if (lowerName.contains("wash") && lowerName.contains("light")) {
            Log.d(TAG, "      → Washroom light → light3");
            return "light3";
        }

        // Fan matching
        if (lowerName.matches(".*fan[-\\s]*1.*") || (lowerName.contains("main") && lowerName.contains("fan"))) {
            Log.d(TAG, "      → Matched fan1");
            return "fan1";
        }
        if (lowerName.matches(".*fan[-\\s]*2.*")) {
            Log.d(TAG, "      → Matched fan2");
            return "fan2";
        }

        // Plug matching
        if (lowerName.matches(".*plug[-\\s]*1.*")) {
            Log.d(TAG, "      → Matched plug1");
            return "plug1";
        }

        // Generic fallback - first of type
        if (lowerName.contains("light")) {
            Log.d(TAG, "      → Generic light fallback → light1");
            return "light1";
        }
        if (lowerName.contains("fan")) {
            Log.d(TAG, "      → Generic fan fallback → fan1");
            return "fan1";
        }
        if (lowerName.contains("plug")) {
            Log.d(TAG, "      → Generic plug fallback → plug1");
            return "plug1";
        }

        Log.d(TAG, "      → No match found, returning: " + lowerName.replaceAll("[^a-z0-9]", "_"));
        return lowerName.replaceAll("[^a-z0-9]", "_");
    }

    @Override
    public void onDestroy() {
        if (deviceMappingRef != null && syncListener != null) {
            deviceMappingRef.removeEventListener(syncListener);
            Log.d(TAG, "Sync listener removed");
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
