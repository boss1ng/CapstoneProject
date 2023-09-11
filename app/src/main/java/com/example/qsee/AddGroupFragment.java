package com.example.qsee;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AddGroupFragment extends DialogFragment {

    private Button cancelBtn;
    private Button createBtn;
    private TextInputLayout grpName;
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
                String groupName = grpName.getEditText().getText().toString().trim();
                if (!groupName.isEmpty()) {
                    // Get a reference to the Firebase database
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                    // Query to fetch the userId based on the username
                    DatabaseReference usersReference = databaseReference.child("MobileUsers");
                    Query query = usersReference.orderByChild("username").equalTo(username);

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Retrieve the userId
                                String userId = dataSnapshot.getChildren().iterator().next().getKey();

                                // Create a new Group object with the admin (userId)
                                Groups group = new Groups();
                                group.setAdmin(username);

                                // Get a reference to the "Groups" node at the root level
                                DatabaseReference groupsReference = databaseReference.child("Groups");

                                // Create a new child node for the current user under the "Groups" node
                                DatabaseReference userGroupsReference = groupsReference.child(userId);

                                // Create a new child node with the group name under the user's node
                                DatabaseReference newGroupReference = userGroupsReference.child(groupName);
                                newGroupReference.setValue(group);

                                // Display a Toast for successful group creation
                                Toast.makeText(getActivity(), "Group created successfully", Toast.LENGTH_SHORT).show();

                                // Close the dialog
                                dismiss();
                            } else {
                                // Handle the case where the username does not exist
                                // You can display an error message or take appropriate action
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle any database query errors here
                        }
                    });
                } else {
                    // Display a Toast message for an empty group name
                    Toast.makeText(getActivity(), "Group name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
