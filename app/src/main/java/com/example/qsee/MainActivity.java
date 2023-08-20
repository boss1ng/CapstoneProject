package com.example.qsee;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Find the "Create an Account" button by its ID
        View createAccountButton = findViewById(R.id.createAccount);

        // Find the "Forgot Password" button by its ID
        View forgotPasswordButton = findViewById(R.id.forgotPassword);

        // Set a click listener for the button
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the "Create an Account" button click here
                Intent intent = new Intent(MainActivity.this, Register.class);
                startActivity(intent);
            }
         });
        // Set a click listener for the "Forgot Password" button
        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the "Forgot Password" button click here
                Intent intent = new Intent(MainActivity.this, ForgotPass.class);
                startActivity(intent);
            }
        });
    }
}