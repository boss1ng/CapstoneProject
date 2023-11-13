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
import android.widget.ImageButton;
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

    boolean isUserInQuezonCity = true;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ImageButton accommodationsButton = view.findViewById(R.id.accomodation);
        ImageButton restaurantButton = view.findViewById(R.id.restaurant);
        ImageButton shoppingButton = view.findViewById(R.id.shopping);
        ImageButton religiousButton = view.findViewById(R.id.religious);
        ImageButton recreationButton = view.findViewById(R.id.recreation);
        ImageButton hospitalButton = view.findViewById(R.id.hospital);
        ImageButton schoolButton = view.findViewById(R.id.school);
        ImageButton attractionButton = view.findViewById(R.id.attraction);
// Retrieve selected categories from Bundle arguments
        Bundle getBundle = getArguments();

        if (getBundle != null) {
            String userID = getBundle.getString("userId");
            Toast.makeText(getContext(), userID, Toast.LENGTH_SHORT).show();

        accommodationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCategoryListFragment("Accommodations",userID);
            }
        });


        restaurantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCategoryListFragment("Restaurants and Cafes",userID);
            }
        });

        shoppingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCategoryListFragment("Shopping",userID);
            }
        });

        religiousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCategoryListFragment("Religious Sites",userID);
            }
        });

        recreationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCategoryListFragment("Sports and Recreation",userID);
            }
        });

        hospitalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCategoryListFragment("Health and Wellness",userID);
            }
        });

        schoolButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCategoryListFragment("Schools",userID);
            }
        });

        attractionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCategoryListFragment("Attractions",userID);
            }
        });


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
                    openPlaceDetailFragment(selectedLocation, userID);
                }
            });
        } else {
            Log.e("SearchFragment", "locationAutoCompleteTextView is null");
        }
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
    private void openCategoryListFragment(String category, String userId) {
        // Create a new instance of CategoryListFragment with the category name and userId
        CategoryListFragment categoryListFragment = CategoryListFragment.newInstance(category, userId);

        // Replace the current fragment with the CategoryListFragment
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, categoryListFragment);
        transaction.addToBackStack(null);
        transaction.commit();
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

    private void openPlaceDetailFragment(String selectedLocation, String userId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Location");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String location = snapshot.child("Location").getValue(String.class);
                    if (location != null && location.equals(selectedLocation)) {
                        String placeAddress = snapshot.child("Address").getValue(String.class);
                        String placeRating = snapshot.child("AverageRate").getValue(String.class);
                        String placeDescription = snapshot.child("Description").getValue(String.class);
                        String placeLink = snapshot.child("Link").getValue(String.class);
                        String highestPrice = snapshot.child("HighestPrice").getValue(String.class);
                        String lowestPrice = snapshot.child("LowestPrice").getValue(String.class);
                        String latitude = snapshot.child("Latitude").getValue(String.class);
                        String longitude = snapshot.child("Longitude").getValue(String.class);
                        String placePrice = "₱" + lowestPrice + " - ₱" + highestPrice;

                        if (lowestPrice != null && highestPrice != null) {
                            if (lowestPrice.equals("") || highestPrice.equals("")) {
                                lowestPrice = "-";
                                highestPrice = "-";
                                placePrice = "-";
                            } else {
                                placePrice = "₱" + lowestPrice + " - ₱" + highestPrice;
                            }
                        }

                        Bundle args = new Bundle();
                        args.putString("userId", userId); // Pass the userId
                        args.putString("placeName", selectedLocation);
                        args.putString("placeAddress", placeAddress);
                        args.putString("placeRating", placeRating);
                        args.putString("placeDescription", placeDescription);
                        args.putString("placeLink", placeLink);
                        args.putString("placePrice", placePrice);
                        args.putString("destinationLatitude", latitude);
                        args.putString("destinationLongitude", longitude);

                        args.putString("isUserInQuezonCity", String.valueOf(isUserInQuezonCity));

                        PlaceDialogSearch placeDialogSearch = new PlaceDialogSearch();
                        placeDialogSearch.setArguments(args);

                        placeDialogSearch.show(getParentFragmentManager(), "PlaceDialogSearch");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors or onCancelled events here
            }
        });
    }
}
