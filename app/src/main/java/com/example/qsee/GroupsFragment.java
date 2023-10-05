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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupsFragment extends Fragment {
    private String userId;
    private GroupListAdapter adapter;

    // Reference to the Firebase Realtime Database
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the default layout for this fragment
        View view = inflater.inflate(R.layout.fragment_grouplist, container, false);

        // Initialize RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.groupRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Create a list of mock groups (you should replace this with your actual data)
        List<Groups> groupList = createMockGroupList();

        // Retrieve the username from the arguments
        Bundle args = getArguments();
        if (args != null) {
            userId = args.getString("userId");
            loadUserGroups();
        }


        // Create and set the adapter for the RecyclerView
        adapter = new GroupListAdapter(groupList, userId);
        recyclerView.setAdapter(adapter);


        // Query the database to find the userId based on the username
        databaseReference.child("MobileUsers").orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            userId = userSnapshot.getKey();
                            loadUserGroups();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("GroupsFragment", "Error finding user ID: " + databaseError.getMessage());
                    }
                });
        return view;
    }

    // Define a method to create mock group data (replace with your actual data retrieval)
    private List<Groups> createMockGroupList() {
        List<Groups> groupList = new ArrayList<>();
        // Populate groupList with actual data from Firebase or elsewhere
        return groupList;
    }


    private void loadUserGroups() {
        if (userId == null) {
            // userId is not yet initialized or null, handle this situation appropriately
            Log.e("GroupsFragment", "userId is null or not initialized");
            return;
        }

        DatabaseReference groupsRef = databaseReference.child("Groups"); // Reference to the "Groups" node

        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Groups> userGroups = new ArrayList<>();

                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    String groupName = groupSnapshot.child("groupName").getValue(String.class);
                    String adminId = groupSnapshot.child("admin").getValue(String.class);

                    // Check if the user is a member of this group
                    boolean isUserMember = false;

                    for (int i = 1; i <= 50; i++) {
                        String memberKey = "member" + i;
                        String memberValue = groupSnapshot.child(memberKey).getValue(String.class);

                        if (memberValue != null && memberValue.equals(userId)) {
                            // User is a member of this group
                            isUserMember = true;
                            break; // No need to check further
                        }
                    }

                    if (isUserMember) {
                        // User is a member, add this group to userGroups
                        Groups group = new Groups(groupName, adminId);
                        userGroups.add(group);
                    }
                }

                // Update the RecyclerView adapter with the retrieved user groups
                if (adapter != null) {
                    adapter.updateData(userGroups);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("GroupsFragment", "Error loading user groups: " + databaseError.getMessage());
            }
        });
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton addGroupBtn = view.findViewById(R.id.addGroupBtn);

        addGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the AddGroupFragment as a pop-up dialog
                showAddGroupDialog();
            }
        });
    }

    private void showAddGroupDialog() {
        FragmentManager fragmentManager = getChildFragmentManager();
        AddGroupFragment addGroupFragment = new AddGroupFragment(userId);


        // Pass the stored username to AddGroupFragment using arguments
        Bundle args = new Bundle();
        args.putString("userId", userId);
        addGroupFragment.setArguments(args);

        addGroupFragment.show(fragmentManager, "AddGroupFragment");
    }
}
