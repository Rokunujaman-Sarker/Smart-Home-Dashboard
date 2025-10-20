# Fix Google Sign-In Error 10 (DEVELOPER_ERROR)

## Problem
Google Sign-In fails with error code 10, which means the Firebase project is missing the Android OAuth client configuration with SHA-1 certificate.

## Solution: Add SHA-1 Certificate to Firebase

### Step 1: Get Your SHA-1 Certificate

Open Command Prompt (cmd.exe) and run this command:

```cmd
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

**Look for the SHA-1 line in the output:**
```
Certificate fingerprints:
SHA1: AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD
```

**Copy the SHA-1 value** (the part after "SHA1:")

### Step 2: Add SHA-1 to Firebase Console

1. Go to **Firebase Console**: https://console.firebase.google.com/
2. Select your project: **smart-home-dashboard-407-8db57**
3. Click the **gear icon** ⚙️ (Settings) → **Project settings**
4. Scroll down to **"Your apps"** section
5. Find your Android app: **com.example.smarthomefull**
6. Click **"Add fingerprint"** button
7. **Paste your SHA-1** certificate
8. Click **"Save"**

### Step 3: Download New google-services.json

**Important:** After adding the SHA-1, Firebase generates a new configuration file.

1. In the same **Project settings** page
2. Scroll to your Android app
3. Click **"google-services.json"** button to download
4. **Replace** the old file in your project:
   - Location: `app/google-services.json`
   - The new file will include the Android OAuth client

### Step 4: Verify the New Configuration

Open the new `google-services.json` file and look for an entry like this:

```json
"oauth_client": [
  {
    "client_id": "YOUR_PROJECT-xxxxx.apps.googleusercontent.com",
    "client_type": 1,
    "android_info": {
      "package_name": "com.example.smarthomefull",
      "certificate_hash": "YOUR_SHA1_HERE"
    }
  },
  {
    "client_id": "YOUR_PROJECT-xxxxx.apps.googleusercontent.com",
    "client_type": 3
  }
]
```

**You should now see TWO entries:**
- `client_type: 1` (Android) ← This was missing!
- `client_type: 3` (Web)

### Step 5: Clean and Rebuild

After replacing the file:

1. In Android Studio: **Build → Clean Project**
2. Then: **Build → Rebuild Project**
3. Uninstall the app from your device/emulator
4. Run the app again

### Step 6: Test Google Sign-In

1. Open the app
2. Click **"Sign in with Google"**
3. Select your Google account
4. Sign-in should now work! ✅

---

## Additional Notes

### For Release/Production Builds

When you're ready to publish your app, you'll also need to add the **release SHA-1**:

```cmd
keytool -list -v -keystore "path\to\your\release.keystore" -alias your_alias_name
```

Add this SHA-1 to Firebase following the same steps above.

### Common Mistakes

1. ❌ Not replacing the old google-services.json file
2. ❌ Placing the file in the wrong location (must be in `app/` folder)
3. ❌ Not cleaning/rebuilding after replacing the file
4. ❌ Testing with old app installation (must reinstall)

### Why This Happens

Google Sign-In requires the app's signing certificate to be registered in Firebase for security. Without it:
- Firebase can't verify your app
- Google Sign-In returns error code 10 (DEVELOPER_ERROR)
- The authentication process fails

---

## Troubleshooting

### Can't Find debug.keystore?

If the keytool command says "keystore not found", run the app once in Android Studio first. This will automatically generate the debug keystore.

### Still Getting Error 10?

1. Make sure you downloaded the NEW google-services.json AFTER adding SHA-1
2. Completely uninstall the app from your device
3. Clean project and rebuild
4. Run the app fresh

### Multiple Developers?

Each developer needs to add their debug SHA-1 to Firebase. You can add multiple SHA-1 fingerprints.

---

## Quick Checklist

- [ ] Got SHA-1 from debug keystore
- [ ] Added SHA-1 to Firebase Console
- [ ] Downloaded NEW google-services.json
- [ ] Replaced old google-services.json in app/ folder
- [ ] Cleaned and rebuilt project
- [ ] Uninstalled old app
- [ ] Tested Google Sign-In

---

**After completing these steps, Google Sign-In should work perfectly!** 🎉

