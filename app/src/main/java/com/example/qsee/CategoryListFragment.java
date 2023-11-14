package com.example.qsee;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryListFragment extends Fragment {

    private RecyclerView recyclerView;
    private CategoryListAdapter adapter; // You need to create this adapter
    private String categoryName; // Store the category name here
    private String userId;

    public static CategoryListFragment newInstance(String categoryName, String userId) {
        CategoryListFragment fragment = new CategoryListFragment();
        Bundle args = new Bundle();
        args.putString("categoryName", categoryName);
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categorylist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve the category name from the arguments bundle
        Bundle args = getArguments();
        if (args != null) {
            categoryName = args.getString("categoryName");
            userId = args.getString("userId");

        TextView catName = view.findViewById(R.id.categoryName);
        ImageView backBtn = view.findViewById(R.id.backButton);
        catName.setText(categoryName);

        // Set an OnClickListener for the back button
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the previous fragment (e.g., the search fragment)
                getParentFragmentManager().popBackStack();
            }
        });

        // Find and initialize views
        recyclerView = view.findViewById(R.id.groupRecyclerView);

        // Initialize the RecyclerView with a LinearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);

        // Initialize the adapter (You need to create this adapter)
        adapter = new CategoryListAdapter(requireContext(), getFragmentManager(),userId);
        }
        // Set the adapter to the RecyclerView
        recyclerView.setAdapter(adapter);

        // Now, add the Firebase database query code here to retrieve matching locations
        DatabaseReference locationsRef = FirebaseDatabase.getInstance().getReference().child("Location");
        List<String> matchingLocations = new ArrayList<>();

        locationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear the list when onDataChange is called to avoid duplicates
                List<LocationItem> matchingLocations = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String location = snapshot.child("Location").getValue(String.class);
                    String category = snapshot.child("Category").getValue(String.class);
                    String imageUrl = snapshot.child("Link").getValue(String.class);

                    if (category != null && category.equals(categoryName)) {
                        // If the category matches the passed category, add the location to the list
                        if (location != null) {
                            LocationItem locationItem = new LocationItem(location, imageUrl);
                            matchingLocations.add(locationItem);
                        }
                    }
                }

                // Now, matchingLocations contains all locations with the specified category
                // You can use this list as needed, e.g., update your RecyclerView adapter with these locations
                adapter.setItems(matchingLocations);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors
                Log.e("CategoryListFragment", "Firebase Database Error: " + databaseError.getMessage());
            }
        });
    }
}

