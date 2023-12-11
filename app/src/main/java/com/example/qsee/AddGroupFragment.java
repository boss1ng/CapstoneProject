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

    private String Group1 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup1.png?alt=media&token=3bc087ae-5ed1-426f-a126-b09b35248dd0";
    private String Group2 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup2.png?alt=media&token=8860d08b-40f6-4a56-942b-7cb2075304e3";
    private String Group3 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup3.png?alt=media&token=75c15895-e0c7-40b3-b2d7-2830fc7398f2";
    private String Group4 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup4.png?alt=media&token=200ad3a5-5b72-446d-8291-938673f1061c";
    private String Group5 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup5.png?alt=media&token=0739f2d7-05f9-4ea3-96aa-a204a66a7beb";
    private String Group6 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup6.png?alt=media&token=bd749ca2-c997-4302-bd14-d34600fc6b7c";
    private String Group7 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup7.png?alt=media&token=54940844-fded-4278-94cc-0cedd98cfa90";
    private String Group8 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup8.png?alt=media&token=f1534918-4510-48b4-aaa2-46d9b32f3b46";
    private String Group9 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup9.png?alt=media&token=eb600104-3f9c-4b64-b407-1c0b9c22b5a2";
    private String Group10 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup10.png?alt=media&token=a0a4b8d8-fbcf-4ee3-9f02-47924a417b92";
    private String Group11 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup11.png?alt=media&token=4fa71607-d156-4440-b1d8-5803c7bca366";
    private String Group12 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup12.png?alt=media&token=b8a99e22-76a3-48ce-b96f-556ed4014fc5";
    private String Group13 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup13.png?alt=media&token=bfe1916a-3415-4c7e-bc7f-6abb89418eae";
    private String Group14 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup14.png?alt=media&token=3e0f0fa1-84c1-466e-b947-b7c75599af6c";
    private String Group15 = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/CollaborationPhoto%2FGroup15.png?alt=media&token=02646a6b-904a-400c-bf3f-7539e5297fdd";


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
        String[] groupUrls = {Group1, Group2, Group3, Group4, Group5,Group6, Group7, Group8, Group9, Group10, Group11, Group12, Group13, Group14, Group15};

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

                    // Check if the group name contains restricted characters
                    if (groupName.contains(".") || groupName.contains("#") || groupName.contains("$") || groupName.contains("[") || groupName.contains("]")) {
                        // Display a Toast message for invalid group name
                        Toast.makeText(getActivity(), "Group name cannot contain ., #, $, [, or ]", Toast.LENGTH_LONG).show();
                        return;
                    }
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
                                            Toast.makeText(getActivity(), "Group " + groupName + " created", Toast.LENGTH_LONG).show();

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
