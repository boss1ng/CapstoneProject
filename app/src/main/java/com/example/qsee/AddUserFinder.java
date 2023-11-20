package com.example.qsee;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddUserFinder extends DialogFragment {

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private String userId;
    private String groupName;

    public AddUserFinder() {
        // Required empty public constructor
    }

    public static AddUserFinder newInstance(String userId, String groupName) {
        AddUserFinder fragment = new AddUserFinder();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("groupName", groupName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            groupName = getArguments().getString("groupName");
            // Log the retrieved username and groupName
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.adduserfinder, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Initialize UI components from the layout
        TextInputEditText userIdTextView = view.findViewById(R.id.editTextUserId);
        Button inviteBtn = view.findViewById(R.id.inviteBtn);

        int maxLength = 10;
        userIdTextView.setFilters(new InputFilter[] { new InputFilter.LengthFilter(maxLength) });

        // Set a click listener for the "Invite" button
        inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredUserId = userIdTextView.getText().toString().trim();

                // Check if the userId is not empty
                if (!enteredUserId.isEmpty()) {
                    // Retrieve user details (firstname and lastname) based on userId
                    retrieveUserDetails(enteredUserId);
                } else {
                    Toast.makeText(getActivity(), "Please enter a username", Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }

    private void retrieveUserDetails(final String member) {
        databaseReference.child("MobileUsers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot userSnapshot) {
                boolean userFound = false; // Flag to check if a user is found

                if (userSnapshot.exists()) {
                    for (DataSnapshot user : userSnapshot.getChildren()) {
                        // Assuming each child has an "encryptedUsername" field
                        String encryptedUsername = user.child("username").getValue(String.class);

                        if (encryptedUsername != null) {
                            // Decrypt the username
                            String username = AESUtils.decrypt(encryptedUsername);

                            if (username != null && username.toLowerCase().equals(member.toLowerCase())) {
                                // Match found, retrieve user details
                                userFound = true;

                                String encryptedFirstName = user.child("firstName").getValue(String.class);
                                String encryptedLastName = user.child("lastName").getValue(String.class);
                                String ID = user.child("userId").getValue(String.class);

                                // Decrypt firstname and lastname
                                String firstName = AESUtils.decrypt(encryptedFirstName);
                                String lastName = AESUtils.decrypt(encryptedLastName);

                                // Now, you have decrypted firstname and lastname
                                // Display the AddUserToGroup dialog with user details
                                showAddUserToGroupDialog(userId, groupName, ID, firstName, lastName);
                            }
                        }
                    }

                    // Check if no user was found and show toast
                    if (!userFound) {
                        // If no match is found after iterating through all children
                        Toast.makeText(getActivity(), "User not found", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors, if any
            }
        });
    }




    private void showAddUserToGroupDialog(String userId, String groupName, String member, String firstname, String lastname) {
        // Create an instance of the AddUserToGroup dialog fragment
        AddUserToGroup addUserToGroupDialog = new AddUserToGroup(userId, groupName, member, firstname, lastname);

        // Show the dialog using the FragmentManager
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        addUserToGroupDialog.show(fragmentManager, "AddUserToGroupDialog");
    }

}
