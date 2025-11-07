package com.example.smarthomefull;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.annotation.SuppressLint;
import android.widget.Button;
import android.widget.EditText;
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

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button loginBtn, registerBtn, googleBtn;
    FirebaseAuth mAuth;
    GoogleSignInClient googleSignInClient;
    boolean firebaseReady = false;

    // Activity Result launcher for Google sign-in
    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (!firebaseReady) return;

                Intent data = result.getData();
                if (data == null) {
                    toastShort("Google sign-in canceled");
                    return;
                }

                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account == null || TextUtils.isEmpty(account.getIdToken())) {
                        toastLong("Missing ID token; check Firebase Google Sign-In setup");
                        return;
                    }
                    setAuthUiEnabled(false);
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    mAuth.signInWithCredential(credential).addOnCompleteListener(task1 -> {
                        setAuthUiEnabled(true);
                        if (task1.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            String msg = task1.getException() != null ? task1.getException().getMessage() : "Google auth failed";
                            Log.e("LoginActivity", "Firebase signInWithCredential failed", task1.getException());
                            toastLong("Google auth failed: " + msg);
                        }
                    });
                } catch (ApiException e) {
                    Log.e("LoginActivity", "Google sign-in failed", e);
                    String errorMsg;
                    if (e.getStatusCode() == 10) {
                        errorMsg = "Google Sign-In setup incomplete. Please add SHA-1 certificate to Firebase Console. See FIX_GOOGLE_SIGNIN_ERROR_10.md for instructions.";
                    } else {
                        errorMsg = "Google sign-in failed: " + e.getStatusCode();
                    }
                    toastLong(errorMsg);
                }
            });

    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);
        googleBtn = findViewById(R.id.googleBtn);

        // Optimistically disable buttons until init completes
        setAuthUiEnabled(false);

        // Initialize Firebase safely
        try {
            FirebaseApp app = FirebaseApp.initializeApp(getApplicationContext());
            if (app == null) {
                // In some setups Firebase auto-init may still allow direct instance retrieval
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
            Log.e("LoginActivity", "Firebase init failed", t);
            firebaseReady = false;
        }

        if (firebaseReady) {
            setAuthUiEnabled(true);
            // If already logged in, go directly to dashboard
            FirebaseUser current = mAuth.getCurrentUser();
            if (current != null && (current.isEmailVerified() || current.getProviderData().size() > 1)) {
                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                finish();
                return;
            }
        } else {
            // Keep Google disabled and inform user, but allow UI to render
            toastLong("Firebase not configured. Add google-services.json to enable login.");
            // Buttons remain disabled
            return;
        }

        // Google Sign-In availability
        String webClientId = getWebClientIdOrNull();
        if (webClientId == null) {
            googleBtn.setEnabled(false);
            googleBtn.setAlpha(0.6f);
            googleBtn.setOnClickListener(null);
            toastLong("Google Sign-In disabled: configure default_web_client_id via Firebase.");
        } else {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build();
            googleSignInClient = GoogleSignIn.getClient(this, gso);
            googleBtn.setEnabled(true);
            googleBtn.setAlpha(1f);
            googleBtn.setOnClickListener(v -> googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));
        }

        loginBtn.setOnClickListener(v -> loginUser());

        // Register button now navigates to RegistrationActivity
        registerBtn.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
        });
    }

    void loginUser(){
        if(!firebaseReady) return;
        String e = email.getText().toString().trim();
        String p = password.getText().toString().trim();
        if (!isValidEmail(e)) { toastShort("Enter a valid email"); return; }
        if (TextUtils.isEmpty(p)) { toastShort("Enter password"); return; }
        setAuthUiEnabled(false);
        mAuth.signInWithEmailAndPassword(e,p).addOnCompleteListener(task -> {
            setAuthUiEnabled(true);
            if(task.isSuccessful()){
                FirebaseUser user = mAuth.getCurrentUser();
                if(user!=null && (user.isEmailVerified() || user.getProviderData().size()>1)){
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finish();
                } else {
                    toastLong("Verify your email first");
                }
            } else {
                String reason = readableAuthError(task.getException());
                Log.e("LoginActivity", "Login failed", task.getException());
                toastLong("Login failed: " + reason);
            }
        });
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @SuppressLint("DiscouragedApi")
    private String getWebClientIdOrNull() {
        try {
            int resId = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
            if (resId == 0) return null;
            String id = getString(resId);
            if (TextUtils.isEmpty(id)) return null;
            // Common placeholder used in local strings when google-services.json isn't configured to generate one
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
                case "ERROR_USER_NOT_FOUND":
                    return "No account found with that email";
                case "ERROR_WRONG_PASSWORD":
                    return "Incorrect password";
                case "ERROR_USER_DISABLED":
                    return "Account is disabled";
                case "ERROR_OPERATION_NOT_ALLOWED":
                    return "Operation not allowed";
                case "ERROR_WEAK_PASSWORD":
                    return "Weak password";
                default:
                    return code;
            }
        }
        return ex != null ? ex.getMessage() : "unknown error";
    }

    private void setAuthUiEnabled(boolean enabled) {
        if (loginBtn != null) loginBtn.setEnabled(enabled);
        if (registerBtn != null) registerBtn.setEnabled(enabled);
        if (googleBtn != null) googleBtn.setEnabled(enabled);
        float alpha = enabled ? 1f : 0.6f;
        if (loginBtn != null) loginBtn.setAlpha(alpha);
        if (registerBtn != null) registerBtn.setAlpha(alpha);
        if (googleBtn != null) googleBtn.setAlpha(alpha);
    }

    private void toastShort(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }
    private void toastLong(String msg) { Toast.makeText(this, msg, Toast.LENGTH_LONG).show(); }
}
