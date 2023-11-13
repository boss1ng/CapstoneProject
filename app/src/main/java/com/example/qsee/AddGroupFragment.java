package com.example.qsee;

import android.content.pm.ActivityInfo;
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

import java.util.Random;

public class AddGroupFragment extends DialogFragment {

    private Button cancelBtn;
    private Button createBtn;
    private TextInputLayout grpName;
    private String userId;

    private String Group1 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup%201.png?alt=media&token=a61202f9-0f6e-4f2b-a59b-0bdaf152903e";
    private String Group2 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup%202.png?alt=media&token=9f547f1b-2db9-4746-b428-92b83a6d9183";
    private String Group3 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup%203.png?alt=media&token=25a226c7-43f6-4d3d-813c-bc8024428570";
    private String Group4 = "";
    private String Group5 = "";
    private String Group6 = "";
    private String Group7 = "";
    private String Group8 = "";
    private String Group9 = "";
    private String Group10 = "";
    private String Group11 = "";
    private String Group12 = "";
    private String Group13 = "";
    private String Group14 = "";
    private String Group15 = "";


    // Constructor to accept the username as an argument
    public AddGroupFragment(String userId) {
        this.userId = userId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_addgroup, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Retrieve the username from the arguments
        Bundle args = getArguments();
        if (args != null) {
            userId = args.getString("userId");
        }


        // Initialize and set up your dialog views and buttons here

        return view;
    }

    private String getRandomGroup() {
        // Create an array of your URLs
        String[] groupUrls = {Group1, Group2, Group3};

        // Use Random to get a random index from the array
        Random random = new Random();
        int randomIndex = random.nextInt(groupUrls.length);

        // Return the randomly chosen URL
        return groupUrls[randomIndex];
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
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference groupsReference = databaseReference.child("Groups");

                    // Check if the group name already exists
                    groupsReference.child(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // The group name already exists, show a toast message
                                Toast.makeText(getActivity(), "Group already exists. Try a different group name", Toast.LENGTH_LONG).show();
                            } else {
                                // Group name does not exist, proceed to create the group
                                DatabaseReference usersReference = databaseReference.child("MobileUsers");
                                Query query = usersReference.orderByChild("userId").equalTo(userId);

                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            // Retrieve the userId
                                            String userId = dataSnapshot.getChildren().iterator().next().getKey();

                                            // Create a new Group object with the admin (userId)
                                            Groups group = new Groups();
                                            group.setGroupName(groupName);
                                            group.setAdmin(userId);

                                            // Get a reference to the "Groups" node at the root level
                                            DatabaseReference newGroupReference = groupsReference.child(groupName);

                                            // Create a new child node with the group name under the user's node
                                            newGroupReference.setValue(group);

                                            // Set the creator (userId) as member1
                                            newGroupReference.child("member1").setValue(userId);

                                            // Image of the Group
                                            // Call getRandomGroup to get a random URL among 15 choices
                                            String randomUrl = getRandomGroup();
                                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Icons").child(group.getGroupName());
                                            databaseReference.child("GroupPhoto").setValue(randomUrl);
                                            // Display a Toast for successful group creation
                                            Toast.makeText(getActivity(), "Group created successfully", Toast.LENGTH_LONG).show();

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
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle any database query errors here
                        }
                    });
                } else {
                    // Display a Toast message for an empty group name
                    Toast.makeText(getActivity(), "Group name cannot be empty", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
