package com.example.qsee;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


public class LocationFragment extends Fragment {

    private TextView noLocationsTextView;
    private ImageView noItinerary;
    private String userId;
    private static final String TAG = "LocationFragment"; // Added TAG constant

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_list, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Retrieve the username from the arguments
        Bundle args = getArguments();
        if (args != null) {
            userId = args.getString("userId");
        }

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Groups");

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

                // Query the Itinerary node for all entries
                DatabaseReference itineraryRef = database.getReference("Itinerary");
                itineraryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot itineraryDataSnapshot) {
                        // Loop through each group
                        for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                            // Check if the userId exists in any of the member fields
                            for (int i = 1; i <= 50; i++) {
                                String memberKey = "member" + i;
                                if (groupSnapshot.child(memberKey).exists() && groupSnapshot.child(memberKey).getValue(String.class).equals(userId)) {
                                    // Get the groupName
                                    String groupName = groupSnapshot.child("groupName").getValue(String.class);

                                    for (DataSnapshot itinerarySnapshot : itineraryDataSnapshot.getChildren()) {
                                        // Check if the itinerary has the same admin and the same groupName or just the same userId
                                        String admin = itinerarySnapshot.child("admin").getValue(String.class);
                                        String iterName = itinerarySnapshot.child("iterName").getValue(String.class);
                                        String itineraryGroupName = itinerarySnapshot.child("groupName").getValue(String.class);

                                        if ((admin != null && admin.equals(userId)) || (itineraryGroupName != null && itineraryGroupName.equals(groupName))) {
                                            // Check if the entry already exists in the locationList
                                            boolean isAlreadyAdded = false;
                                            for (Location loc : locationList) {
                                                if (loc.getLocationAdmin().equals(admin) && loc.getLocationName().equals(iterName)) {
                                                    isAlreadyAdded = true;
                                                    break;
                                                }
                                            }

                                            // If not already added, add the entry to locationList
                                            if (!isAlreadyAdded) {
                                                Location location = new Location();
                                                location.setLocationAdmin(admin);
                                                location.setLocationName(iterName);
                                                locationList.add(location);
                                            }
                                        }
                                    }
                                    break; // Exit the loop once the userId is found in a member field
                                }
                            }
                        }
                        locationAdapter.notifyDataSetChanged();

                        // Update the visibility of the "No Locations Added" TextView
                        if (locationList.isEmpty()) {
                            noLocationsTextView.setVisibility(View.VISIBLE);
                            noItinerary.setVisibility(View.VISIBLE);
                        } else {
                            noLocationsTextView.setVisibility(View.GONE);
                            noItinerary.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException());
                    }
                });
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

        // Initialize the "No Locations Added" TextView
        noLocationsTextView = view.findViewById(R.id.noLocationsTextView);
        noItinerary = view.findViewById(R.id.noItinerary);

        return view;
    }
}
