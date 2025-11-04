package com.example.smarthomefull;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class DashboardActivity extends AppCompatActivity {

    LinearLayout roomsContainer;
    FloatingActionButton fab;
    SwitchMaterial mainSwitch;
    TextView welcomeText;
    ImageView profileImage;
    android.widget.Button esp32SettingsButton;
    DatabaseReference userRef;
    DatabaseReference roomsRef;
    String uid;
    boolean firebaseReady = false;
    boolean isMainSwitchChanging = false; // Flag to prevent recursive updates

    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        roomsContainer = findViewById(R.id.roomsContainer);
        fab = findViewById(R.id.fab);
        mainSwitch = findViewById(R.id.mainSwitch);
        welcomeText = findViewById(R.id.welcome);
        profileImage = findViewById(R.id.profile);
        esp32SettingsButton = findViewById(R.id.esp32SettingsButton);

        // Initialize Firebase
        try {
            FirebaseApp app = FirebaseApp.getInstance();
            if (app == null) {
                app = FirebaseApp.initializeApp(this);
            }
            firebaseReady = (app != null);
            android.util.Log.d("DashboardActivity", "Firebase initialized: " + firebaseReady);
        } catch (Exception e) {
            android.util.Log.e("DashboardActivity", "Firebase init error: " + e.getMessage(), e);
            firebaseReady = false;
        }

        FirebaseUser user = null;
        if (firebaseReady) {
            try {
                user = FirebaseAuth.getInstance().getCurrentUser();
            } catch (Exception e) {
                android.util.Log.e("DashboardActivity", "Error getting current user: " + e.getMessage(), e);
            }
        }

        if(user==null){
            // No user - redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        } else {
            uid = user.getUid();
            try {
                userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
                roomsRef = userRef.child("rooms");
                android.util.Log.d("DashboardActivity", "Database refs created successfully");
                android.util.Log.d("DashboardActivity", "Database URL: " + FirebaseDatabase.getInstance().getReference().toString());
            } catch (Exception e) {
                android.util.Log.e("DashboardActivity", "Error creating database refs: " + e.getMessage(), e);
                Toast.makeText(this, "Database connection error. Please check your Firebase setup.", Toast.LENGTH_LONG).show();
            }

            // Display user name
            String displayName = user.getDisplayName();
            String email = user.getEmail();
            if (displayName != null && !displayName.isEmpty()) {
                welcomeText.setText(getString(R.string.welcome_user_name, displayName));
            } else if (email != null) {
                welcomeText.setText(getString(R.string.welcome_user_name, email.split("@")[0]));
            } else {
                welcomeText.setText(getString(R.string.welcome_user_name, "User"));
            }
        }

        // Profile image click - show options
        profileImage.setOnClickListener(v -> showProfileOptions());

        // ESP32 Settings button click
        esp32SettingsButton.setOnClickListener(v ->
            startActivity(new Intent(DashboardActivity.this, ESP32SettingsActivity.class))
        );

        // Load rooms from Firebase
        loadRooms();

        if (firebaseReady && userRef != null) {
            // Main switch sync - listen to server state
            userRef.child("mainSwitch").addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String val = snapshot.getValue(String.class);
                    boolean isOn = "ON".equals(val);

                    // Update UI without triggering the listener
                    mainSwitch.setOnCheckedChangeListener(null);
                    mainSwitch.setChecked(isOn);

                    // Re-attach the listener for user interactions only
                    mainSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        // Only update if user interaction (not programmatic change)
                        if (buttonView.isPressed()) {
                            userRef.child("mainSwitch").setValue(isChecked ? "ON" : "OFF");

                            // Turn off all devices when main switch is OFF
                            if (!isChecked) {
                                turnOffAllDevices();
                            }
                        }
                    });
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {
                    android.util.Log.e("DashboardActivity", "Main switch error: " + error.getMessage());
                }
            });
        } else {
            mainSwitch.setOnCheckedChangeListener(null);
        }

        // FAB - Add new room
        fab.setOnClickListener(v -> {
            if (!firebaseReady || roomsRef == null) {
                Toast.makeText(this, "Firebase not ready. Please restart the app.", Toast.LENGTH_LONG).show();
                android.util.Log.e("DashboardActivity", "Cannot add room: Firebase not ready or roomsRef is null");
            } else {
                showAddRoomDialog();
            }
        });
    }

    void loadRooms() {
        roomsContainer.removeAllViews();

        if (!firebaseReady || roomsRef == null) {
            Toast.makeText(this, "Firebase not ready", Toast.LENGTH_SHORT).show();
            android.util.Log.e("DashboardActivity", "Firebase not ready or roomsRef is null");
            // Show placeholder
            TextView placeholder = new TextView(this);
            placeholder.setText("Unable to connect to database.\nPlease check your internet connection and Firebase setup.");
            placeholder.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            placeholder.setTextSize(16f);
            placeholder.setPadding(16, 32, 16, 32);
            placeholder.setGravity(android.view.Gravity.CENTER);
            roomsContainer.addView(placeholder);
            return;
        }

        android.util.Log.d("DashboardActivity", "Setting up rooms listener for path: " + roomsRef.toString());

        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                android.util.Log.d("DashboardActivity", "onDataChange called. Snapshot exists: " + snapshot.exists() + ", Children count: " + snapshot.getChildrenCount());

                roomsContainer.removeAllViews();

                boolean hasRooms = false;

                for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                    String roomKey = roomSnapshot.getKey();
                    String roomName = roomSnapshot.child("name").getValue(String.class);

                    android.util.Log.d("DashboardActivity", "Found room - Key: " + roomKey + ", Name: " + roomName);

                    if (roomName == null || roomName.isEmpty()) {
                        roomName = roomKey;
                    }

                    hasRooms = true;
                    createRoomCard(roomKey, roomName);
                }

                if (!hasRooms) {
                    android.util.Log.d("DashboardActivity", "No rooms found, showing placeholder");
                    // No rooms - show placeholder
                    TextView placeholder = new TextView(DashboardActivity.this);
                    placeholder.setText("No rooms added yet.\nTap + to add your first room!");
                    placeholder.setTextColor(ContextCompat.getColor(DashboardActivity.this, android.R.color.white));
                    placeholder.setTextSize(16f);
                    placeholder.setPadding(16, 32, 16, 32);
                    placeholder.setGravity(android.view.Gravity.CENTER);
                    roomsContainer.addView(placeholder);
                } else {
                    android.util.Log.d("DashboardActivity", "Displayed " + roomsContainer.getChildCount() + " room(s)");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e("DashboardActivity", "Database error: " + error.getMessage());
                android.util.Log.e("DashboardActivity", "Error code: " + error.getCode());
                android.util.Log.e("DashboardActivity", "Error details: " + error.getDetails());
                Toast.makeText(DashboardActivity.this, "Error loading rooms: " + error.getMessage(),
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    void createRoomCard(String roomKey, String roomName) {
        // Create a simple card using LinearLayout instead of inflating
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(32, 24, 32, 24);
        card.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_blue_light));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);

        // Create text view for room name
        TextView t = new TextView(this);
        t.setText(roomName);
        t.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        t.setTextSize(18f);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        t.setLayoutParams(textParams);

        card.addView(t);

        // Click to enter room
        card.setOnClickListener(v -> {
            Intent i = new Intent(DashboardActivity.this, RoomActivity.class);
            i.putExtra("roomName", roomKey);
            i.putExtra("roomDisplayName", roomName);
            startActivity(i);
        });

        // Long click to edit/delete room
        card.setOnLongClickListener(v -> {
            showRoomOptions(roomKey, roomName);
            return true;
        });

        roomsContainer.addView(card);
    }

    void showAddRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Room");

        final EditText input = new EditText(this);
        input.setHint("Room name (e.g., Living Room)");
        input.setPadding(32, 16, 32, 16);
        builder.setView(input);

        builder.setPositiveButton("Add", null);
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String roomName = input.getText().toString().trim();
                if (roomName.isEmpty()) {
                    Toast.makeText(this, "Please enter a room name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (roomsRef == null) {
                    Toast.makeText(this, "Database not ready. Please restart the app.", Toast.LENGTH_LONG).show();
                    android.util.Log.e("DashboardActivity", "Cannot add room: roomsRef is null");
                    dialog.dismiss();
                    return;
                }

                // Create a clean key from the room name and add timestamp to make it unique
                String roomKey = roomName.replaceAll("[^a-zA-Z0-9]", "");
                if (roomKey.isEmpty()) {
                    roomKey = "Room";
                }
                // Always append timestamp to ensure uniqueness
                roomKey = roomKey + "_" + System.currentTimeMillis();

                android.util.Log.d("DashboardActivity", "Attempting to add room: " + roomName + " with key: " + roomKey);
                android.util.Log.d("DashboardActivity", "Writing to path: " + roomsRef.child(roomKey).child("name").toString());

                // Save the room to Firebase
                roomsRef.child(roomKey).child("name").setValue(roomName)
                    .addOnSuccessListener(aVoid -> {
                        android.util.Log.d("DashboardActivity", "Room added successfully: " + roomName);
                        Toast.makeText(this, "Room added successfully!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("DashboardActivity", "Failed to add room: " + e.getMessage(), e);
                        android.util.Log.e("DashboardActivity", "Exception type: " + e.getClass().getName());
                        Toast.makeText(this, "Failed to add room: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
            });
        });

        dialog.show();
    }

    void showRoomOptions(String roomKey, String roomName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(roomName);
        builder.setItems(new String[]{"Rename Room", "Delete Room"}, (dialog, which) -> {
            if (which == 0) {
                // Rename
                showRenameRoomDialog(roomKey, roomName);
            } else {
                // Delete
                showDeleteRoomDialog(roomKey, roomName);
            }
        });
        builder.show();
    }

    void showRenameRoomDialog(String roomKey, String oldName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Room");

        final EditText input = new EditText(this);
        input.setText(oldName);
        input.setHint("New room name");
        input.setPadding(32, 16, 32, 16);
        input.selectAll();
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "Please enter a room name", Toast.LENGTH_SHORT).show();
                return;
            }

            roomsRef.child(roomKey).child("name").setValue(newName)
                .addOnSuccessListener(aVoid ->
                    Toast.makeText(this, "Room renamed successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to rename room: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    void showDeleteRoomDialog(String roomKey, String roomName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Room?");
        builder.setMessage("Are you sure you want to delete '" + roomName + "' and all its devices?");

        builder.setPositiveButton("Delete", (dialog, which) ->
            roomsRef.child(roomKey).removeValue()
                .addOnSuccessListener(aVoid ->
                    Toast.makeText(this, "Room deleted successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to delete room: " + e.getMessage(), Toast.LENGTH_SHORT).show())
        );

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    void showProfileOptions() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Inflate the custom dialog layout
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_profile, null);

        // Get references to dialog views
        TextView profileName = dialogView.findViewById(R.id.profileName);
        TextView profileEmail = dialogView.findViewById(R.id.profileEmail);
        TextView profileUid = dialogView.findViewById(R.id.profileUid);
        android.widget.Button editNameButton = dialogView.findViewById(R.id.editNameButton);
        android.widget.Button copyUidButton = dialogView.findViewById(R.id.copyUidButton);
        android.widget.Button logoutButton = dialogView.findViewById(R.id.logoutButton);
        android.widget.Button closeButton = dialogView.findViewById(R.id.closeButton);

        // Set user information
        String displayName = user.getDisplayName();
        String email = user.getEmail();
        String userId = user.getUid();

        profileName.setText(displayName != null && !displayName.isEmpty() ? displayName : "Not set");
        profileEmail.setText(email != null ? email : "N/A");
        profileUid.setText(userId);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Edit Name button click
        editNameButton.setOnClickListener(v -> {
            dialog.dismiss();
            showEditNameDialog();
        });

        // Copy UID button click
        copyUidButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("UID", userId);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(DashboardActivity.this, R.string.uid_copied, Toast.LENGTH_SHORT).show();
        });

        // Logout button click
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            finish();
        });

        // Close button click
        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    void showEditNameDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit_name_dialog_title);

        final EditText input = new EditText(this);
        input.setHint(R.string.name_hint);
        input.setText(user.getDisplayName());
        input.setPadding(32, 16, 32, 16);
        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            input.selectAll();
        }
        builder.setView(input);

        builder.setPositiveButton(R.string.save_button, null);
        builder.setNegativeButton(R.string.cancel_button, null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String newName = input.getText().toString().trim();
                if (newName.isEmpty()) {
                    Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update user profile with new name
                com.google.firebase.auth.UserProfileChangeRequest profileUpdates =
                    new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build();

                user.updateProfile(profileUpdates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, R.string.name_updated, Toast.LENGTH_SHORT).show();
                        // Update the welcome text with new name
                        welcomeText.setText(getString(R.string.welcome_user_name, newName));
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update name: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            });
        });

        dialog.show();
    }

    void turnOffAllDevices() {
        if (!firebaseReady || roomsRef == null) {
            android.util.Log.e("DashboardActivity", "Cannot turn off devices: Firebase not ready");
            return;
        }

        android.util.Log.d("DashboardActivity", "Turning off all devices due to main switch OFF");

        // Listen to all rooms and turn off their devices
        roomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                    String roomKey = roomSnapshot.getKey();

                    // Iterate through all devices in this room
                    for (DataSnapshot deviceSnapshot : roomSnapshot.getChildren()) {
                        // Skip the "name" field
                        if ("name".equals(deviceSnapshot.getKey())) {
                            continue;
                        }

                        String deviceKey = deviceSnapshot.getKey();
                        String currentState = deviceSnapshot.child("state").getValue(String.class);

                        // Turn off the device if it's currently ON
                        if ("ON".equals(currentState)) {
                            roomsRef.child(roomKey).child(deviceKey).child("state").setValue("OFF")
                                .addOnSuccessListener(aVoid ->
                                    android.util.Log.d("DashboardActivity", "Turned off device: " + deviceKey + " in room: " + roomKey))
                                .addOnFailureListener(e ->
                                    android.util.Log.e("DashboardActivity", "Failed to turn off device: " + e.getMessage()));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e("DashboardActivity", "Failed to turn off devices: " + error.getMessage());
                Toast.makeText(DashboardActivity.this, "Failed to control devices", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
