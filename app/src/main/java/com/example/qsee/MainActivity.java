package com.example.qsee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextInputLayout unameInputLayout;
    private TextInputLayout passInputLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        unameInputLayout = findViewById(R.id.unameInput);
        passInputLayout = findViewById(R.id.passInput);

        // Find the "Create an Account" button by its ID
        View createAccountButton = findViewById(R.id.createAccount);

        // Find the "Forgot Password" button by its ID
        View forgotPasswordButton = findViewById(R.id.forgotPassword);

        View signInButton = findViewById(R.id.SignInButton);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the "Sign In" button click here
                signIn();
            }
        });


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
                // Check if the username is empty
                String username = unameInputLayout.getEditText().getText().toString();
                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(MainActivity.this, "Please enter your username", Toast.LENGTH_SHORT).show();
                    return; // Don't proceed if the username is empty
                }

                // Check if the username exists in the Firebase database
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                DatabaseReference usersReference = databaseReference.child("MobileUsers");

                usersReference.orderByChild("username").equalTo(username)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // Username exists, proceed to the "ForgotPass" activity
                                    Intent intent = new Intent(MainActivity.this, ForgotPass.class);
                                    intent.putExtra("username", username);
                                    startActivity(intent);
                                } else {
                                    // Username does not exist in the database
                                    Toast.makeText(MainActivity.this, "Username does not exist", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle database error, if any
                                Toast.makeText(MainActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void signIn() {
        String username = unameInputLayout.getEditText().getText().toString();
        String password = passInputLayout.getEditText().getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get a reference to the Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Point to the "MobileUsers" node
        DatabaseReference usersReference = databaseReference.child("MobileUsers");

        // Query all child nodes under "MobileUsers"
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null && user.getUsername().equals(username) && user.getPassword().equals(password)) {
                            // Username and password match, sign in successful
                            String userId = user.getUserId(); // Get the user's ID
                            Intent intent = new Intent(MainActivity.this, Home.class);
                            intent.putExtra("userId", userId); // Pass the userId as an extra
                            startActivity(intent);
                            return;
                        }
                    }
                }

                // No matching username and password found
                Toast.makeText(MainActivity.this, "Username or Password is incorrect.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error, if any
                Toast.makeText(MainActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}