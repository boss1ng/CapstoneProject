package com.example.qsee;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase with your custom database URL
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("1:67612839105:web:7d8ffd05e13733ef609840") // Your Firebase App ID
                .setApiKey("AIzaSyB08GrYWprzzbddoHqvTR5Ln-9i0CEXqHs") // Your Firebase API Key
                .setDatabaseUrl("https://capstone-project-ffe21-default-rtdb.asia-southeast1.firebasedatabase.app/") // Your specific database URL
                .build();

        FirebaseApp.initializeApp(this, options);
    }
}

