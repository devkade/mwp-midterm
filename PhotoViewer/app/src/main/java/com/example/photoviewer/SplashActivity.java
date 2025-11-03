package com.example.photoviewer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.photoviewer.services.SessionManager;
import com.example.photoviewer.utils.SecureTokenManager;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize SecureTokenManager on app startup (earliest point)
        try {
            SecureTokenManager.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent nextActivity;
            if (SessionManager.getInstance().isLoggedIn()) {
                nextActivity = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                nextActivity = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(nextActivity);
            finish();
        }, SPLASH_DURATION);
    }
}
