package com.example.qsee;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddGroupFragment extends DialogFragment {

    private Button cancelBtn;
    private Button createBtn;
    private TextInputLayout grpName;
    // Add a field to store the username
    private String username;

    // Constructor to accept the username as an argument
    public AddGroupFragment(String username) {
        this.username = username;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_addgroup, container, false);

        // Retrieve the username from the arguments
        Bundle args = getArguments();
        if (args != null) {
            username = args.getString("username");
            // Now you have the username, and you can use it in this fragment
            Log.d("AddGroupFragment", username);
        }

        // Initialize and set up your dialog views and buttons here

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        cancelBtn = view.findViewById(R.id.CancelBtn);
        createBtn = view.findViewById(R.id.CreateBtn);
        grpName = view.findViewById(R.id.addGroupName);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("groups");

        // Set click listeners for the buttons
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Cancel button click
                dismiss(); // Close the dialog
            }
        });
        // Get a reference to the root of your Firebase database
        DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sanitizedUsername = username.replace(".", "_"); // Replace '.' with '_'
                String groupName = grpName.getEditText().getText().toString().trim();
                if (!groupName.isEmpty()) {
                    // Create a new Group object with only "Admin"
                    Groups group = new Groups();
                    group.setAdmin(username); // Set the admin (formerly createdBy) field

                    // Get a reference to the "Groups" node at the root level
                    DatabaseReference groupsReference = rootReference.child("Groups");

                    // Create a new child node for the current user under the "Groups" node
                    DatabaseReference userGroupsReference = groupsReference.child(sanitizedUsername);

                    // Create a new child node with the group name under the user's node
                    DatabaseReference newGroupReference = userGroupsReference.child(groupName);
                    newGroupReference.setValue(group);

                    // Close the dialog
                    dismiss();
                } else {
                    // Handle the case where the group name is empty
                }
            }
        });
    }
}
