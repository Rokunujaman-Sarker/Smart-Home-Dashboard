package com.example.smarthomefull;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            // Initialize Firebase if not already initialized
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
            }

            // Get the database instance and configure it
            FirebaseDatabase database = FirebaseDatabase.getInstance();

            // Enable offline persistence
            database.setPersistenceEnabled(true);

            // Enable detailed logging for debugging
            FirebaseDatabase.getInstance().setLogLevel(com.google.firebase.database.Logger.Level.DEBUG);

            Log.d(TAG, "Firebase initialized successfully with persistence enabled");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
        }
    }
}
