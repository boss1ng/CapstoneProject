package com.example.qsee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Home extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.action_home) {
                    // Handle Home Fragment or perform action
                } else if (itemId == R.id.action_search) {
                    // Handle Search Fragment or perform action
                } else if (itemId == R.id.action_maps) {
                    // Handle Maps Fragment or perform action
                } else if (itemId == R.id.action_quiz) {
                    // Handle Quiz Fragment or perform action
                } else if (itemId == R.id.action_profile) {
                    // Handle Profile Fragment or perform action
                }
                return true;
            }
        });

    }
}

