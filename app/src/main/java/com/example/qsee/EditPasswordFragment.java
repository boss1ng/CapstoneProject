package com.example.qsee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class EditPasswordFragment extends Fragment {

    private String userId;
    private TextInputLayout currentPasswordEditText; // Add this
    private TextInputLayout newPasswordEditText; // Add this
    private TextInputLayout reEnterPasswordEditText; // Add this
    private TextView forgotPass;

    public static EditPasswordFragment newInstance(String userId) {
        EditPasswordFragment fragment = new EditPasswordFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_changepass, container, false);
        // Find the TextInputEditText for the current password
        currentPasswordEditText = view.findViewById(R.id.currentPwd);
        // Find the TextInputEditText for the new password
        newPasswordEditText = view.findViewById(R.id.newPwd);
        // Find the TextInputEditText for re-entering the new password
        reEnterPasswordEditText = view.findViewById(R.id.reNewPwd);
        forgotPass = view.findViewById(R.id.forgotPassword);


        // Retrieve the userId argument inside onCreateView
        Bundle args = getArguments();
        if (args != null) {
            userId = args.getString("userId");
        }
        Log.d("EditPasswordFragment", "Received userId: " + userId);

        forgotPass = view.findViewById(R.id.forgotPassword);
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the click on the "Forgot Password" text
                openForgotPassActivity(userId);
            }
        });


        Button changePasswordButton = view.findViewById(R.id.changePwd);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the user's input for the current password
                String enteredCurrentPassword = currentPasswordEditText.getEditText().getText().toString();
                // Retrieve the user's input for the new password
                String newPassword = newPasswordEditText.getEditText().getText().toString();
                // Retrieve the user's input for re-entered password
                String reEnteredPassword = reEnterPasswordEditText.getEditText().getText().toString();

                // Implement the logic to change the password here
                // Retrieve user's data from Firebase based on the username
                DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("MobileUsers");
                Query query = usersReference.orderByChild("userId").equalTo(userId);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                // Get encrypted user data from Firebase
                                String encryptedPassword = userSnapshot.child("password").getValue(String.class);

                                // Decrypt the values
                                String currentPassword = AESUtils.decrypt(encryptedPassword);

                                // Compare the entered password with the current password
                                if (enteredCurrentPassword.equals(currentPassword)) {
                                    // Passwords match, now check the new password pattern
                                    if (isValidPassword(newPassword)) {
                                        // Check if the new password matches the re-entered password
                                        if (newPassword.equals(reEnteredPassword)) {
                                            // New password and re-entered password are equal
                                            // You can proceed to change the password
                                            // Update the password in Firebase using newPassword
                                            userSnapshot.getRef().child("password").setValue(AESUtils.encrypt(newPassword));
                                            // Navigate back to the ProfileFragment
                                            getParentFragmentManager().popBackStack();
                                            // Show a success message to the user
                                            Toast.makeText(getActivity(), "Password Changed Successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // New password and re-entered password do not match, display an error message
                                            Toast.makeText(getActivity(), "New password do not match", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        // New password does not match the pattern, display an error message
                                        Toast.makeText(getActivity(), "New password does not meet the requirements", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Passwords do not match, display a toast message to the user
                                    Toast.makeText(getActivity(), "Current Password does not match", Toast.LENGTH_SHORT).show();
                                    // You may also want to clear the password fields and prompt the user to re-enter
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error if needed
                    }
                });
            }
        });


        Button cancel = view.findViewById(R.id.cancelBt);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        return view;
    }

    private void openForgotPassActivity(String userId) {
        // Create an Intent to start the ForgotPass activity
        Intent intent = new Intent(getActivity(), ForgotPass.class);

        // Pass the userId to the ForgotPass activity
        intent.putExtra("userId", userId);

        // Start the ForgotPass activity
        startActivity(intent);
    }

    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*(),.?\":{}|<>])(?=.*[0-9]).{8,}$";
        return password.matches(passwordPattern);
    }
}

