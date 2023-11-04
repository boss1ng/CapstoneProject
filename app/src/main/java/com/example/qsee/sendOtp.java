package com.example.qsee;

import static android.Manifest.permission.SEND_SMS;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;
import java.util.concurrent.TimeUnit;


public class sendOtp extends AppCompatActivity {

    private EditText otpBox1, otpBox2, otpBox3, otpBox4, otpBox5, otpBox6;
    private String myVerificationId;
    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private DatabaseReference databaseReference;
    // Declare a global CountDownTimer variable
    private CountDownTimer resendOtpTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requestotp);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        otpBox1 = findViewById(R.id.otpBox1);
        otpBox2 = findViewById(R.id.otpBox2);
        otpBox3 = findViewById(R.id.otpBox3);
        otpBox4 = findViewById(R.id.otpBox4);
        otpBox5 = findViewById(R.id.otpBox5);
        otpBox6 = findViewById(R.id.otpBox6);

        // Retrieve the contact number from the Intent
        String contactNumber = getIntent().getStringExtra("contactNumber");


        // Generate a 4-digit OTP
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000);

        String otpMessage = "Your OTP is: " + otp;
        sendSMSUsingFirebase(contactNumber, otpMessage);



        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, 1);
        }
        TextView sendOtpAgainTextView = findViewById(R.id.sendOtpAgain);
        TextView countdownTimer = findViewById(R.id.countdownTimer);
        resendOtpTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                // Update your UI with the remaining time if necessary
                // For example, you can update a TextView to show the remaining time
                countdownTimer.setText("You can send an OTP again in " + seconds + " seconds");
            }

            public void onFinish() {
                // Enable the button or perform any other action to allow OTP resend
                sendOtpAgainTextView.setEnabled(true);
                sendOtpAgainTextView.setTextColor(ContextCompat.getColor(sendOtp.this, R.color.blue));
                countdownTimer.setText(""); // Clear the timer text
            }
        };

        sendOtpAgainTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if the timer is already running
                if (resendOtpTimer != null && !sendOtpAgainTextView.isEnabled()) {
                    // Notify the user with a Toast message that they need to wait for the timer to finish
                    Toast.makeText(sendOtp.this, "Please wait for the timer to end before requesting a new OTP.", Toast.LENGTH_SHORT).show();
                } else {
                    // Disable the button and start the timer
                    view.setEnabled(false);
                    resendOtpTimer.start();
                    sendSMSUsingFirebase(contactNumber, otpMessage);
                }
            }
        });



        Button verifyButton = findViewById(R.id.otpConfirmButton);
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Retrieving the text from each EditText
                String enteredOtp = otpBox1.getText().toString() +
                        otpBox2.getText().toString() +
                        otpBox3.getText().toString() +
                        otpBox4.getText().toString() +
                        otpBox5.getText().toString() +
                        otpBox6.getText().toString();

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(myVerificationId, enteredOtp);
                signInWithPhoneAuthCredential(credential);
            }
        });
        setupTextListeners();
    }
    private void sendSMSUsingFirebase(String phoneNumber, String message) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String phoneNumberWithCountryCode = "+63" + phoneNumber.substring(1);
        PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                // Handle successful verification completion here
                Toast.makeText(getApplicationContext(), "Verification completed successfully!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                // Handle verification failure here
                Toast.makeText(getApplicationContext(), "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                // The SMS verification code has been sent to the provided phone number.
                // You can use the verificationId and the user's input to verify the code.
                // Store the verificationId and use it to verify the code from the user.
                // For example:
                // myVerificationId = verificationId;
                myVerificationId = verificationId;
                sendOtp.this.forceResendingToken = forceResendingToken;
                Toast.makeText(getApplicationContext(), "Verification code has been sent", Toast.LENGTH_SHORT).show();
                resendOtpTimer.start();
            }
        };
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumberWithCountryCode,  // Phone number to verify
                60,           // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                this,        // Activity (for callback binding)
                callbacks,
                forceResendingToken);  // ForceResendingToken
    }



    private void setupTextListeners() {
        otpBox1.addTextChangedListener(new GenericTextWatcher(otpBox1));
        otpBox2.addTextChangedListener(new GenericTextWatcher(otpBox2));
        otpBox3.addTextChangedListener(new GenericTextWatcher(otpBox3));
        otpBox4.addTextChangedListener(new GenericTextWatcher(otpBox4));
        otpBox5.addTextChangedListener(new GenericTextWatcher(otpBox5));
        otpBox6.addTextChangedListener(new GenericTextWatcher(otpBox6));
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        String username = getIntent().getStringExtra("username");
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Verification successful
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
                                                        String encryptedbirthdate = user.getBirthdate();
                                                        String decryptedbirthdate = AESUtils.decrypt(encryptedbirthdate);
                                                        String formattedBirthdate = formatBirthdate(decryptedbirthdate);
                                                        String encryptedPassword = AESUtils.encrypt(formattedBirthdate);

                                                        // Set the password value as the formatted birthdate
                                                        userSnapshot.getRef().child("password").setValue(encryptedPassword);

                                                        Log.d("Birthday", formattedBirthdate);

                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            // Handle database error, if any
                                            Toast.makeText(sendOtp.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            showSuccessDialog();
                        } else {
                            // Verification failed
                            Toast.makeText(getApplicationContext(), "OTP does not match", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private class GenericTextWatcher implements TextWatcher {
        private View view;

        private GenericTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String text = editable.toString();
            if (text.length() == 1) {
                View nextView = view.focusSearch(View.FOCUS_RIGHT);
                if (nextView != null) {
                    nextView.requestFocus();
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // Retrieve the text from the EditText
            EditText editText = (EditText) view;
            String text = editText.getText().toString().trim();

            // Detect backspace press and move focus to the previous OTP box if the current OTP box is empty
            if (i1 > i2 && text.isEmpty()) {
                View previousView = view.focusSearch(View.FOCUS_LEFT);
                if (previousView != null) {
                    previousView.requestFocus();
                }
            }
        }

    }
    private void showSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.successresetpass);
        dialog.setCancelable(false);

        Button backToLoginButton = dialog.findViewById(R.id.backToLoginButton);
        backToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                // Navigate back to the login screen (MainActivity)
                Intent intent = new Intent(sendOtp.this, MainActivity.class);
                startActivity(intent);
            }
        });

        dialog.show();
    }
    private String formatBirthdate(String birthdate) {
        String[] parts = birthdate.split("/");
        String formattedMonth = String.format("%02d", Integer.parseInt(parts[0]));
        String formattedDay = String.format("%02d", Integer.parseInt(parts[1]));
        String formattedYear = parts[2];
        return formattedMonth + formattedDay + formattedYear;
    }

}
