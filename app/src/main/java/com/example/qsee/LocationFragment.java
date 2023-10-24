package com.example.qsee;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class LocationFragment extends Fragment {

    private String userId;
    private static final String TAG = "LocationFragment"; // Added TAG constant

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_list, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Retrieve the username from the arguments
        Bundle args = getArguments();
        if (args != null) {
            userId = args.getString("userId");
        }


        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Itinerary");

        // Assuming you have a RecyclerView named "groupRecyclerView"
        RecyclerView recyclerView = view.findViewById(R.id.groupRecyclerView);
        List<Location> locationList = new ArrayList<>(); // Corrected import for Location class

        // Create an adapter for your RecyclerView
        LocationAdapter locationAdapter = new LocationAdapter(locationList, userId, getContext());

        // Set the adapter to the RecyclerView
        recyclerView.setAdapter(locationAdapter);

        // Set a layout manager to position your items
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Add a ValueEventListener to fetch the data
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                locationList.clear();
                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    if (locationSnapshot.child("admin").getValue(String.class).equals(userId)) {
                        String iterName = locationSnapshot.child("iterName").getValue(String.class);
                        String admin = locationSnapshot.child("admin").getValue(String.class);
                        Location location = new Location();
                        location.setLocationAdmin(admin);
                        location.setLocationName(iterName);
                        locationList.add(location);
                    }
                }
                locationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });




        // Find the "addLocationBtn" button
        FloatingActionButton addLocationBtn = view.findViewById(R.id.addLocationBtn);

        // Set a click listener for the button
        addLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the dialog fragment
                LocationDialogFragment dialogFragment = new LocationDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);
                dialogFragment.setArguments(bundle);
                FragmentManager fragmentManager = getParentFragmentManager();
                dialogFragment.show(fragmentManager, "LocationDialogFragment");
            }
        });

        return view;
    }
}
