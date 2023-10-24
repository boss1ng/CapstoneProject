package com.example.qsee;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.*;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
            public void onPlaceSelected(@NonNull com.google.android.libraries.places.api.model.Place place) {
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

                // Create a new PlaceDetailDialogFragment and pass the place details as arguments
                FilterCategories fragment = new FilterCategories();
                fragment.setCancelable(false);

                // Retrieve selected categories from Bundle arguments
                Bundle getBundle = getArguments();

                // Use Bundle to pass values
                Bundle bundle = new Bundle();

                if (getBundle != null) {
                    String categoryName = getBundle.getString("categoryName");
                    bundle.putString("categoryName", categoryName);
                    fragment.setArguments(bundle);
                }

                else {

                }

                BottomNavigationView bottomNavigationView = getView().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setVisibility(View.GONE);

                // Show the PlaceDetailDialogFragment as a dialog
                fragment.show(getChildFragmentManager(), "FilterCategories");

            }

        });

        return view;
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
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
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

                    String categoryName = bundle.getString("categoryName");
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
                                                    Bundle args = new Bundle();
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

                                                    BottomNavigationView bottomNavigationView = getView().findViewById(R.id.bottomNavigationView);
                                                    bottomNavigationView.setVisibility(View.GONE);

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
                                Bundle args = new Bundle();
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
