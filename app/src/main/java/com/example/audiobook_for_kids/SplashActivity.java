package com.example.audiobook_for_kids;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 5000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        ImageView logoImageView = findViewById(R.id.iv_splash_logo);
        TextView appNameTextView = findViewById(R.id.tv_splash_app_name);
        TextView taglineTextView = findViewById(R.id.tv_splash_tagline);

        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);

        // Apply animations with delays
        logoImageView.startAnimation(zoomIn);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            appNameTextView.startAnimation(slideUp);
        }, 500);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            taglineTextView.startAnimation(fadeIn);
        }, 1000);

        // Navigate to login activity after splash duration
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }

}
