package com.example.qsee;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    // Create a LatLngBounds that includes Quezon City, Philippines.
    private LatLngBounds QUEZON_CITY = new LatLngBounds(
            new LatLng(14.65, 121.03),      // SW bounds
            new LatLng(14.70, 121.07));     // NE bounds
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private PlacesClient placesClient;

    Double currentUserLocationLat;
    Double currentUserLocationLong;

    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Retrieve selected categories from Bundle arguments
        Bundle getBundle = getArguments();

        if (getBundle != null) {
            String userID = getBundle.getString("userId");
            Toast.makeText(getContext(), userID, Toast.LENGTH_SHORT).show();
            // Toast.makeText(getContext(), getBundle.getString("isVisited"), Toast.LENGTH_SHORT).show();
        }

        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
        // Set the default item as highlighted
        MenuItem defaultItem = bottomNavigationView.getMenu().findItem(R.id.action_maps);
        defaultItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.action_home) {
                    loadFragment(new HomeFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                    LinearLayout layoutFilter = view.findViewById(R.id.filterMenu);
                    layoutFilter.setVisibility(View.GONE);
                    FragmentContainerView fragmentContainerView = view.findViewById(R.id.maps);
                    fragmentContainerView.setVisibility(View.GONE);
                } else if (itemId == R.id.action_search) {
                    loadFragment(new SearchFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                    LinearLayout layoutFilter = view.findViewById(R.id.filterMenu);
                    layoutFilter.setVisibility(View.GONE);
                    FragmentContainerView fragmentContainerView = view.findViewById(R.id.maps);
                    fragmentContainerView.setVisibility(View.GONE);
                } else if (itemId == R.id.action_maps) {
                    loadFragment(new MapsFragment());
                    //BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
                    bottomNavigationView.setVisibility(View.GONE);
                    LinearLayout layoutFilter = view.findViewById(R.id.filterMenu);
                    layoutFilter.setVisibility(View.GONE);
                    FragmentContainerView fragmentContainerView = view.findViewById(R.id.maps);
                    fragmentContainerView.setVisibility(View.GONE);
                } else if (itemId == R.id.action_quiz) {
                    loadFragment(new StartQuizFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                    LinearLayout layoutFilter = view.findViewById(R.id.filterMenu);
                    layoutFilter.setVisibility(View.GONE);
                    FragmentContainerView fragmentContainerView = view.findViewById(R.id.maps);
                    fragmentContainerView.setVisibility(View.GONE);
                } else if (itemId == R.id.action_profile) {
                    loadFragment(new ProfileFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                    LinearLayout layoutFilter = view.findViewById(R.id.filterMenu);
                    layoutFilter.setVisibility(View.GONE);
                    FragmentContainerView fragmentContainerView = view.findViewById(R.id.maps);
                    fragmentContainerView.setVisibility(View.GONE);
                }
                return true;
            }
        });

        // Initialize the FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_api_key));
        }

        // Create a PlacesClient
        placesClient = Places.createClient(requireContext());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.maps);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        /*
        // Initialize AutocompleteSupportFragment
        AutocompleteSupportFragment autocompleteFragment =
                (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        // Specify the types of place data to return (e.g., address, establishment)
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set the filter to restrict the search to a specific type of place (e.g., cities)
        autocompleteFragment.setTypeFilter(TypeFilter.CITIES);

        // Set up a PlaceSelectionListener to handle selected places
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // Handle the selected place
                LatLng location = place.getLatLng();
                // Move the camera to the selected place
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
            }

            @Override
            public void onError(@NonNull Status status) {
                // Handle any errors
            }
        });
         */

        //BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
        //bottomNavigationView.setVisibility(View.INVISIBLE);

        ImageButton filterMenuBar = view.findViewById(R.id.filterMenuBar);
        filterMenuBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                // Find and remove the LinearLayout
                LinearLayout filterMenu = view.findViewById(R.id.filterMenu);
                if (filterMenu != null && filterMenu.getParent() != null) {
                    ((ViewGroup) filterMenu.getParent()).removeView(filterMenu);
                }
                 */

                final Bitmap[] bitmap = {null};

                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        // Capture the screenshot of the map
                        googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                            @Override
                            public void onSnapshotReady(Bitmap snapshot) {
                                // Combine this map snapshot with the parent layout's screenshot

                                FilterCategories fragment = new FilterCategories(snapshot);
                                fragment.setCancelable(false);

                                // Retrieve selected categories from Bundle arguments
                                Bundle getBundle = getArguments();

                                // Use Bundle to pass values
                                Bundle bundle = new Bundle();

                                if (getBundle != null) {
                                    String categoryName = getBundle.getString("categoryName");
                                    bundle.putString("categoryName", categoryName);

                                    String userID = getBundle.getString("userId");
                                    bundle.putString("userId", userID);
                                    fragment.setArguments(bundle);
                                }

                                else {

                                }

                                BottomNavigationView bottomNavigationView = getView().findViewById(R.id.bottomNavigationView);
                                bottomNavigationView.setVisibility(View.GONE);

                                LinearLayout layoutFilter = view.findViewById(R.id.filterMenu);
                                layoutFilter.setVisibility(View.GONE);

                                FragmentContainerView fragmentContainerView = view.findViewById(R.id.maps);
                                fragmentContainerView.setVisibility(View.GONE);

                                // Show the PlaceDetailDialogFragment as a dialog
                                fragment.show(getChildFragmentManager(), "FilterCategories");

                            }
                        });
                    }
                });

                /*
                FragmentContainerView constraintLayout = view.findViewById(R.id.maps);
                constraintLayout.setDrawingCacheEnabled(true);
                Bitmap screenshot = Bitmap.createBitmap(constraintLayout.getDrawingCache());
                constraintLayout.setDrawingCacheEnabled(false);

                // Assuming you have a reference to the FragmentContainerView
                FragmentContainerView fragmentContainerView = view.findViewById(R.id.maps);
                // Capture the screenshot of the FragmentContainerView and other relevant views
                fragmentContainerView.setDrawingCacheEnabled(true);
                Bitmap screenshot = Bitmap.createBitmap(fragmentContainerView.getDrawingCache());
                fragmentContainerView.setDrawingCacheEnabled(false);

                ConstraintLayout constraintLayout = view.findViewById(R.id.fragment_container);
                constraintLayout.setDrawingCacheEnabled(true);
                Bitmap screenshot = Bitmap.createBitmap(constraintLayout.getDrawingCache());
                constraintLayout.setDrawingCacheEnabled(false);

                View viewToCapture = view.findViewById(R.id.fragment_container);; // Replace with the actual view you want to capture
                viewToCapture.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(viewToCapture.getDrawingCache());
                viewToCapture.setDrawingCacheEnabled(false);
                 */

                /*
                // Create a new PlaceDetailDialogFragment and pass the place details as arguments
                FilterCategories fragment = new FilterCategories(bitmap[0]);
                fragment.setCancelable(false);

                // Retrieve selected categories from Bundle arguments
                Bundle getBundle = getArguments();

                // Use Bundle to pass values
                Bundle bundle = new Bundle();

                if (getBundle != null) {
                    String categoryName = getBundle.getString("categoryName");
                    bundle.putString("categoryName", categoryName);

                    String userID = getBundle.getString("userId");
                    bundle.putString("userId", userID);
                    fragment.setArguments(bundle);
                }

                else {

                }

                BottomNavigationView bottomNavigationView = getView().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setVisibility(View.GONE);


                //LinearLayout layoutFilter = view.findViewById(R.id.filterMenu);
                //layoutFilter.setVisibility(View.GONE);

                //FragmentContainerView fragmentContainerView = view.findViewById(R.id.maps);
                //fragmentContainerView.setVisibility(View.GONE);


                // Show the PlaceDetailDialogFragment as a dialog
                fragment.show(getChildFragmentManager(), "FilterCategories");
                 */

            }

        });

        return view;
    }

    private void loadFragment(Fragment fragment) {
        // Use Bundle to pass values
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Constrain the camera target to the Quezon City bounds.
        mMap.setLatLngBoundsForCameraTarget(QUEZON_CITY);

        float minZoomLevel = 13;
        float maxZoomLevel = 16;

        // Set the minimum and maximum zoom levels.
        mMap.setMinZoomPreference(minZoomLevel); // Set the minimum desired zoom level.
        mMap.setMaxZoomPreference(maxZoomLevel); // Set the maximum desired zoom level.

        // Get the maximum zoom level available on the map.
        //float maxZoomLevel = mMap.getMaxZoomLevel();

        // Create a CameraUpdate object to set the zoom level to the maximum.
        //CameraUpdate zoomOut = CameraUpdateFactory.newLatLngZoom(mMap.getCameraPosition().target, maxZoomLevel);

        // Apply the zoomOut update to the map.
        //mMap.animateCamera(zoomOut);

        // Check if location permission is granted
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Request location permission if not granted
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Enable the My Location layer on the map
        mMap.setMyLocationEnabled(true);

        // Get the user's last known location and move the camera there
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                currentUserLocationLat = location.getLatitude();
                currentUserLocationLong = location.getLongitude();
                LatLng userLocation = new LatLng(latitude, longitude);

                // Add a marker at the user's location
                // mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));

                // Move the camera to the user's location
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            }
        });

        // For Reading the Database
        // Initialize Firebase Database reference
        // Reference to the "Location" node in Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Location");

        // Add markers for places retrieved from Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Retrieve selected categories from Bundle arguments
                Bundle bundle = getArguments();
                if (bundle != null) {

                    String visited = bundle.getString("isVisited");
                    String categoryName = bundle.getString("categoryName");

                    /*
                    if (visited == "YES") {
                        Toast.makeText(getContext(), bundle.getString("isVisited"), Toast.LENGTH_SHORT).show();

                        LinearLayout layoutFilter = getView().findViewById(R.id.filterMenu);
                        layoutFilter.setVisibility(View.GONE);

                        FragmentContainerView fragmentContainerView = getView().findViewById(R.id.maps);
                        fragmentContainerView.setVisibility(View.GONE);
                    }
                     */

                    if (categoryName != "") {

                        if (categoryName != null) {

                            // Toast.makeText(getContext(), categoryName, Toast.LENGTH_LONG).show();

                            // Split the string by the '+' character
                            String[] categories = categoryName.split("\\+");

                            // Get the count of the resulting substrings
                            int numberOfCategories = categories.length;

                            boolean noMatchesFound = true; // Assume no matches found initially

                            for (int i = 0; i < categories.length; i++) {

                                String passedCategory = categories[i];

                                for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {

                                    // Extract place data (e.g., latitude, longitude, name) from placeSnapshot
                                    String address = placeSnapshot.child("Address").getValue(String.class);
                                    String name = placeSnapshot.child("Location").getValue(String.class);
                                    String category = placeSnapshot.child("Category").getValue(String.class);
                                    String latitude = placeSnapshot.child("Latitude").getValue(String.class);
                                    String longitude = placeSnapshot.child("Longitude").getValue(String.class);
                                    String stringRating = placeSnapshot.child("AverageRate").getValue(String.class);
                                    String description = placeSnapshot.child("Description").getValue(String.class);
                                    String imageLink = placeSnapshot.child("Link").getValue(String.class);
                                    String lowestPrice = placeSnapshot.child("LowestPrice").getValue(String.class);
                                    String highestPrice = placeSnapshot.child("HighestPrice").getValue(String.class);
                                    String placePrice = "₱" + lowestPrice + " - ₱" + highestPrice;

                                    if (category.equals(passedCategory)) {

                                        noMatchesFound = false;

                                        try {
                                            Double doubleLatitude = Double.parseDouble(latitude);
                                            Double doubleLongitude = Double.parseDouble(longitude);

                                            // Create MarkerOptions or LatLng objects for each place
                                            LatLng location = new LatLng(doubleLatitude, doubleLongitude);

                                            MarkerOptions markerOptions = new MarkerOptions()
                                                    .position(location)
                                                    .title(name)
                                                    //.title(rating)
                                                    .snippet(address + "@" + stringRating + "@" + description + "@" + imageLink + "@" + placePrice + "@" + doubleLatitude + "@" + doubleLongitude);

                                            // Add markers to the Google Map
                                            Marker marker = mMap.addMarker(markerOptions);

                                            // Set a click listener for each marker
                                            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                                @Override
                                                public boolean onMarkerClick(Marker marker) {
                                                    // Handle marker click event here
                                                    // Show place details in your app

                                                    String locationDetails = marker.getSnippet();

                                                    // Split the input string into an array of parts
                                                    String[] parts = locationDetails.split("@");

                                                    // Create a new PlaceDetailDialogFragment and pass the place details as arguments
                                                    PlaceDetailDialogFragment fragment = new PlaceDetailDialogFragment();

                                                    // Use Bundle to pass values
                                                    Bundle args = new Bundle();

                                                    String userID = bundle.getString("userId");
                                                    args.putString("userId", userID);

                                                    args.putString("placeName", marker.getTitle());
                                                    args.putString("placeAddress", parts[0]); // Use the snippet as address
                                                    args.putString("placeRating", parts[1]); // Replace with actual rating
                                                    args.putString("placeDescription", parts[2]);
                                                    args.putString("placeLink", parts[3]);
                                                    args.putString("placePrice", parts[4]);
                                                    args.putDouble("userLatitude", currentUserLocationLat);
                                                    args.putDouble("userLongitude", currentUserLocationLong);
                                                    args.putString("destinationLatitude", parts[5]);
                                                    args.putString("destinationLongitude", parts[6]);

                                                    fragment.setArguments(args);

                                                    //BottomNavigationView bottomNavigationView = getView().findViewById(R.id.bottomNavigationView);
                                                    //bottomNavigationView.setVisibility(View.GONE);

                                                    ImageButton filterMenuBar = getView().findViewById(R.id.filterMenuBar);
                                                    filterMenuBar.setEnabled(false);

                                                    //LinearLayout linearLayout = getView().findViewById(R.id.filterMenu);
                                                    //linearLayout.setVisibility(View.GONE);

                                                    // Show the PlaceDetailDialogFragment as a dialog
                                                    fragment.show(getChildFragmentManager(), "PlaceDetailDialogFragment");

                                                    return true;
                                                }
                                            });

                                        } catch (NumberFormatException e) {
                                            // Handle the case where the String cannot be parsed as a Double
                                            // This can happen if the String is not a valid numeric format
                                        }

                                    }

                                    else {
                                        continue; // You can break out of the loop once category is not found in categories[]

                                /*
                                if (count == categories.length) {

                                    for (int y = 0; y < categories.length; y++) {
                                        if (y == (categories.length-1))
                                            selectedCategories = "and " + categories[y] + ".";
                                        else
                                            selectedCategories = categories[y] + ", ";
                                    }
                                }
                                */

                                    }

                                }

                            }

                            String noCategoryMessage = "";

                            if (noMatchesFound == true) {
                                if (categories.length == 1)
                                    noCategoryMessage = "There is no registered establishments under the selected category.";
                                else
                                    noCategoryMessage = "There is no registered establishments under the selected categories.";

                                //Toast.makeText(getContext(), noCategoryMessage, Toast.LENGTH_LONG).show();
                            }
                        }

                        else {
                            // CALL METHOD
                            populateMap();
                        }

                    }

                    else {
                        //Toast.makeText(getContext(), "NO TEXT PROVIDED", Toast.LENGTH_LONG).show();
                        populateMap();
                    }
                }

                else {
                    populateMap();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // This method is called if there is an error reading from the database
                // Handle the error here
            }
        });
    }

    public void populateMap() {
        // For Reading the Database
        // Initialize Firebase Database reference
        // Reference to the "Location" node in Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Location");

        // Add markers for places retrieved from Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                    // Extract place data (e.g., latitude, longitude, name) from placeSnapshot
                    String address = placeSnapshot.child("Address").getValue(String.class);
                    String name = placeSnapshot.child("Location").getValue(String.class);
                    String category = placeSnapshot.child("Category").getValue(String.class);
                    String latitude = placeSnapshot.child("Latitude").getValue(String.class);
                    String longitude = placeSnapshot.child("Longitude").getValue(String.class);
                    String stringRating = placeSnapshot.child("AverageRate").getValue(String.class);
                    String description = placeSnapshot.child("Description").getValue(String.class);
                    String imageLink = placeSnapshot.child("Link").getValue(String.class);
                    String lowestPrice = placeSnapshot.child("LowestPrice").getValue(String.class);
                    String highestPrice = placeSnapshot.child("HighestPrice").getValue(String.class);
                    String placePrice = "₱" + lowestPrice + " - ₱" + highestPrice;

                    try {
                        Double doubleLatitude = Double.parseDouble(latitude);
                        Double doubleLongitude = Double.parseDouble(longitude);

                        // Create MarkerOptions or LatLng objects for each place
                        LatLng location = new LatLng(doubleLatitude, doubleLongitude);

                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(location)
                                .title(name)
                                //.title(rating)
                                .snippet(address + "@" + stringRating + "@" + description + "@" + imageLink + "@" + placePrice + "@" + doubleLatitude + "@" + doubleLongitude);

                        // Add markers to the Google Map
                        Marker marker = mMap.addMarker(markerOptions);

                        // Set a click listener for each marker
                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                // Handle marker click event here
                                // Show place details in your app

                                String locationDetails = marker.getSnippet();

                                // Split the input string into an array of parts
                                String[] parts = locationDetails.split("@");

                                // Create a new PlaceDetailDialogFragment and pass the place details as arguments
                                PlaceDetailDialogFragment fragment = new PlaceDetailDialogFragment();

                                // Use Bundle to pass values
                                Bundle args = new Bundle();

                                // Retrieve place details from arguments
                                Bundle getBundle = getArguments();

                                if (getBundle != null) {
                                    String userID = getBundle.getString("userId");
                                    args.putString("userId", userID);

                                    args.putString("placeName", marker.getTitle());
                                    args.putString("placeAddress", parts[0]); // Use the snippet as address
                                    args.putString("placeRating", parts[1]); // Replace with actual rating
                                    args.putString("placeDescription", parts[2]);
                                    args.putString("placeLink", parts[3]);
                                    args.putString("placePrice", parts[4]);
                                    args.putDouble("userLatitude", currentUserLocationLat);
                                    args.putDouble("userLongitude", currentUserLocationLong);
                                    args.putString("destinationLatitude", parts[5]);
                                    args.putString("destinationLongitude", parts[6]);
                                    fragment.setArguments(args);
                                }

                                ImageButton filterMenuBar = getView().findViewById(R.id.filterMenuBar);
                                filterMenuBar.setEnabled(false);

                                // Show the PlaceDetailDialogFragment as a dialog
                                fragment.show(getChildFragmentManager(), "PlaceDetailDialogFragment");

                                return true;
                            }
                        });

                    } catch (NumberFormatException e) {
                        // Handle the case where the String cannot be parsed as a Double
                        // This can happen if the String is not a valid numeric format
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
