package com.example.qsee;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    // Create a LatLngBounds that includes Quezon City, Philippines.

    // 13
    private LatLngBounds QUEZON_CITY_13 = new LatLngBounds(
            new LatLng(14.637, 121.02),      // SW bounds
            new LatLng(14.7289, 121.103));     // NE bounds
    // 14
    private LatLngBounds QUEZON_CITY_14 = new LatLngBounds(
            new LatLng(14.61, 121.004),      // SW bounds      .. bawas->bababa .. bawas->kakaliwa
            new LatLng(14.757, 121.123));     // NE bounds     .. dagdag->tataas .. dagdag->kakanan
    // 15
    private LatLngBounds QUEZON_CITY_15 = new LatLngBounds(
            new LatLng(14.597, 120.995),      // SW bounds
            new LatLng(14.7675, 121.129));     // NE bounds
    // 16
    private LatLngBounds QUEZON_CITY_16 = new LatLngBounds(
            new LatLng(14.592, 120.99),      // SW bounds
            new LatLng(14.7735, 121.133));     // NE bounds

    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private PlacesClient placesClient;

    Double currentUserLocationLat;
    Double currentUserLocationLong;
    private double placeLatitude = 0.0;
    private double placeLongitude = 0.0;

    boolean isUserInQuezonCity = true;

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
            //Toast.makeText(getContext(), userID, Toast.LENGTH_LONG).show();
            // Toast.makeText(getContext(), getBundle.getString("isVisited"), Toast.LENGTH_LONG).show();
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
                    FragmentContainerView fragmentContainerView = view.findViewById(R.id.mapsFragment);
                    fragmentContainerView.setVisibility(View.GONE);
                } else if (itemId == R.id.action_search) {
                    loadFragment(new SearchFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                    LinearLayout layoutFilter = view.findViewById(R.id.filterMenu);
                    layoutFilter.setVisibility(View.GONE);
                    FragmentContainerView fragmentContainerView = view.findViewById(R.id.mapsFragment);
                    fragmentContainerView.setVisibility(View.GONE);
                } else if (itemId == R.id.action_maps) {
                    loadFragment(new MapsFragment());
                    //BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
                    bottomNavigationView.setVisibility(View.GONE);
                    LinearLayout layoutFilter = view.findViewById(R.id.filterMenu);
                    layoutFilter.setVisibility(View.GONE);
                    FragmentContainerView fragmentContainerView = view.findViewById(R.id.mapsFragment);
                    fragmentContainerView.setVisibility(View.GONE);
                } else if (itemId == R.id.action_quiz) {
                    loadFragment(new StartQuizFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                    LinearLayout layoutFilter = view.findViewById(R.id.filterMenu);
                    layoutFilter.setVisibility(View.GONE);
                    FragmentContainerView fragmentContainerView = view.findViewById(R.id.mapsFragment);
                    fragmentContainerView.setVisibility(View.GONE);
                } else if (itemId == R.id.action_profile) {
                    loadFragment(new ProfileFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                    LinearLayout layoutFilter = view.findViewById(R.id.filterMenu);
                    layoutFilter.setVisibility(View.GONE);
                    FragmentContainerView fragmentContainerView = view.findViewById(R.id.mapsFragment);
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
                .findFragmentById(R.id.mapsFragment);
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

                                FragmentContainerView fragmentContainerView = view.findViewById(R.id.mapsFragment);
                                fragmentContainerView.setVisibility(View.GONE);

                                // Show the PlaceDetailDialogFragment as a dialog
                                fragment.show(getChildFragmentManager(), "FilterCategories");

                            }
                        });
                    }
                });
            }
        });

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        String userID = sharedPreferences.getString("user", "0");
        String destinationLatitude = sharedPreferences.getString("destinationLatitude", "0.0");
        String destinationLongitude = sharedPreferences.getString("destinationLongitude", "0.0");
        String originLatitude = sharedPreferences.getString("originLatitude", "0.0");
        String originLongitude = sharedPreferences.getString("originLongitude", "0.0");

        //Toast.makeText(getContext(), "UserID: " + userID, Toast.LENGTH_LONG).show();
        //Toast.makeText(getContext(), "Dest LATITUDE: " + destinationLatitude, Toast.LENGTH_LONG).show();
        //Toast.makeText(getContext(), "Dest LONGITUDE: " + destinationLongitude, Toast.LENGTH_LONG).show();

        if (!(userID.equals("0")) || !(destinationLatitude.equals("0.0")) || !(destinationLongitude.equals("0.0")) || !(originLatitude.equals("0.0")) || !(originLongitude.equals("0.0"))) {

            if (getBundle != null) {
                String passUserID = getBundle.getString("userId");

                if (passUserID.equals(userID)) {
                    MapsFragmentResume mapsFragmentResume = new MapsFragmentResume();
                    mapsFragmentResume.setCancelable(false);

                    String placeName = getBundle.getString("placeName");

                    // Use Bundle to pass values
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", passUserID);
                    bundle.putString("placeName", placeName);
                    bundle.putString("userCurrentLatitude", originLatitude);
                    bundle.putString("userCurrentLongitude", originLongitude);
                    bundle.putString("destinationLatitude", destinationLatitude);
                    bundle.putString("destinationLongitude", destinationLongitude);
                    mapsFragmentResume.setArguments(bundle);

                    mapsFragmentResume.show(getChildFragmentManager(), "MapsFragmentResume");
                }
            }
        }

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
        //mMap.setLatLngBoundsForCameraTarget(QUEZON_CITY);

        float minZoomLevel = 13;
        float maxZoomLevel = 16;

        // Set the minimum and maximum zoom levels.
        mMap.setMinZoomPreference(minZoomLevel); // Set the minimum desired zoom level.
        mMap.setMaxZoomPreference(maxZoomLevel); // Set the maximum desired zoom level.


// RESTRICTION TO QUEZON CITY.
///*
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            float currentZoomLevel = mMap.getCameraPosition().zoom;
            @Override
            public void onCameraMove() {
                CameraPosition cameraPosition = mMap.getCameraPosition();
                if(cameraPosition.zoom >= 13.0 && cameraPosition.zoom < 14.0) {
                    mMap.setLatLngBoundsForCameraTarget(QUEZON_CITY_13);
                }

                else if(cameraPosition.zoom >= 14.0 && cameraPosition.zoom < 15.0) {
                    mMap.setLatLngBoundsForCameraTarget(QUEZON_CITY_14);
                }

                else if(cameraPosition.zoom >= 15.0 && cameraPosition.zoom < 16.0) {
                    mMap.setLatLngBoundsForCameraTarget(QUEZON_CITY_15);
                }

                else {
                    mMap.setLatLngBoundsForCameraTarget(QUEZON_CITY_16);
                }
            }
        });
//*/

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
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 13));


                Bundle args = getArguments();
                if (args != null) {
                    //placeLatitude = args.getDouble("placeLatitude", currentUserLocationLat);
                    //placeLongitude = args.getDouble("placeLongitude", currentUserLocationLong);

                    placeLatitude = args.getDouble("placeLatitude");
                    placeLongitude = args.getDouble("placeLongitude");

                    if (placeLatitude == 0.0 && placeLongitude == 0.0) {
                        // IF RESTRICTION TO QUEZON CITY IS REMOVED, change to 15.
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15)); // 15 minZoomLevel
                    }

                    else {
                        // Log the placeLatitude and placeLongitude values
                        Log.d("Debug", "placeLatitude: " + placeLatitude);
                        Log.d("Debug", "placeLongitude: " + placeLongitude);

                        // Create a LatLng object with the place's latitude and longitude
                        LatLng placeLatLng = new LatLng(placeLatitude, placeLongitude);

                        // Move the camera to the specified location and set an appropriate zoom level
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, maxZoomLevel)); // Adjust the zoom level as needed
                    }
                }


                // RESTRICTION TO QUEZON CITY.
///*
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, minZoomLevel));

                Geocoder geocoder = new Geocoder(getContext());

                try {
                    List<android.location.Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                    if (addresses != null && addresses.size() > 0) {
                        Address address = addresses.get(0);

                        // You can now extract address components
                        String completeAddress = address.getAddressLine(0); // Full street address
                        String city = address.getLocality();
                        String state = address.getAdminArea();
                        String postalCode = address.getPostalCode();
                        String country = address.getCountryName();

                        /*
                        https://quezoncity.com/000001a/000001b/qc+links/backups/quezon+city+zip+code+6.html
                        1105	1102	1106	1116	1109	1111	1119	1110	1115	1126	1120	1101	1117	1100	1121
                        1128	1104	1112	1123	1113	1125	1118	1122	1114	1127	1124	1103	1108	1107
                         */

                        if (completeAddress.contains("Metro Manila")) {

                            if (completeAddress.contains("1105") || completeAddress.contains("1102") || completeAddress.contains("1106") || completeAddress.contains("1116") ||
                                    completeAddress.contains("1109") || completeAddress.contains("1111") || completeAddress.contains("1119") || completeAddress.contains("1110") ||
                                    completeAddress.contains("1115") || completeAddress.contains("1126") || completeAddress.contains("1120") || completeAddress.contains("1101") ||
                                    completeAddress.contains("1117") || completeAddress.contains("1100") || completeAddress.contains("1121") || completeAddress.contains("1128") ||
                                    completeAddress.contains("1104") || completeAddress.contains("1112") || completeAddress.contains("1123") || completeAddress.contains("1113") ||
                                    completeAddress.contains("1125") || completeAddress.contains("1118") || completeAddress.contains("1122") || completeAddress.contains("1114") ||
                                    completeAddress.contains("1127") || completeAddress.contains("1124") || completeAddress.contains("1103") || completeAddress.contains("1108") ||
                                    completeAddress.contains("1107") || completeAddress.contains("Quezon City")) {

                                isUserInQuezonCity = true;
                            }
                            else
                                isUserInQuezonCity = false;
                        }
                        else
                            isUserInQuezonCity = false;

                    } else {
                        // Geocoder couldn't find an address for the given latitude and longitude
                    }
                } catch (IOException e) {
                    // Handle geocoding errors (e.g., network issues, service not available)
                    throw new RuntimeException(e);
                }

                //isUserInQuezonCity = QUEZON_CITY_13.contains(new LatLng(latitude, longitude));

                if (isUserInQuezonCity) {
                    // The user is within Quezon City
                    // You can perform specific actions or display messages as needed.
                    //Toast.makeText(getContext(), "WITHIN", Toast.LENGTH_LONG).show();
                } else {
                    // The user is outside Quezon City
                    // You can handle this case accordingly.
                    //Toast.makeText(getContext(), "OUTSIDE", Toast.LENGTH_LONG).show();
                    Toast.makeText(getContext(), "You are outside Quezon City.", Toast.LENGTH_LONG).show();

                    // Create a Handler to introduce a delay
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Display the second Toast with LENGTH_LONG duration after a delay
                            Toast.makeText(getContext(), "You won't be able to route.", Toast.LENGTH_LONG).show();
                        }
                    }, 3500); // 2000 milliseconds (2 seconds) delay
                }
//*/


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
                        Toast.makeText(getContext(), bundle.getString("isVisited"), Toast.LENGTH_LONG).show();

                        LinearLayout layoutFilter = getView().findViewById(R.id.filterMenu);
                        layoutFilter.setVisibility(View.GONE);

                        FragmentContainerView fragmentContainerView = getView().findViewById(R.id.mapsFragment);
                        fragmentContainerView.setVisibility(View.GONE);
                    }
                     */

                    if (categoryName != "") {

                        if (categoryName != null) {

                            //Toast.makeText(getContext(), categoryName, Toast.LENGTH_LONG).show();

                            // Split the string by the '+' character
                            String[] categories = categoryName.split("\\+");

                            // Get the count of the resulting substrings
                            int numberOfCategories = categories.length;

                            boolean noMatchesFound = true; // Assume no matches found initially

                            for (int i = 0; i < categories.length; i++) {

                                String passedCategory = categories[i];
                                String description = null;
                                String lowestPrice = null;
                                String highestPrice = null;
                                String placePrice = null;

                                for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {

                                    // Extract place data (e.g., latitude, longitude, name) from placeSnapshot
                                    String address = placeSnapshot.child("Address").getValue(String.class);
                                    String name = placeSnapshot.child("Location").getValue(String.class);
                                    String category = placeSnapshot.child("Category").getValue(String.class);
                                    String latitude = placeSnapshot.child("Latitude").getValue(String.class);
                                    String longitude = placeSnapshot.child("Longitude").getValue(String.class);
                                    String stringRating = placeSnapshot.child("AverageRate").getValue(String.class);

                                    if (placeSnapshot.child("Description").getValue(String.class) == null)
                                        description = "-";
                                    else
                                        description = placeSnapshot.child("Description").getValue(String.class);

                                    if (placeSnapshot.child("LowestPrice").getValue(String.class) == null || placeSnapshot.child("HighestPrice").getValue(String.class) == null) {
                                        lowestPrice = "-";
                                        highestPrice = "-";
                                        placePrice = "-";
                                    }
                                    else {
                                        lowestPrice = placeSnapshot.child("LowestPrice").getValue(String.class);
                                        highestPrice = placeSnapshot.child("HighestPrice").getValue(String.class);
                                        placePrice = "₱" + lowestPrice + " - ₱" + highestPrice;
                                    }

                                    String imageLink = placeSnapshot.child("Link").getValue(String.class);

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

                                                    args.putString("isUserInQuezonCity", String.valueOf(isUserInQuezonCity));

                                                    /*
                                                    if (isUserInQuezonCity) {
                                                        // The user is within Quezon City
                                                        // You can perform specific actions or display messages as needed.
                                                        Toast.makeText(getContext(), "WITHIN", Toast.LENGTH_LONG).show();
                                                        args.putString("isUserInQuezonCity", "TRUE");
                                                    } else {
                                                        // The user is outside Quezon City
                                                        // You can handle this case accordingly.
                                                        args.putString("isUserInQuezonCity", "FALSE");
                                                    }
                                                    */

                                                    fragment.setArguments(args);

                                                    //BottomNavigationView bottomNavigationView = getView().findViewById(R.id.bottomNavigationView);
                                                    //=bottomNavigationView.setVisibility(View.GONE);

                                                    //FragmentContainerView fragmentContainerView = getView().findViewById(R.id.mapsFragment);
                                                    //fragmentContainerView.setVisibility(View.GONE);

                                                    ImageButton filterMenuBar = getView().findViewById(R.id.filterMenuBar);
                                                    filterMenuBar.setEnabled(false);

                                                    //LinearLayout linearLayout = getView().findViewById(R.id.filterMenu);
                                                    //linearLayout.setVisibility(View.GONE);

                                                    // Detach the MapsFragment
                                                    //getChildFragmentManager().beginTransaction().detach(MapsFragment.this).commit();

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

                                Toast.makeText(getContext(), noCategoryMessage, Toast.LENGTH_LONG).show();
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

                String description = null;
                String lowestPrice = null;
                String highestPrice = null;
                String placePrice = null;

                for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                    // Extract place data (e.g., latitude, longitude, name) from placeSnapshot
                    String address = placeSnapshot.child("Address").getValue(String.class);
                    String name = placeSnapshot.child("Location").getValue(String.class);
                    String category = placeSnapshot.child("Category").getValue(String.class);
                    String latitude = placeSnapshot.child("Latitude").getValue(String.class);
                    String longitude = placeSnapshot.child("Longitude").getValue(String.class);
                    String stringRating = placeSnapshot.child("AverageRate").getValue(String.class);

                    if (placeSnapshot.child("Description").getValue(String.class) != null) {
                        if (placeSnapshot.child("Description").getValue(String.class).equals(""))
                            description = "-";
                        else
                            description = placeSnapshot.child("Description").getValue(String.class);
                    }

                    if (placeSnapshot.child("LowestPrice").getValue(String.class) != null && placeSnapshot.child("HighestPrice").getValue(String.class) != null) {
                        if (placeSnapshot.child("LowestPrice").getValue(String.class).equals("") || placeSnapshot.child("HighestPrice").getValue(String.class).equals("")) {
                            lowestPrice = "-";
                            highestPrice = "-";
                            placePrice = "-";
                        }
                        else {
                            lowestPrice = placeSnapshot.child("LowestPrice").getValue(String.class);
                            highestPrice = placeSnapshot.child("HighestPrice").getValue(String.class);
                            placePrice = "₱" + lowestPrice + " - ₱" + highestPrice;
                        }
                    }

                    String imageLink = placeSnapshot.child("Link").getValue(String.class);

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

                        Bundle args = getArguments();
                        if (args != null) {
                            placeLatitude = args.getDouble("placeLatitude");
                            placeLongitude = args.getDouble("placeLongitude");

                            if (doubleLatitude == placeLatitude && doubleLongitude == placeLongitude) {
                                // Set a different color for the marker (e.g., red)
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                            } else {
                                // Set a default color for the marker (e.g., blue)
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            }
                        }

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

                                    args.putString("isUserInQuezonCity", String.valueOf(isUserInQuezonCity));

                                    fragment.setArguments(args);
                                }

                                //FragmentContainerView fragmentContainerView = getView().findViewById(R.id.mapsFragment);
                                //fragmentContainerView.setVisibility(View.GONE);

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
