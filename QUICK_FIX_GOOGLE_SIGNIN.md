# QUICK FIX GUIDE - Google Sign-In Error 10

## ⚠️ Problem
You're seeing "Google sign-in failed: 10" because Firebase doesn't have your app's SHA-1 certificate.

## ✅ SOLUTION - Follow These Steps

### STEP 1: Get Your SHA-1 Certificate

**Double-click this file:** `get_sha1.bat` (in this same folder)

This will show your SHA-1 certificate. It looks like:
```
SHA1: AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD
```

**COPY the entire SHA-1 value** (the part after "SHA1:")

---

### STEP 2: Add SHA-1 to Firebase

1. Open your browser and go to: **https://console.firebase.google.com/**

2. Click on your project: **smart-home-dashboard-407-8db57**

3. Click the **gear icon ⚙️** (top-left) → Select **"Project settings"**

4. Scroll down to **"Your apps"** section

5. You'll see your Android app: **com.example.smarthomefull**

6. Click the **"Add fingerprint"** button

7. **Paste your SHA-1** certificate that you copied in Step 1

8. Click **"Save"**

---

### STEP 3: Download NEW google-services.json

**⚠️ CRITICAL: You MUST download a new file after adding SHA-1!**

1. Still in **Project settings** → **Your apps** section

2. Look for the **"google-services.json"** download button/link

3. Click it to download the **NEW** configuration file

4. The new file will be downloaded to your Downloads folder

---

### STEP 4: Replace the Old File

1. Find the downloaded `google-services.json` in your Downloads folder

2. Copy it to replace the old file at:
   ```
   C:\Users\User\Desktop\Smart Home Dashboard\app\google-services.json
   ```

3. When asked, choose **"Replace"** the existing file

**Verify the new file has Android OAuth client:**
- Open the new `google-services.json` file
- Look for `"oauth_client"` section
- You should see TWO entries:
  - One with `"client_type": 1` (Android) ← **This is new!**
  - One with `"client_type": 3` (Web)

---

### STEP 5: Clean & Rebuild in Android Studio

1. In Android Studio menu: **Build → Clean Project**
2. Wait for it to finish
3. Then: **Build → Rebuild Project**
4. Wait for the rebuild to complete

---

### STEP 6: Reinstall the App

**Important:** You must completely uninstall the old app!

**On your Android device/emulator:**
1. Long-press the app icon
2. Select **"Uninstall"** or drag to uninstall
3. Confirm uninstall

**Then run from Android Studio:**
1. Click the green **Run** button (or press Shift+F10)
2. Wait for the app to install and launch

---

### STEP 7: Test Google Sign-In

1. Open the app (should show Registration/Login screen)
2. Click **"SIGN UP WITH GOOGLE"** or **"Sign in with Google"**
3. Select your Google account
4. ✅ **It should work now!** You'll be signed in successfully

---

## 📋 Quick Checklist

- [ ] Ran `get_sha1.bat` and copied SHA-1 certificate
- [ ] Added SHA-1 to Firebase Console
- [ ] Downloaded NEW google-services.json
- [ ] Replaced old google-services.json in app/ folder
- [ ] Cleaned Project in Android Studio
- [ ] Rebuilt Project in Android Studio
- [ ] Uninstalled old app from device
- [ ] Ran app fresh from Android Studio
- [ ] Tested Google Sign-In

---

## 🔍 Verify It Worked

After following all steps, the new error message will tell you if there's still a problem:
- ❌ If you still see error 10 → You didn't replace the google-services.json file
- ✅ If it works → You're all set!

---

## ❓ Troubleshooting

**Q: I don't see the "Add fingerprint" button**
A: Make sure you're in Project Settings → Your apps section. Scroll down past the general info.

**Q: I added SHA-1 but still getting error 10**
A: Did you download the NEW google-services.json AFTER adding SHA-1? This is critical!

**Q: How do I know if I have the new file?**
A: Open google-services.json and search for `"client_type": 1` - it should exist now.

**Q: The keytool command doesn't work**
A: Make sure you've run the app at least once from Android Studio. This creates the debug keystore.

---

## 📞 Need More Help?

See the detailed guide: `FIX_GOOGLE_SIGNIN_ERROR_10.md`

---

**Once you complete these steps, Google Sign-In will work perfectly! 🎉**

