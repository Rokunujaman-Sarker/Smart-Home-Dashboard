package com.example.smarthomefull;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegistrationActivity extends AppCompatActivity {

    EditText fullName, emailRegister, passwordRegister, confirmPassword;
    CheckBox termsCheckbox;
    Button registerButton, googleRegisterBtn;
    TextView loginLink;
    FirebaseAuth mAuth;
    GoogleSignInClient googleSignInClient;
    boolean firebaseReady = false;

    // Activity Result launcher for Google sign-in
    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (!firebaseReady) return;

                Intent data = result.getData();
                if (data == null) {
                    showToast("Google sign-up canceled");
                    return;
                }

                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account == null || TextUtils.isEmpty(account.getIdToken())) {
                        showToastLong("Missing ID token; check Firebase Google Sign-In setup");
                        return;
                    }
                    setUiEnabled(false);
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    mAuth.signInWithCredential(credential).addOnCompleteListener(task1 -> {
                        setUiEnabled(true);
                        if (task1.isSuccessful()) {
                            showToast("Google sign-up successful!");
                            goToDashboard();
                        } else {
                            String msg = task1.getException() != null ? task1.getException().getMessage() : "Google auth failed";
                            Log.e("RegistrationActivity", "Firebase signInWithCredential failed", task1.getException());
                            showToastLong("Google auth failed: " + msg);
                        }
                    });
                } catch (ApiException e) {
                    Log.e("RegistrationActivity", "Google sign-in failed", e);
                    String errorMsg;
                    if (e.getStatusCode() == 10) {
                        errorMsg = "Google Sign-In setup incomplete. Please add SHA-1 certificate to Firebase Console. See FIX_GOOGLE_SIGNIN_ERROR_10.md for instructions.";
                    } else {
                        errorMsg = "Google sign-in failed: " + e.getStatusCode();
                    }
                    showToastLong(errorMsg);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize views
        fullName = findViewById(R.id.fullName);
        emailRegister = findViewById(R.id.emailRegister);
        passwordRegister = findViewById(R.id.passwordRegister);
        confirmPassword = findViewById(R.id.confirmPassword);
        termsCheckbox = findViewById(R.id.termsCheckbox);
        registerButton = findViewById(R.id.registerButton);
        googleRegisterBtn = findViewById(R.id.googleRegisterBtn);
        loginLink = findViewById(R.id.loginLink);

        // Initialize Firebase
        try {
            FirebaseApp app = FirebaseApp.initializeApp(getApplicationContext());
            if (app == null) {
                try {
                    mAuth = FirebaseAuth.getInstance();
                    firebaseReady = true;
                } catch (Throwable t) {
                    firebaseReady = false;
                }
            } else {
                mAuth = FirebaseAuth.getInstance();
                firebaseReady = true;
            }
        } catch (Throwable t) {
            Log.e("RegistrationActivity", "Firebase init failed", t);
            firebaseReady = false;
        }

        if (!firebaseReady) {
            setUiEnabled(false);
            showToastLong("Firebase not configured. Add google-services.json to enable registration.");
            return;
        }

        // Check if already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && (currentUser.isEmailVerified() || currentUser.getProviderData().size() > 1)) {
            goToDashboard();
            return;
        }

        // Setup Google Sign-In
        String webClientId = getWebClientIdOrNull();
        if (webClientId == null) {
            googleRegisterBtn.setEnabled(false);
            googleRegisterBtn.setAlpha(0.6f);
            googleRegisterBtn.setOnClickListener(null);
        } else {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build();
            googleSignInClient = GoogleSignIn.getClient(this, gso);
            googleRegisterBtn.setEnabled(true);
            googleRegisterBtn.setAlpha(1f);
            googleRegisterBtn.setOnClickListener(v -> googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));
        }

        // Register button click
        registerButton.setOnClickListener(v -> handleRegistration());

        // Login link click
        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void handleRegistration() {
        if (!firebaseReady) {
            showToast("Firebase not ready");
            return;
        }

        // Get input values
        String name = fullName.getText().toString().trim();
        String email = emailRegister.getText().toString().trim();
        String password = passwordRegister.getText().toString().trim();
        String confirmPass = confirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            fullName.setError("Name is required");
            fullName.requestFocus();
            return;
        }

        if (name.length() < 2) {
            fullName.setError("Name must be at least 2 characters");
            fullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailRegister.setError("Email is required");
            emailRegister.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailRegister.setError("Enter a valid email");
            emailRegister.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordRegister.setError("Password is required");
            passwordRegister.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordRegister.setError("Password must be at least 6 characters");
            passwordRegister.requestFocus();
            return;
        }

        if (!password.equals(confirmPass)) {
            confirmPassword.setError("Passwords do not match");
            confirmPassword.requestFocus();
            return;
        }

        if (!termsCheckbox.isChecked()) {
            showToast("Please accept the terms and conditions");
            return;
        }

        // All validations passed - proceed with registration
        setUiEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Set display name
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        // Send verification email
                                        user.sendEmailVerification()
                                                .addOnCompleteListener(emailTask -> {
                                                    setUiEnabled(true);
                                                    if (emailTask.isSuccessful()) {
                                                        showToastLong("Registration successful! Please check your email for verification link.");
                                                        // Clear fields
                                                        clearFields();
                                                        // Wait 2 seconds then go to login
                                                        new android.os.Handler().postDelayed(() -> {
                                                            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                                                            finish();
                                                        }, 2000);
                                                    } else {
                                                        showToastLong("Registration successful but failed to send verification email. Please check your inbox.");
                                                        goToDashboard();
                                                    }
                                                });
                                    });
                        }
                    } else {
                        setUiEnabled(true);
                        String reason = readableAuthError(task.getException());
                        Log.e("RegistrationActivity", "Registration failed", task.getException());
                        showToastLong("Registration failed: " + reason);
                    }
                });
    }

    private void clearFields() {
        fullName.setText("");
        emailRegister.setText("");
        passwordRegister.setText("");
        confirmPassword.setText("");
        termsCheckbox.setChecked(false);
    }

    private void goToDashboard() {
        startActivity(new Intent(RegistrationActivity.this, DashboardActivity.class));
        finish();
    }

    private void setUiEnabled(boolean enabled) {
        fullName.setEnabled(enabled);
        emailRegister.setEnabled(enabled);
        passwordRegister.setEnabled(enabled);
        confirmPassword.setEnabled(enabled);
        termsCheckbox.setEnabled(enabled);
        registerButton.setEnabled(enabled);
        googleRegisterBtn.setEnabled(enabled);
        loginLink.setEnabled(enabled);

        float alpha = enabled ? 1f : 0.6f;
        registerButton.setAlpha(alpha);
        googleRegisterBtn.setAlpha(alpha);
    }

    private String getWebClientIdOrNull() {
        try {
            int resId = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
            if (resId == 0) return null;
            String id = getString(resId);
            if (TextUtils.isEmpty(id)) return null;
            if (id.toUpperCase().startsWith("REPLACE")) return null;
            return id;
        } catch (Exception e) {
            return null;
        }
    }

    private String readableAuthError(Exception ex) {
        if (ex instanceof FirebaseAuthException) {
            String code = ((FirebaseAuthException) ex).getErrorCode();
            switch (code) {
                case "ERROR_INVALID_EMAIL":
                    return "Invalid email format";
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    return "Email already in use";
                case "ERROR_WEAK_PASSWORD":
                    return "Weak password";
                case "ERROR_USER_DISABLED":
                    return "Account is disabled";
                case "ERROR_OPERATION_NOT_ALLOWED":
                    return "Operation not allowed";
                default:
                    return code;
            }
        }
        return ex != null ? ex.getMessage() : "Unknown error";
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showToastLong(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
