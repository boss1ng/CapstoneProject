package com.example.qsee;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class splashActivity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(splashActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish(); // Close the splash screen activity
            }
        }, SPLASH_DISPLAY_DURATION);
    }
}

