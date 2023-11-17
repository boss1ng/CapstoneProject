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
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;

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
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchFragment extends Fragment {

    private View view;

    boolean isUserInQuezonCity = true;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ImageView background = view.findViewById(R.id.imageView4);
        ImageButton accommodationsButton = view.findViewById(R.id.accomodation);
        ImageButton restaurantButton = view.findViewById(R.id.restaurant);
        ImageButton shoppingButton = view.findViewById(R.id.shopping);
        ImageButton religiousButton = view.findViewById(R.id.religious);
        ImageButton recreationButton = view.findViewById(R.id.recreation);
        ImageButton hospitalButton = view.findViewById(R.id.hospital);
        ImageButton schoolButton = view.findViewById(R.id.school);
        ImageButton attractionButton = view.findViewById(R.id.attraction);

        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/bg_quezonmemorialcircle.jpg?alt=media&token=b297a47e-b875-404c-b5a9-903f1a45c60f").into(background);
        // Retrieve selected categories from Bundle arguments
        Bundle getBundle = getArguments();

        if (getBundle != null) {
            String userID = getBundle.getString("userId");
            //Toast.makeText(getContext(), userID, Toast.LENGTH_LONG).show();

        DatabaseReference destinationsRef = FirebaseDatabase.getInstance().getReference("Location");

            destinationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<DestinationData> destinationList = new ArrayList<>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String ratingStr = snapshot.child("AverageRate").getValue(String.class);
                        if (ratingStr != null) {
                            double rating = Double.parseDouble(ratingStr);
                            if (rating >= 1.0 && rating <= 5.0) {
                                String name = snapshot.child("Location").getValue(String.class);
                                String imageUrl = snapshot.child("Link").getValue(String.class);

                                // Format the rating to one decimal place
                                DecimalFormat df = new DecimalFormat(rating == 0 ? "0" : "#0.0");
                                String formattedRating = df.format(rating);

                                destinationList.add(new DestinationData(name, imageUrl, Double.parseDouble(formattedRating)));
                            }
                        }
                    }

                    // Sort the destinations by rating in descending order
                    Collections.sort(destinationList, (d1, d2) -> Double.compare(d2.getRating(), d1.getRating()));

                    // Take the top 5 destinations (or less if there are fewer than 5)
                    List<DestinationData> top5Destinations = destinationList.subList(0, Math.min(5, destinationList.size()));

                    // After fetching, filtering, and sorting data, update the UI with the top 5 destinations
                    updateUIWithTop5Destinations(top5Destinations, userID);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle database error
                }
            });









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

                    FrameLayout frameLayout = view.findViewById(R.id.searchTab);
                    frameLayout.setVisibility(View.GONE);
                    TextInputLayout textInputLayout = view.findViewById(R.id.searchText);
                    textInputLayout.setVisibility(View.GONE);
                    TextView textView = view.findViewById(R.id.plainText);
                    textView.setVisibility(View.GONE);
                    TextView textView1 = view.findViewById(R.id.plainText2);
                    textView1.setVisibility(View.GONE);
                    HorizontalScrollView horizontalScrollView = view.findViewById(R.id.horizontalScrollView);
                    horizontalScrollView.setVisibility(View.GONE);
                    HorizontalScrollView horizontalScrollView2 = view.findViewById(R.id.horizontalScrollView2);
                    horizontalScrollView2.setVisibility(View.GONE);

                } else if (itemId == R.id.action_search) {
                    loadFragment(new SearchFragment());
                    bottomNavigationView.setVisibility(View.GONE);

                    FrameLayout frameLayout = view.findViewById(R.id.searchTab);
                    frameLayout.setVisibility(View.GONE);
                    TextInputLayout textInputLayout = view.findViewById(R.id.searchText);
                    textInputLayout.setVisibility(View.GONE);
                    TextView textView = view.findViewById(R.id.plainText);
                    textView.setVisibility(View.GONE);
                    TextView textView1 = view.findViewById(R.id.plainText2);
                    textView1.setVisibility(View.GONE);
                    HorizontalScrollView horizontalScrollView = view.findViewById(R.id.horizontalScrollView);
                    horizontalScrollView.setVisibility(View.GONE);
                    HorizontalScrollView horizontalScrollView2 = view.findViewById(R.id.horizontalScrollView2);
                    horizontalScrollView2.setVisibility(View.GONE);

                } else if (itemId == R.id.action_maps) {
                    loadFragment(new MapsFragment());
                    bottomNavigationView.setVisibility(View.GONE);

                    FrameLayout frameLayout = view.findViewById(R.id.searchTab);
                    frameLayout.setVisibility(View.GONE);
                    TextInputLayout textInputLayout = view.findViewById(R.id.searchText);
                    textInputLayout.setVisibility(View.GONE);
                    TextView textView = view.findViewById(R.id.plainText);
                    textView.setVisibility(View.GONE);
                    TextView textView1 = view.findViewById(R.id.plainText2);
                    textView1.setVisibility(View.GONE);
                    HorizontalScrollView horizontalScrollView = view.findViewById(R.id.horizontalScrollView);
                    horizontalScrollView.setVisibility(View.GONE);
                    HorizontalScrollView horizontalScrollView2 = view.findViewById(R.id.horizontalScrollView2);
                    horizontalScrollView2.setVisibility(View.GONE);

                } else if (itemId == R.id.action_quiz) {
                    loadFragment(new StartQuizFragment());
                    bottomNavigationView.setVisibility(View.GONE);

                    FrameLayout frameLayout = view.findViewById(R.id.searchTab);
                    frameLayout.setVisibility(View.GONE);
                    TextInputLayout textInputLayout = view.findViewById(R.id.searchText);
                    textInputLayout.setVisibility(View.GONE);
                    TextView textView = view.findViewById(R.id.plainText);
                    textView.setVisibility(View.GONE);
                    TextView textView1 = view.findViewById(R.id.plainText2);
                    textView1.setVisibility(View.GONE);
                    HorizontalScrollView horizontalScrollView = view.findViewById(R.id.horizontalScrollView);
                    horizontalScrollView.setVisibility(View.GONE);
                    HorizontalScrollView horizontalScrollView2 = view.findViewById(R.id.horizontalScrollView2);
                    horizontalScrollView2.setVisibility(View.GONE);

                } else if (itemId == R.id.action_profile) {
                    loadFragment(new ProfileFragment());
                    bottomNavigationView.setVisibility(View.GONE);

                    FrameLayout frameLayout = view.findViewById(R.id.searchTab);
                    frameLayout.setVisibility(View.GONE);
                    TextInputLayout textInputLayout = view.findViewById(R.id.searchText);
                    textInputLayout.setVisibility(View.GONE);
                    TextView textView = view.findViewById(R.id.plainText);
                    textView.setVisibility(View.GONE);
                    TextView textView1 = view.findViewById(R.id.plainText2);
                    textView1.setVisibility(View.GONE);
                    HorizontalScrollView horizontalScrollView = view.findViewById(R.id.horizontalScrollView);
                    horizontalScrollView.setVisibility(View.GONE);
                    HorizontalScrollView horizontalScrollView2 = view.findViewById(R.id.horizontalScrollView2);
                    horizontalScrollView2.setVisibility(View.GONE);
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

    private void updateUIWithTop5Destinations(List<DestinationData> top5Destinations, String userId) {
        // Get references to the LinearLayout views
        LinearLayout place2Layout = view.findViewById(R.id.Place2);
        LinearLayout place3Layout = view.findViewById(R.id.Place3);
        LinearLayout place4Layout = view.findViewById(R.id.Place4);
        LinearLayout place5Layout = view.findViewById(R.id.Place5);

        // Check if there are any top-rated destinations available
        if (top5Destinations.isEmpty()) {
            // If the list is empty, hide the horizontalScrollView2
            HorizontalScrollView horizontalScrollView2 = view.findViewById(R.id.horizontalScrollView2);
            horizontalScrollView2.setVisibility(View.GONE);

            // Display a message to inform the user
            TextView noDestinationsTextView = view.findViewById(R.id.plainText3);
            noDestinationsTextView.setVisibility(View.VISIBLE);

            // Hide all the LinearLayout views
            place2Layout.setVisibility(View.GONE);
            place3Layout.setVisibility(View.GONE);
            place4Layout.setVisibility(View.GONE);
            place5Layout.setVisibility(View.GONE);
        } else {
            // If there are top-rated destinations, show the horizontalScrollView2
            HorizontalScrollView horizontalScrollView2 = view.findViewById(R.id.horizontalScrollView2);
            horizontalScrollView2.setVisibility(View.VISIBLE);

            // Iterate through the list of top 5 destinations and update the UI
            for (int i = 0; i < top5Destinations.size(); i++) {
                DestinationData destination = top5Destinations.get(i);

                int imageId = getResources().getIdentifier("popDestImage" + (i + 1), "id", getActivity().getPackageName());
                int rateId = getResources().getIdentifier("popDestRate" + (i + 1), "id", getActivity().getPackageName());
                int nameId = getResources().getIdentifier("popDestName" + (i + 1), "id", getActivity().getPackageName());

                ImageView imageView = view.findViewById(imageId);
                TextView rateTextView = view.findViewById(rateId);
                TextView nameTextView = view.findViewById(nameId);

                // Update the UI elements with the fetched data
                nameTextView.setText(destination.getName());
                rateTextView.setText(String.valueOf(destination.getRating()));
                Picasso.get().load(destination.getImageUrl()).into(imageView);

                // Set a click listener for the destination item
                int finalI = i;
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Get the selected destination's name
                        String selectedLocation = destination.getName();
                        // Call the openPlaceDetailFragment method with the selectedLocation
                        openPlaceDetailFragment(selectedLocation, userId);
                    }
                });
            }

            // Show the appropriate number of LinearLayout views based on the size of top5Destinations
            switch (top5Destinations.size()) {
                case 1:
                    place2Layout.setVisibility(View.GONE);
                    place3Layout.setVisibility(View.GONE);
                    place4Layout.setVisibility(View.GONE);
                    place5Layout.setVisibility(View.GONE);
                    break;
                case 2:
                    place2Layout.setVisibility(View.VISIBLE);
                    place3Layout.setVisibility(View.GONE);
                    place4Layout.setVisibility(View.GONE);
                    place5Layout.setVisibility(View.GONE);
                    break;
                case 3:
                    place2Layout.setVisibility(View.VISIBLE);
                    place3Layout.setVisibility(View.VISIBLE);
                    place4Layout.setVisibility(View.GONE);
                    place5Layout.setVisibility(View.GONE);
                    break;
                case 4:
                    place2Layout.setVisibility(View.VISIBLE);
                    place3Layout.setVisibility(View.VISIBLE);
                    place4Layout.setVisibility(View.VISIBLE);
                    place5Layout.setVisibility(View.GONE);
                    break;
                case 5:
                    place2Layout.setVisibility(View.VISIBLE);
                    place3Layout.setVisibility(View.VISIBLE);
                    place4Layout.setVisibility(View.VISIBLE);
                    place5Layout.setVisibility(View.VISIBLE);
                    break;
                default:
                    // Handle more than 5 destinations if needed
                    break;
            }
        }
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
