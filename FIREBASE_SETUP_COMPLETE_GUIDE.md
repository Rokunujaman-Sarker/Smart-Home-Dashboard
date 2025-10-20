# Complete Firebase Setup Guide for Android Smart Home App

## 📋 Table of Contents
1. [Create Firebase Project](#step-1-create-firebase-project)
2. [Add Android App to Firebase](#step-2-add-android-app-to-firebase)
3. [Download and Add google-services.json](#step-3-download-google-servicesjson)
4. [Configure Gradle Files](#step-4-configure-gradle-files)
5. [Enable Firebase Authentication](#step-5-enable-firebase-authentication)
6. [Enable Realtime Database](#step-6-enable-realtime-database)
7. [Set Database Rules](#step-7-set-database-rules)
8. [Verify Setup](#step-8-verify-setup)
9. [Troubleshooting](#troubleshooting)

---

## Step 1: Create Firebase Project

### 1.1 Go to Firebase Console
- Open your browser and go to: **https://console.firebase.google.com/**
- Sign in with your Google account

### 1.2 Create New Project
1. Click **"Add project"** or **"Create a project"**
2. Enter project name: `smart-home-app` (or any name you prefer)
3. Click **"Continue"**

### 1.3 Google Analytics (Optional)
1. You can choose to **enable or disable** Google Analytics
2. If enabled, select or create an Analytics account
3. Click **"Create project"**
4. Wait for Firebase to set up your project (30-60 seconds)
5. Click **"Continue"** when ready

---

## Step 2: Add Android App to Firebase

### 2.1 Register Your App
1. In the Firebase Console, click the **Android icon** to add an Android app
2. You'll see a form with several fields:

### 2.2 Fill in App Details

**Android package name:** (REQUIRED)
```
com.example.smarthomefull
```
> ⚠️ **Important:** This must match the `applicationId` in your `app/build.gradle` file

To verify your package name:
- Open: `app/build.gradle`
- Look for: `applicationId "com.example.smarthomefull"`

**App nickname:** (Optional)
```
Smart Home App
```

**Debug signing certificate SHA-1:** (Optional, but recommended for Google Sign-In)

To get your SHA-1:
1. Open Terminal in Android Studio
2. Run this command:

**For Windows:**
```bash
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

**For Mac/Linux:**
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

3. Copy the **SHA-1** value and paste it into Firebase
4. Click **"Register app"**

---

## Step 3: Download google-services.json

### 3.1 Download the Configuration File
1. After registering, Firebase will show a **"Download google-services.json"** button
2. Click to download the file
3. **Save it somewhere you can find it**

### 3.2 Add to Your Project
1. In Android Studio, switch to **Project** view (dropdown at top-left)
2. Navigate to: `YourProject/app/` folder
3. **Drag and drop** or **copy** the `google-services.json` file into the `app/` folder
4. The file should be at: `app/google-services.json` (same level as `app/build.gradle`)

### 3.3 Verify Placement
Your project structure should look like:
```
smart-home-app/
├── app/
│   ├── google-services.json  ← File should be here
│   ├── build.gradle
│   ├── src/
│   └── ...
├── build.gradle
└── settings.gradle
```

---

## Step 4: Configure Gradle Files

### 4.1 Project-Level build.gradle

Open: `build.gradle` (Project: your_project_name)

Find the `buildscript` section and add the Google services classpath:

```gradle
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.1.0'  // Your version may vary
        classpath 'com.google.gms:google-services:4.4.0'  // ← ADD THIS LINE
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
```

**Important Notes:**
- If you don't have an `allprojects` block, that's okay (newer Android projects don't need it)
- Use the latest version of google-services plugin: Check at https://firebase.google.com/docs/android/setup

### 4.2 App-Level build.gradle

Open: `app/build.gradle`

#### 4.2.1 Apply the Google Services Plugin
At the **TOP** of the file (after `plugins` block or first line):

```gradle
plugins {
    id 'com.android.application'
    // ...other plugins
}

// ADD THIS LINE
apply plugin: 'com.google.gms.google-services'
```

**OR** at the **BOTTOM** of the file:

```gradle
// All your existing code...

// ADD THIS AT THE VERY BOTTOM
apply plugin: 'com.google.gms.google-services'
```

#### 4.2.2 Add Firebase Dependencies

In the `dependencies` section, add Firebase libraries:

```gradle
dependencies {
    // Existing dependencies...
    implementation 'androidx.appcompat:appcompat:1.6.1'
    // ... other dependencies

    // Firebase BoM (Bill of Materials) - manages versions
    implementation platform('com.google.firebase:firebase-bom:32.5.0')
    
    // Firebase Authentication
    implementation 'com.google.firebase:firebase-auth'
    
    // Firebase Realtime Database
    implementation 'com.google.firebase:firebase-database'
    
    // (Optional) Firebase Analytics
    implementation 'com.google.firebase:firebase-analytics'
}
```

**Using Firebase BoM (Recommended):**
- The BoM automatically manages Firebase library versions
- You don't need to specify versions for individual Firebase libraries
- Latest BoM version (as of Oct 2025): 32.5.0 or newer

**Alternative (Without BoM):**
```gradle
implementation 'com.google.firebase:firebase-auth:22.3.0'
implementation 'com.google.firebase:firebase-database:20.3.0'
```

### 4.3 Sync Gradle
1. Click **"Sync Now"** at the top of the screen
2. Wait for Gradle to sync (may take 1-2 minutes)
3. Check for errors in the **Build** tab at the bottom

---

## Step 5: Enable Firebase Authentication

### 5.1 Open Authentication
1. In Firebase Console, select your project
2. In the left sidebar, click **"Build"** → **"Authentication"**
3. Click **"Get started"**

### 5.2 Enable Sign-In Methods

#### For Email/Password Authentication:
1. Click the **"Sign-in method"** tab
2. Find **"Email/Password"** in the list
3. Click on it
4. Toggle **"Enable"** to ON
5. Click **"Save"**

#### (Optional) Enable Google Sign-In:
1. Click **"Google"** in the sign-in providers list
2. Toggle **"Enable"** to ON
3. Select a **Support email** (your email)
4. Click **"Save"**

### 5.3 Create Test User (Optional)
1. Click the **"Users"** tab
2. Click **"Add user"**
3. Enter:
   - Email: `test@example.com`
   - Password: `test123456`
4. Click **"Add user"**

---

## Step 6: Enable Realtime Database

### 6.1 Create Database
1. In Firebase Console, click **"Build"** → **"Realtime Database"**
2. Click **"Create Database"**

### 6.2 Choose Database Location
1. Select a location close to your users:
   - **United States:** `us-central1`
   - **Europe:** `europe-west1`
   - **Asia:** `asia-southeast1`
2. Click **"Next"**

### 6.3 Set Security Rules
You'll see two options:

**Option 1: Start in locked mode** (Recommended for production)
- Only authenticated users can read/write
- Choose this for now

**Option 2: Start in test mode**
- Anyone can read/write (NOT secure)
- Only use for quick testing

Select **"Start in locked mode"** and click **"Enable"**

### 6.4 Note Your Database URL
Your database URL will look like:
```
https://smart-home-app-xxxxx-default-rtdb.firebaseio.com/
```
You'll need this if you're using a non-default database region.

---

## Step 7: Set Database Rules

### 7.1 Open Rules Editor
1. In Realtime Database, click the **"Rules"** tab
2. You'll see a JSON editor

### 7.2 Set Up Secure Rules

Replace the existing rules with this:

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

**What this does:**
- Each user can only read/write their own data at `users/{their-uid}/`
- Prevents users from accessing other users' data
- Requires authentication

### 7.3 Publish Rules
1. Click **"Publish"** button
2. Confirm the changes

### 7.4 Test Rules (Optional)
You can test rules in the Firebase Console:
1. Click the **"Rules Playground"** tab
2. Try simulated read/write operations
3. Verify they work as expected

---

## Step 8: Verify Setup

### 8.1 Check google-services.json
1. Open `app/google-services.json` in Android Studio
2. Verify it contains:
   - Your project ID
   - Your package name: `com.example.smarthomefull`
   - API keys
   - Database URL

Example structure:
```json
{
  "project_info": {
    "project_number": "123456789",
    "project_id": "smart-home-app-xxxxx",
    "storage_bucket": "smart-home-app-xxxxx.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:123456789:android:abc123...",
        "android_client_info": {
          "package_name": "com.example.smarthomefull"
        }
      }
    }
  ]
}
```

### 8.2 Verify Gradle Sync
1. Check that Gradle synced without errors
2. Build menu → Clean Project
3. Build menu → Rebuild Project

### 8.3 Run the App
1. Connect your device or start an emulator
2. Click **Run** (green play button)
3. The app should launch without Firebase errors

### 8.4 Test Firebase Connection

Add this test code temporarily in your `DashboardActivity.onCreate()`:

```java
// Test Firebase connection
FirebaseDatabase.getInstance().getReference(".info/connected")
    .addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            boolean connected = snapshot.getValue(Boolean.class);
            if (connected) {
                android.util.Log.d("Firebase", "✅ Connected to Firebase!");
                Toast.makeText(DashboardActivity.this, 
                    "Firebase Connected!", Toast.LENGTH_SHORT).show();
            } else {
                android.util.Log.d("Firebase", "❌ Disconnected from Firebase");
            }
        }
        
        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            android.util.Log.e("Firebase", "Connection test failed: " + error.getMessage());
        }
    });
```

Run the app and check Logcat for the "✅ Connected to Firebase!" message.

---

## Step 9: Set Up Database Structure

### 9.1 Understanding Your Data Structure

Your app uses this structure:
```
users/
  └── {user_uid}/
       ├── mainSwitch: "ON" or "OFF"
       └── rooms/
            ├── LivingRoom_1234567890/
            │    ├── name: "Living Room"
            │    ├── device1_id/
            │    │    ├── name: "Light"
            │    │    ├── type: "Light"
            │    │    └── state: "ON"
            │    └── device2_id/
            │         ├── name: "Fan"
            │         ├── type: "Fan"
            │         └── state: "OFF"
            └── Bedroom_1234567891/
                 └── name: "Bedroom"
```

### 9.2 Manually Add Test Data (Optional)

1. In Firebase Console → Realtime Database → Data tab
2. Click **"+"** next to the database name
3. Add structure:
   - Name: `users`
   - Click **"+"** to add child
   - Name: `{paste-your-user-uid}`
   - Add children: `mainSwitch`, `rooms`

To find your user UID:
- Go to Authentication → Users tab
- Copy the UID of your test user

---

## Troubleshooting

### ❌ Error: "google-services.json not found"
**Solution:**
- Verify `google-services.json` is in `app/` folder (not in `src/`)
- Sync Gradle again
- Clean and Rebuild project

### ❌ Error: "Default FirebaseApp is not initialized"
**Solution:**
- Check `google-services.json` is present
- Verify `apply plugin: 'com.google.gms.google-services'` is in `app/build.gradle`
- Check package name matches in both `google-services.json` and `build.gradle`

### ❌ Error: "Failed to get FirebaseDatabase instance: Specify DatabaseURL"
**Solution:**
If your database is not in the US region, add this in your code:
```java
FirebaseDatabase database = FirebaseDatabase.getInstance("https://your-project-default-rtdb.firebaseio.com/");
```

### ❌ Error: "Permission denied"
**Solution:**
- Go to Firebase Console → Realtime Database → Rules
- Verify rules allow authenticated users to read/write
- Make sure user is signed in (`FirebaseAuth.getInstance().getCurrentUser()` is not null)

### ❌ Error: "buildToolsVersion is not specified"
**Solution:**
Add to `app/build.gradle`:
```gradle
android {
    buildToolsVersion "34.0.0"
    // ... rest of config
}
```

### ❌ Rooms not showing after adding
**Checklist:**
1. Check Logcat for errors
2. Verify user is authenticated
3. Check Firebase Console → Realtime Database → Data - do you see the room?
4. Verify database rules allow reading
5. Check that `roomsRef` path is correct: `users/{uid}/rooms`

### ❌ App crashes on startup
**Solution:**
1. Check Logcat for the error stack trace
2. Verify all Firebase dependencies are added
3. Make sure you're using compatible versions
4. Try invalidating caches: File → Invalidate Caches → Invalidate and Restart

---

## Quick Reference: All Required Files

### ✅ File 1: `google-services.json`
**Location:** `app/google-services.json`
**Source:** Downloaded from Firebase Console

### ✅ File 2: Project-level `build.gradle`
**Location:** `build.gradle` (Project level)
```gradle
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.4.0'
    }
}
```

### ✅ File 3: App-level `build.gradle`
**Location:** `app/build.gradle`
```gradle
plugins {
    id 'com.android.application'
}

apply plugin: 'com.google.gms.google-services'

android {
    // ... config
}

dependencies {
    implementation platform('com.google.firebase:firebase-bom:32.5.0')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-database'
}
```

---

## Testing Your Setup

### Test 1: Authentication
1. Run the app
2. Register a new user
3. Check Firebase Console → Authentication → Users
4. You should see the new user listed

### Test 2: Add a Room
1. Sign in to the app
2. Tap the + button
3. Enter "Living Room"
4. Tap "Add"
5. Check Firebase Console → Realtime Database → Data
6. Navigate to `users/{uid}/rooms/`
7. You should see `LivingRoom_xxxxx/name: "Living Room"`

### Test 3: Add a Device
1. Tap on a room
2. Tap the + button in the room
3. Enter device name: "Light"
4. Select device type
5. Tap "Add"
6. Check Firebase Console - device should appear under the room

---

## Best Practices

### Security
✅ Always use authentication
✅ Set proper database rules
✅ Don't expose API keys in version control (add `google-services.json` to `.gitignore` if needed)
✅ Use environment-specific Firebase projects (dev, staging, production)

### Performance
✅ Use `.indexOn` rules for queries
✅ Limit data fetching with `.limitToLast()` or `.limitToFirst()`
✅ Remove listeners when not needed (`removeEventListener()`)
✅ Use `.setPersistenceEnabled(true)` for offline support

### Development
✅ Use Logcat to debug Firebase operations
✅ Test on real devices (not just emulator)
✅ Monitor usage in Firebase Console
✅ Check Firebase Status page for outages: https://status.firebase.google.com/

---

## Additional Resources

- **Firebase Documentation:** https://firebase.google.com/docs/android/setup
- **Realtime Database Guide:** https://firebase.google.com/docs/database/android/start
- **Authentication Guide:** https://firebase.google.com/docs/auth/android/start
- **Firebase Console:** https://console.firebase.google.com/
- **Sample Apps:** https://github.com/firebase/quickstart-android

---

## Need Help?

If you encounter issues:

1. **Check Logcat** - Filter by "Firebase" or your activity name
2. **Firebase Console** - Check data, rules, and authentication
3. **Stack Overflow** - Search for specific error messages
4. **Firebase Support** - https://firebase.google.com/support

---

**Setup Complete! 🎉**

Your Firebase backend is now configured and ready to use. Your app can now:
- ✅ Authenticate users
- ✅ Store room data in Realtime Database
- ✅ Store device data
- ✅ Sync in real-time across devices

