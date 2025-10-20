# Keep Firebase models and data classes (generic safeguard)
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep Google Play Services auth classes
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

