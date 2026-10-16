package com.tfmhntspn.subguard;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.content.Intent;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText email, password;
    Button loginBtn, googleBtn;
    TextView forgotPassword, signup;
    FirebaseAuth auth;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.btn_login);
        googleBtn = findViewById(R.id.btn_google);
        forgotPassword = findViewById(R.id.forgot_password);
        signup = findViewById(R.id.signup);
        auth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(v -> {
            String emailText = email.getText() != null ? email.getText().toString() : "";
            String passwordText = password.getText() != null ? password.getText().toString() : "";
            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(LoginActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }
            auth.signInWithEmailAndPassword(emailText, passwordText)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login Failed: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        googleBtn.setOnClickListener(v -> signInWithGoogle());

        // go to sign up screen
        signup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });

        //go to forgot password scree
        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
            finish();
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void signInWithGoogle() {
        // this creates an object that has the confirguration that the credential manager api needs to
        //prompt the user to sign in with their Google Account
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false)
                .build();

        // creates the actual request package that tells the Android Credential Manager what sign-in methods you want
        //to offer the user
        GetCredentialRequest credentialRequest = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        //intialises an instance of the Credential Manager API
        CredentialManager credentialManager = CredentialManager.create(this);

        credentialManager.getCredentialAsync(
                this,
                credentialRequest,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        Credential credential = result.getCredential();
                        handleSignIn(credential);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "GetCredentialException: " + e.getLocalizedMessage(), e);
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Google Sign-In failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
                    }
                }
        );
    }

    private void handleSignIn(Credential credential) {
        if (credential instanceof CustomCredential) {
            CustomCredential customCredential = (CustomCredential) credential;
            if (credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
                try {
                    GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(customCredential.getData());
                    firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse Google ID token credential", e);
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Failed to parse Google ID token", Toast.LENGTH_SHORT).show());
                }
            } else {
                Log.w(TAG, "Credential type is not Google ID token!");
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Invalid credential type", Toast.LENGTH_SHORT).show());
            }
        } else {
            Log.w(TAG, "Credential is not an instance of CustomCredential!");
            runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Google Sign-In failed: Invalid credential", Toast.LENGTH_SHORT).show());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = auth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication Failed: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
