package com.example.qsee;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;


public class sendOtp extends AppCompatActivity {

    private EditText otpBox1, otpBox2, otpBox3, otpBox4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requestotp);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        otpBox1 = findViewById(R.id.otpBox1);
        otpBox2 = findViewById(R.id.otpBox2);
        otpBox3 = findViewById(R.id.otpBox3);
        otpBox4 = findViewById(R.id.otpBox4);

        // Retrieve the contact number from the Intent
        String contactNumber = getIntent().getStringExtra("contactNumber");

        // Generate a 4-digit OTP
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000);


        Button verifyButton = findViewById(R.id.otpConfirmButton);
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Simulate OTP verification success and show the success dialog
                verifyOtpAndShowDialog("1234"); // Replace "1234" with the actual OTP value
            }
        });
        setupTextListeners();
    }

    private void setupTextListeners() {
        otpBox1.addTextChangedListener(new GenericTextWatcher(otpBox1));
        otpBox2.addTextChangedListener(new GenericTextWatcher(otpBox2));
        otpBox3.addTextChangedListener(new GenericTextWatcher(otpBox3));
        otpBox4.addTextChangedListener(new GenericTextWatcher(otpBox4));
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

    private void verifyOtpAndShowDialog(String enteredOtp) {
        // Simulate successful OTP verification
        // In a real implementation, you would use your OTP verification logic here

        // Once OTP verification is successful, show the success dialog
        showSuccessDialog();
    }
}
