package com.example.qsee;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupViewFragment extends Fragment {

    private static final String TAG = "GroupEditFragment";
    private String groupName;
    private String userId;
    private List<String> memberList;
    private RecyclerView recyclerView;
    private GroupViewAdapter groupViewAdapter;
    private DatabaseReference databaseReference;

    public GroupViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_groupview, container, false);

        // Retrieve the groupName and userId from the arguments
        if (getArguments() != null) {
            groupName = getArguments().getString("groupName");
            userId = getArguments().getString("userId");
        }

        // Initialize the member list
        memberList = new ArrayList<>();

        // Set the group name in the TextView
        TextView groupNameTextView = view.findViewById(R.id.groupName);
        groupNameTextView.setText(groupName); // Set the group name in the TextView

        // Set up the RecyclerView and the GroupEditAdapter
        recyclerView = view.findViewById(R.id.groupRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        groupViewAdapter = new GroupViewAdapter(memberList, groupName);
        recyclerView.setAdapter(groupViewAdapter);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupName);

        // Query the members in the group
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear the member list before populating it with new data
                memberList.clear();

                // Iterate through the members and add them to the memberList
                for (int i = 1; i <= 50; i++) {
                    String memberKey = "member" + i;
                    if (dataSnapshot.hasChild(memberKey)) {
                        String memberId = dataSnapshot.child(memberKey).getValue(String.class);
                        if (memberId != null) {
                            memberList.add(memberId);
                        }
                    }
                }
                // Notify the adapter that the data set has changed
                groupViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
                Log.e(TAG, "Error fetching group members: " + databaseError.getMessage());
            }
        });

        // Handle the back button click to pop the back stack
        ImageView backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pop the back stack when the back button is clicked
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }
}



