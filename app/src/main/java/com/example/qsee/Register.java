package com.example.qsee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.app.DatePickerDialog;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class Register extends AppCompatActivity {

    private TextInputLayout textInputLayout;
    private DatePickerDialog datePickerDialog;
    private TextInputLayout fnameInputLayout;
    private TextInputLayout lnameInputLayout;
    private TextInputLayout contactNoInputLayout;
    private TextInputLayout bdateInputLayout;
    private TextInputLayout unameregInputLayout;
    private TextInputLayout passregInputLayout;
    private TextInputLayout repassregInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        textInputLayout = findViewById(R.id.bdate);
        fnameInputLayout = findViewById(R.id.fnameInput);
        lnameInputLayout = findViewById(R.id.lnameInput);
        contactNoInputLayout = findViewById(R.id.contactNo);
        bdateInputLayout = findViewById(R.id.bdate);
        unameregInputLayout = findViewById(R.id.unamereg);
        passregInputLayout = findViewById(R.id.passreg);
        repassregInputLayout = findViewById(R.id.repassreg);

        View registerButton = findViewById(R.id.RegisterButton);
        View regLoginButton = findViewById(R.id.regLogin);

        // Find the EditText associated with contactNoInputLayout
        EditText contactNoEditText = contactNoInputLayout.getEditText();

        if (contactNoEditText != null) {
            // Create an InputFilter to limit the input to 11 digits
            InputFilter[] filters = new InputFilter[1];
            filters[0] = new InputFilter.LengthFilter(11);

            // Apply the InputFilter to the EditText
            contactNoEditText.setFilters(filters);
        }


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String firstName = fnameInputLayout.getEditText().getText().toString();
                String lastName = lnameInputLayout.getEditText().getText().toString();
                String contactNumber = contactNoInputLayout.getEditText().getText().toString();
                String birthdate = bdateInputLayout.getEditText().getText().toString();
                String username = unameregInputLayout.getEditText().getText().toString().toLowerCase();
                String password = passregInputLayout.getEditText().getText().toString();
                String reTypedPassword = repassregInputLayout.getEditText().getText().toString();
                CheckBox consentCheckbox = findViewById(R.id.consentCheckbox);

                // Define the regex pattern
                String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$";

                // Check if the password matches the pattern
                if (password.matches(passwordPattern)) {
                    // Password is valid
                    // Proceed with registration
                } else {
                    // Password is not valid
                    // Show an error message
                    Toast.makeText(Register.this, "Password must be at least 8 characters and include at least one uppercase letter, one lowercase letter, one special character, and one numeric character.", Toast.LENGTH_SHORT).show();
                    return; // Don't proceed with registration
                }

                // Check if any of the input fields are empty
                if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
                        TextUtils.isEmpty(contactNumber) || TextUtils.isEmpty(birthdate) ||
                        TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {

                    // Show an error Toast message if any field is empty
                    Toast.makeText(Register.this, "All fields must be filled out", Toast.LENGTH_SHORT).show();
                    return; // Don't proceed with registration
                }

                // Check if the password and re-typed password match
                if (!password.equals(reTypedPassword)) {
                    // Show an error Toast message if the passwords do not match
                    Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return; // Don't proceed with registration
                }

                // Check if the checkbox is checked
                if (!consentCheckbox.isChecked()) {
                    // Show an error Toast message if the checkbox is not checked
                    Toast.makeText(Register.this, "Please consent to share information as per Data Privacy Act of 2012", Toast.LENGTH_SHORT).show();
                    return; // Don't proceed with registration
                }

                // Check if the username contains a "."
                if (username.contains(".")) {
                    // Show an error Toast message if the username contains a "."
                    Toast.makeText(Register.this, "Username cannot contain a period ('.')", Toast.LENGTH_SHORT).show();
                    return; // Don't proceed with registration
                }

                // Create a new User object
                User user = new User();
                user.setFirstName(AESUtils.encrypt(firstName));
                user.setLastName(AESUtils.encrypt(lastName));
                user.setContactNumber(AESUtils.encrypt(contactNumber));
                user.setBirthdate(AESUtils.encrypt(birthdate));
                user.setUsername(AESUtils.encrypt(username));
                user.setPassword(AESUtils.encrypt(password));


                // Get a reference to the Firebase Realtime Database
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                // Generate a random User ID
                user.generateRandomUserId();


                // Check if the username already exists in the database
                databaseReference.child("MobileUsers").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean isUsernameTaken = false; // Flag to check if the username already exists

                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                User existingUser = userSnapshot.getValue(User.class);
                                if (existingUser != null && existingUser.getUsername().equals(username)) {
                                    // Username already exists, set the flag and break
                                    isUsernameTaken = true;
                                    break;
                                }
                            }
                        }

                        if (isUsernameTaken) {
                            // Username already exists, show an error message
                            Toast.makeText(Register.this, "Username already exists. Choose a different username.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Username is unique, proceed with registration

                            // Write the user data to the database under the generated key
                            databaseReference.child("MobileUsers").child(user.getUserId()).setValue(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Registration in Firebase was successful
                                            Toast.makeText(Register.this, "You have been registered!", Toast.LENGTH_SHORT).show();

                                            // Redirect to MainActivity or perform any other desired actions
                                            Intent intent = new Intent(Register.this, MainActivity.class);
                                            startActivity(intent);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Registration in Firebase failed
                                            Toast.makeText(Register.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error, if any
                        Toast.makeText(Register.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        regLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, MainActivity.class);
                startActivity(intent);
            }
        });

        final EditText dateEditText = textInputLayout.getEditText();

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get current date to pre-select it in the DatePicker
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                datePickerDialog = new DatePickerDialog(Register.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                String formattedDate = (selectedMonth + 1) + "/" + selectedDay + "/" + selectedYear;
                                dateEditText.setText(formattedDate);
                            }
                        }, year, month, day);

                datePickerDialog.show();
            }
        });
    }
}
