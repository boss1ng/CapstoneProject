package com.example.qsee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Home extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        String userId = getIntent().getStringExtra("userId");

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.action_home) {
                    loadFragment(new HomeFragment(), userId);
                } else if (itemId == R.id.action_search) {
                    loadFragment(new SearchFragment(), userId);
                } else if (itemId == R.id.action_maps) {
                    loadFragment(new MapsFragment(), userId);
                } else if (itemId == R.id.action_quiz) {
                    loadFragment(new StartQuizFragment(), userId);
                } else if (itemId == R.id.action_profile) {
                    loadFragment(new ProfileFragment(), userId);
                }
                return true;
            }
        });
        // Load HomeFragment when the activity starts
        loadFragment(new HomeFragment(), userId);

    }

    private void loadFragment(Fragment fragment, String userId) {
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}


