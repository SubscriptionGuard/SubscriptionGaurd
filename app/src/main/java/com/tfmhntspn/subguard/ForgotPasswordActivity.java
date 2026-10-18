package com.tfmhntspn.subguard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText resetEmail;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        resetEmail = findViewById(R.id.reset_email);
        Button sendResetBtn = findViewById(R.id.btn_send_reset);
        auth = FirebaseAuth.getInstance();

        sendResetBtn.setOnClickListener(v -> {
            String email = resetEmail.getText() != null ? resetEmail.getText().toString().trim() : "";

            if (email.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "Please enter your email address", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this, "Password reset email sent. Check your inbox!", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(ForgotPasswordActivity.this, "Failed to send reset email: " + errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        findViewById(R.id.btn_back_login).setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);

            //avoids creating duplicate instances of screen that already exists
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); //closes current forgot password page
        });
    }
}
