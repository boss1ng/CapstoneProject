package com.example.qsee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPass extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpass);
        ;}
    public void forgotPassSubmit(View view) {
        // Handle the "Submit" button click here
        Intent intent = new Intent(this, sendOtp.class); // Change OTPInputActivity to your OTP input activity class
        startActivity(intent);
    }

}
