package com.example.qsee;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.widget.AdapterView; // Add this import for AdapterView
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Retrieve selected categories from Bundle arguments
        Bundle getBundle = getArguments();

        if (getBundle != null) {
            String userID = getBundle.getString("userId");
            Toast.makeText(getContext(), userID, Toast.LENGTH_SHORT).show();
        }

        TextInputLayout locationTextInputLayout = view.findViewById(R.id.searchText);
        AutoCompleteTextView locationAutoCompleteTextView = null; // Get the AutoCompleteTextView -- for search
        if (locationTextInputLayout != null && locationTextInputLayout.getEditText() != null) {
            locationAutoCompleteTextView = (AutoCompleteTextView) locationTextInputLayout.getEditText();
        }

        if (locationAutoCompleteTextView != null) {
            // Fetch data from Firebase Realtime Database
            DatabaseReference locationsRef = FirebaseDatabase.getInstance().getReference().child("Location");
            AutoCompleteTextView finalLocationAutoCompleteTextView = locationAutoCompleteTextView;
            locationsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<String> locationsList = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String location = snapshot.child("Location").getValue(String.class);
                        if (location != null) {
                            locationsList.add(location);
                        }
                    }
                    // Set up the adapter for the AutoCompleteTextView
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, locationsList);
                    finalLocationAutoCompleteTextView.setAdapter(adapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle any errors
                    Log.e("SearchFragment", "Firebase Database Error: " + databaseError.getMessage());
                }
            });

            // Add an item click listener to open the place_detail_fragment
            locationAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String selectedLocation = (String) parent.getItemAtPosition(position);
                    openPlaceDetailFragment(selectedLocation);
                }
            });
        } else {
            Log.e("SearchFragment", "locationAutoCompleteTextView is null");
        }

        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
        // Set the default item as highlighted
        MenuItem defaultItem = bottomNavigationView.getMenu().findItem(R.id.action_search);
        defaultItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.action_home) {
                    loadFragment(new HomeFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                } else if (itemId == R.id.action_search) {
                    loadFragment(new SearchFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                } else if (itemId == R.id.action_maps) {
                    loadFragment(new MapsFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                } else if (itemId == R.id.action_quiz) {
                    loadFragment(new StartQuizFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                } else if (itemId == R.id.action_profile) {
                    loadFragment(new ProfileFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                }
                return true;
            }
        });

        return view;
    }

    private void loadFragment(Fragment fragment) {
        Bundle bundle = new Bundle();

        // Retrieve selected categories from Bundle arguments
        Bundle getBundle = getArguments();

        if (getBundle != null) {
            String userID = getBundle.getString("userId");
            bundle.putString("userId", userID);
            fragment.setArguments(bundle);
        }

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openPlaceDetailFragment(String selectedLocation) {
        // Create the PlaceDetailFragment
        PlaceDetailFragment placeDetailFragment = new PlaceDetailFragment();

        // Pass the selected location to the PlaceDetailFragment
        Bundle bundle = new Bundle();
        bundle.putString("selectedLocation", selectedLocation);
        placeDetailFragment.setArguments(bundle);

        // Replace the current fragment with the PlaceDetailFragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, placeDetailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
