package com.tfmhntspn.subguard;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        findViewById(R.id.btn_back_login).setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);

            //avoids creating duplicate instances of screen that already exists
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); //closes current sign up page
        });
    }
}
