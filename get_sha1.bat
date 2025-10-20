@echo off
echo ========================================
echo Getting SHA-1 Certificate Fingerprint
echo ========================================
echo.

set KEYSTORE_PATH=%USERPROFILE%\.android\debug.keystore

if not exist "%KEYSTORE_PATH%" (
    echo ERROR: Debug keystore not found!
    echo Location checked: %KEYSTORE_PATH%
    echo.
    echo The keystore will be created when you run the app from Android Studio.
    echo Please run the app at least once, then run this script again.
    pause
    exit /b
)

echo Found debug keystore at: %KEYSTORE_PATH%
echo.
echo Extracting SHA-1 fingerprint...
echo.
echo ========================================

keytool -list -v -keystore "%KEYSTORE_PATH%" -alias androiddebugkey -storepass android -keypass android | findstr "SHA1"

echo ========================================
echo.
echo INSTRUCTIONS:
echo 1. Copy the SHA-1 value shown above (the part after "SHA1:")
echo 2. Go to: https://console.firebase.google.com/
echo 3. Open project: smart-home-dashboard-407-8db57
echo 4. Click Settings (gear icon) - Project settings
echo 5. Scroll to "Your apps" section
echo 6. Find: com.example.smarthomefull
echo 7. Click "Add fingerprint"
echo 8. Paste the SHA-1 and Save
echo 9. Download the NEW google-services.json
echo 10. Replace app/google-services.json with the new file
echo 11. Clean and rebuild your project
echo.
pause

