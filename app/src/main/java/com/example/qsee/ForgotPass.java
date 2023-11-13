package com.example.qsee;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ForgotPass extends AppCompatActivity {

    private TextInputLayout contactNoInput;
    private DatabaseReference databaseReference; // Add this referenc
    private static final int MAX_LENGTH = 11;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpass);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Retrieve the username from the intent's extras
        String username = getIntent().getStringExtra("userId");

        contactNoInput = findViewById(R.id.contactNoInput);

        // Set an InputFilter to limit the input length to 11 digits
        contactNoInput.getEditText().setFilters(new InputFilter[] { new InputFilter.LengthFilter(MAX_LENGTH) });
        databaseReference = FirebaseDatabase.getInstance().getReference(); // Initialize the reference
        // Query the database for the user's contact number
        databaseReference.child("MobileUsers").orderByChild("userId").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Assuming there's only one user with this username
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                User user = userSnapshot.getValue(User.class);
                                if (user != null) {
                                    // Decrypt the stored encrypted contact number
                                    String storedEncryptedContactNumber = user.getContactNumber();
                                    String decryptedContactNumber = AESUtils.decrypt(storedEncryptedContactNumber);

                                    // Set the contact number in the TextInputLayout
                                    contactNoInput.getEditText().setText(decryptedContactNumber);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error, if any
                        Toast.makeText(ForgotPass.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        View forgotPassLogin = findViewById(R.id.forgotPassLogin);
        forgotPassLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPass.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ;}
    public void forgotPassSubmit(View view) {

        String contactNumber = contactNoInput.getEditText().getText().toString();
        // Retrieve the username from the intent's extras
        String username = getIntent().getStringExtra("userId");

        // Handle the "Submit" button click here
        Intent intent = new Intent(this, sendOtp.class); // Change OTPInputActivity to your OTP input activity class
        intent.putExtra("contactNumber", contactNumber);
        intent.putExtra("username", username);
        startActivity(intent);
    }

}
