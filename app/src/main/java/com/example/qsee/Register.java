package com.example.qsee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.app.DatePickerDialog;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.material.textfield.TextInputLayout;
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
    private boolean isPasswordVisible = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // Define the regex pattern
        String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$";

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        textInputLayout = findViewById(R.id.bdate);
        fnameInputLayout = findViewById(R.id.fnameInput);
        lnameInputLayout = findViewById(R.id.lnameInput);
        contactNoInputLayout = findViewById(R.id.contactNo);
        bdateInputLayout = findViewById(R.id.bdate);
        unameregInputLayout = findViewById(R.id.unamereg);
        passregInputLayout = findViewById(R.id.passreg);
        repassregInputLayout = findViewById(R.id.repassreg);

        // Find the EditText associated with passregInputLayout
        EditText passwordEditText = passregInputLayout.getEditText();
        EditText repasswordEditText = repassregInputLayout.getEditText();
        // Find the EditText associated with unameregInputLayout
        EditText usernameEditText = unameregInputLayout.getEditText();

        if (usernameEditText != null) {
            // Add a TextWatcher to the username EditText
            usernameEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                    // This method is called to notify you that characters within `charSequence` are about to be replaced with new text with length `count`
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    // This method is called to notify you that somewhere within `charSequence`, the text has been replaced with new text with length `count`
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    // This method is called to notify you that somewhere within `editable`, the text has been changed.

                    // Get the entered username
                    String enteredUsername = editable.toString();

                    // Validate the username
                    String errorMessage = validateUsername(enteredUsername);
                    if (errorMessage != null) {
                        // Show the specific error for the invalid username
                        usernameEditText.setError(errorMessage);
                    } else {
                        // Clear the error if the username is valid
                        usernameEditText.setError(null);
                        // Check username availability
                        checkUsernameAvailability(enteredUsername);
                    }
                }
            });
        }

        passwordEditText.setOnTouchListener((v, event) -> {
            int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    isPasswordVisible = !isPasswordVisible;
                    int inputType = isPasswordVisible ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
                    passwordEditText.setInputType(inputType);

                    // Apply the custom font after changing the InputType
                    applyCustomFont(passwordEditText);

                    passwordEditText.setSelection(passwordEditText.getText().length());
                    return true;
                }
            }
            return false;
        });

        repasswordEditText.setOnTouchListener((v, event) -> {
            int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (repasswordEditText.getRight() - repasswordEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    isPasswordVisible = !isPasswordVisible;
                    int inputType = isPasswordVisible ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
                    repasswordEditText.setInputType(inputType);

                    // Apply the custom font after changing the InputType
                    applyCustomFont(repasswordEditText);

                    repasswordEditText.setSelection(repasswordEditText.getText().length());
                    return true;
                }
            }
            return false;
        });

        if (repasswordEditText != null) {
            // Add a TextWatcher to the re-entered password EditText
            repasswordEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                    // This method is called to notify you that characters within `charSequence` are about to be replaced with new text with length `count`
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    // This method is called to notify you that somewhere within `charSequence`, the text has been replaced with new text with length `count`
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    // This method is called to notify you that somewhere within `editable`, the text has been changed.

                    // Get the entered password and re-entered password
                    String password = passwordEditText != null ? passwordEditText.getText().toString() : "";
                    String reEnteredPassword = editable.toString();

                    // Check if the passwords match
                    if (!TextUtils.isEmpty(password) && !password.equals(reEnteredPassword)) {
                        // Set an error if the passwords don't match
                        repasswordEditText.setError("Passwords do not match.");
                    } else {
                        // Clear the error if the passwords match
                        repasswordEditText.setError(null);
                    }
                }
            });
        }

        if (passwordEditText != null) {
            // Add a TextWatcher to the password EditText
            passwordEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // This method is called to notify you that characters within `s` are about to be replaced with new text with length `after`
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // This method is called to notify you that somewhere within `s`, the text has been replaced with new text with length `count`
                }

                public void afterTextChanged(Editable s) {
                    // This method is called to notify you that somewhere within `s`, the text has been changed.
                    String password = s.toString();

                    // Check if the password matches the pattern
                    if (password.matches(passwordPattern)) {
                        // Password is valid, clear the error
                        passwordEditText.setError(null);
                    } else {
                        // Password is not valid, set error on the password field
                        String errorMessage = getErrorMessage(password);
                        passwordEditText.setError(errorMessage);
                    }
                }
            });
        }

        View registerButton = findViewById(R.id.RegisterButton);
        View regLoginButton = findViewById(R.id.regLogin);
        CheckBox consentCheckbox = findViewById(R.id.consentCheckbox);

        // Find the EditText associated with contactNoInputLayout
        EditText contactNoEditText = contactNoInputLayout.getEditText();

        if (contactNoEditText != null) {
            // Create an InputFilter to limit the input to 11 digits
            InputFilter[] filters = new InputFilter[1];
            filters[0] = new InputFilter.LengthFilter(9);

            // Apply the InputFilter to the EditText
            contactNoEditText.setFilters(filters);
        }

        // Show the dialog when the checkbox is checked
        AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.tos, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button acceptBtn = dialogView.findViewById(R.id.AcceptBtn);
        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                consentCheckbox.setChecked(true); // Check the checkbox when the accept button is clicked
                dialog.dismiss(); // Close the dialog
            }
        });

        // Uncheck the checkbox if the user does not accept the terms
        Button declineBtn = dialogView.findViewById(R.id.DeclineBtn);
        declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                consentCheckbox.setChecked(false); // Uncheck the checkbox
                dialog.dismiss(); // Close the dialog
            }
        });

        // Assuming consentCheckbox is already defined and initialized
        consentCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    // Show the dialog when the checkbox is checked
                    AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.tos, null);
                    builder.setView(dialogView);

                    AlertDialog dialog = builder.create();
                    dialog.show();

                    Button acceptBtn = dialogView.findViewById(R.id.AcceptBtn);
                    acceptBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            consentCheckbox.setChecked(true); // Check the checkbox when the accept button is clicked
                            dialog.dismiss(); // Close the dialog
                        }
                    });

                    // Uncheck the checkbox if the user does not accept the terms
                    Button declineBtn = dialogView.findViewById(R.id.DeclineBtn);
                    declineBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            consentCheckbox.setChecked(false); // Uncheck the checkbox
                            dialog.dismiss(); // Close the dialog
                        }
                    });
            }
        });



        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String firstName = fnameInputLayout.getEditText().getText().toString();
                String lastName = lnameInputLayout.getEditText().getText().toString();
                String contactNumber = contactNoInputLayout.getEditText().getText().toString();

                String modifiedContactNo = "09" + contactNumber;

                String birthdate = bdateInputLayout.getEditText().getText().toString();
                String username = unameregInputLayout.getEditText().getText().toString().toLowerCase();
                String password = passregInputLayout.getEditText().getText().toString();
                String reTypedPassword = repassregInputLayout.getEditText().getText().toString();





                // Check if any of the input fields are empty
                if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
                        TextUtils.isEmpty(contactNumber) || TextUtils.isEmpty(birthdate) ||
                        TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {

                    // Show an error Toast message if any field is empty
                    Toast.makeText(Register.this, "Please fill all the fields.", Toast.LENGTH_LONG).show();
                    return; // Don't proceed with registration
                }

                // Check if the password matches the pattern
                if (password.matches(passwordPattern)) {
                    // Password is valid
                    // Proceed with registration
                } else {
                    // Password is not valid
                    // Show an error message
                    Toast.makeText(Register.this, "Password must be at least 8 characters and include at least one uppercase letter, one lowercase letter, one special character, and one numeric character.", Toast.LENGTH_LONG).show();
                    return; // Don't proceed with registration
                }

                // Check if the password and re-typed password match
                if (!password.equals(reTypedPassword)) {
                    // Show an error Toast message if the passwords do not match
                    Toast.makeText(Register.this, "Passwords do not match.", Toast.LENGTH_LONG).show();
                    return; // Don't proceed with registration
                }

                // Check if the checkbox is checked
                if (!consentCheckbox.isChecked()) {
                    // Show an error Toast message if the checkbox is not checked
                    Toast.makeText(Register.this, "Please consent to share information as per Data Privacy Act of 2012.", Toast.LENGTH_LONG).show();
                    return; // Don't proceed with registration
                }

                // Check if the username contains a "."
                if (username.contains(".")) {
                    // Show an error Toast message if the username contains a "."
                    Toast.makeText(Register.this, "Username cannot contain a period ('.')", Toast.LENGTH_LONG).show();
                    return; // Don't proceed with registration
                }

                // Create a new User object
                User user = new User();
                user.setFirstName(AESUtils.encrypt(firstName));
                user.setLastName(AESUtils.encrypt(lastName));
                //user.setContactNumber(AESUtils.encrypt(contactNumber));
                user.setContactNumber(AESUtils.encrypt(modifiedContactNo));
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
                                if (existingUser != null) {
                                    // Decrypt the encrypted username
                                    String decryptedUsername = AESUtils.decrypt(existingUser.getUsername());
                                    if (decryptedUsername.equals(username)) {
                                        // Username already exists, set the flag and break
                                        isUsernameTaken = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (isUsernameTaken) {
                            // Username already exists, show an error message
                            Toast.makeText(Register.this, "Username already exists. Choose a different username.", Toast.LENGTH_LONG).show();
                        } else {
                            // Username is unique, proceed with registration

                            // Write the user data to the database under the generated key
                            databaseReference.child("MobileUsers").child(user.getUserId()).setValue(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Registration in Firebase was successful
                                            Toast.makeText(Register.this, "You have been registered.", Toast.LENGTH_LONG).show();

                                            // Redirect to MainActivity or perform any other desired actions
                                            Intent intent = new Intent(Register.this, MainActivity.class);
                                            startActivity(intent);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Registration in Firebase failed
                                            Toast.makeText(Register.this, "Registration failed. Please try again.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error, if any
                        Toast.makeText(Register.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
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
    private void showPasswordInfoPopup() {
        // Create a string with bulleted list
        String passwordRequirements = "Password must have:\n\n" +
                "\u2022 At least 8 characters\n" +
                "\u2022 At least one uppercase letter\n" +
                "\u2022 At least one lowercase letter\n" +
                "\u2022 At least one special character\n" +
                "\u2022 At least one numeric character";

        AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
        builder.setTitle("Password Requirements");
        builder.setMessage(passwordRequirements);
        builder.setPositiveButton("OK", null); // You can add a listener if needed
        builder.create().show();
    }
    private String getErrorMessage(String password) {
        StringBuilder errorMessage = new StringBuilder();

        if (password.length() < 8) {
            errorMessage.append("At least 8 characters\n");
        }

        if (!containsUppercase(password)) {
            errorMessage.append("At least one uppercase letter\n");
        }

        if (!containsLowercase(password)) {
            errorMessage.append("At least one lowercase letter\n");
        }

        if (!containsSpecialCharacter(password)) {
            errorMessage.append("At least one special character\n");
        }

        if (!containsNumericCharacter(password)) {
            errorMessage.append("At least one numeric character\n");
        }

        return errorMessage.length() > 0 ? errorMessage.toString() : null;
    }


    private boolean containsUppercase(String password) {
        return !password.equals(password.toLowerCase());
    }

    private boolean containsLowercase(String password) {
        return !password.equals(password.toUpperCase());
    }

    private boolean containsSpecialCharacter(String password) {
        // Check if the password contains at least one special character
        String specialCharacters = "!@#$%^&*()-_=+\\|[{]};:'\",<.>/?";
        for (char c : specialCharacters.toCharArray()) {
            if (password.contains(String.valueOf(c))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsNumericCharacter(String password) {
        // Check if the password contains at least one numeric character
        return password.matches(".*\\d.*");
    }

    private void applyCustomFont(EditText editText) {
        // Load the Raleway font from the res/font directory
        Typeface ralewayFont = ResourcesCompat.getFont(this, R.font.raleway_regular);

        // Apply the custom font to the EditText
        editText.setTypeface(ralewayFont);
    }

    private String validateUsername(String username) {
        // Check if the username is at least 4 characters long
        if (username.length() < 4) {
            return "Username must be at least 4 characters long.";
        }

        // Check if the username contains '.', '$', '#', '[', ']', '/', or ASCII control characters 0-31 or 127
        String invalidCharacters = ".#$[]/";
        for (char c : invalidCharacters.toCharArray()) {
            if (username.contains(String.valueOf(c)) || (c <= 31 || c == 127)) {
                return "Username cannot contain ' " + c + " '";
            }
        }

        return null; // Return null if the username is valid
    }

    // Add a method to check if the username is already taken
    private void checkUsernameAvailability(String username) {
        // Get a reference to the Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Check if the username already exists in the database
        databaseReference.child("MobileUsers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                EditText usernameEditText = unameregInputLayout.getEditText();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User existingUser = userSnapshot.getValue(User.class);
                        if (existingUser != null) {
                            // Decrypt the encrypted username
                            String decryptedUsername = AESUtils.decrypt(existingUser.getUsername());
                            if (decryptedUsername.equals(username)) {
                                // Username already exists, show an error message
                                usernameEditText.setError("Username is already taken.");
                                return;
                            }
                        }
                    }
                }
                // Username is available, clear the error
                usernameEditText.setError(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error, if any
                Toast.makeText(Register.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
