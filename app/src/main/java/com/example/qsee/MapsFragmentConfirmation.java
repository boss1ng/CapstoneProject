package com.example.qsee;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapsFragmentConfirmation extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Polyline currentRoutePolyline; // Declare a member variable to keep track of the current route polyline
    private Polyline currentBorderPolyline; // Declare a member variable to keep track of the current border polyline
    List<PatternItem> pattern = Arrays.asList(new Dash(30), new Gap(20));

    Double currentUserLocationLat;
    Double currentUserLocationLong;

    String placeName;

    String userDestinationLat;

    String userDestinationLong;

    Double passedCurrentUserLocationLat;
    Double passedCurrentUserLocationLong;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps_confirmation, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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

                    ConstraintLayout constraintLayout = getView().findViewById(R.id.constraintParent);
                    constraintLayout.setVisibility(View.GONE);

                    LinearLayout linearLayout = getView().findViewById(R.id.filterMenu);
                    linearLayout.setVisibility(View.GONE);

                    LinearLayout linearLayoutLocation = getView().findViewById(R.id.layoutLocation);
                    linearLayoutLocation.setVisibility(View.GONE);

                    LinearLayout linearLayoutButtons = getView().findViewById(R.id.layoutButtons);
                    linearLayoutButtons.setVisibility(View.GONE);

                    FragmentContainerView fragmentContainerView = getView().findViewById(R.id.maps);
                    fragmentContainerView.setVisibility(View.GONE);
                }

                else if (itemId == R.id.action_search) {
                    loadFragment(new SearchFragment());
                    bottomNavigationView.setVisibility(View.GONE);

                    ConstraintLayout constraintLayout = getView().findViewById(R.id.constraintParent);
                    constraintLayout.setVisibility(View.GONE);

                    LinearLayout linearLayout = getView().findViewById(R.id.filterMenu);
                    linearLayout.setVisibility(View.GONE);

                    LinearLayout linearLayoutLocation = getView().findViewById(R.id.layoutLocation);
                    linearLayoutLocation.setVisibility(View.GONE);

                    LinearLayout linearLayoutButtons = getView().findViewById(R.id.layoutButtons);
                    linearLayoutButtons.setVisibility(View.GONE);

                    FragmentContainerView fragmentContainerView = getView().findViewById(R.id.maps);
                    fragmentContainerView.setVisibility(View.GONE);
                }

                else if (itemId == R.id.action_maps) {
                    loadFragment(new MapsFragment());
                    bottomNavigationView.setVisibility(View.GONE);

                    ConstraintLayout constraintLayout = getView().findViewById(R.id.constraintParent);
                    constraintLayout.setVisibility(View.GONE);

                    LinearLayout linearLayout = getView().findViewById(R.id.filterMenu);
                    linearLayout.setVisibility(View.GONE);

                    LinearLayout linearLayoutLocation = getView().findViewById(R.id.layoutLocation);
                    linearLayoutLocation.setVisibility(View.GONE);

                    LinearLayout linearLayoutButtons = getView().findViewById(R.id.layoutButtons);
                    linearLayoutButtons.setVisibility(View.GONE);

                    FragmentContainerView fragmentContainerView = getView().findViewById(R.id.maps);
                    fragmentContainerView.setVisibility(View.GONE);
                }

                else if (itemId == R.id.action_quiz) {
                    loadFragment(new StartQuizFragment());
                    bottomNavigationView.setVisibility(View.GONE);

                    ConstraintLayout constraintLayout = getView().findViewById(R.id.constraintParent);
                    constraintLayout.setVisibility(View.GONE);

                    LinearLayout linearLayout = getView().findViewById(R.id.filterMenu);
                    linearLayout.setVisibility(View.GONE);

                    LinearLayout linearLayoutLocation = getView().findViewById(R.id.layoutLocation);
                    linearLayoutLocation.setVisibility(View.GONE);

                    LinearLayout linearLayoutButtons = getView().findViewById(R.id.layoutButtons);
                    linearLayoutButtons.setVisibility(View.GONE);

                    FragmentContainerView fragmentContainerView = getView().findViewById(R.id.maps);
                    fragmentContainerView.setVisibility(View.GONE);
                }

                else if (itemId == R.id.action_profile) {
                    loadFragment(new ProfileFragment());
                    bottomNavigationView.setVisibility(View.GONE);

                    ConstraintLayout constraintLayout = getView().findViewById(R.id.constraintParent);
                    constraintLayout.setVisibility(View.GONE);

                    LinearLayout linearLayout = getView().findViewById(R.id.filterMenu);
                    linearLayout.setVisibility(View.GONE);

                    LinearLayout linearLayoutLocation = getView().findViewById(R.id.layoutLocation);
                    linearLayoutLocation.setVisibility(View.GONE);

                    LinearLayout linearLayoutButtons = getView().findViewById(R.id.layoutButtons);
                    linearLayoutButtons.setVisibility(View.GONE);

                    FragmentContainerView fragmentContainerView = getView().findViewById(R.id.maps);
                    fragmentContainerView.setVisibility(View.GONE);
                }
                return true;
            }
        });

        // Initialize the FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Receive the values from the Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            // DESTINATION LOCATION
            placeName = bundle.getString("placeName");
            userDestinationLat = bundle.getString("destinationLatitude");
            userDestinationLong = bundle.getString("destinationLongitude");

            // Populate UI elements with place details
            TextView textViewName = view.findViewById(R.id.textViewName);
            textViewName.setText(placeName);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.maps);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        ImageButton imageButton = view.findViewById(R.id.filterMenuBar);
        imageButton.setEnabled(false);

        Button cancelButton = view.findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // In the fragment or activity where you want to navigate
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                MapsFragment mapsFragment = new MapsFragment();

                // Use Bundle to pass values
                Bundle bundle = new Bundle();

                // Retrieve selected categories from Bundle arguments
                Bundle getBundle = getArguments();

                if (getBundle != null) {
                    String userID = getBundle.getString("userId");
                    bundle.putString("userId", userID);
                    mapsFragment.setArguments(bundle);
                }

                ConstraintLayout constraintLayout = getView().findViewById(R.id.constraintParent);
                constraintLayout.setVisibility(View.GONE);

                LinearLayout linearLayout = getView().findViewById(R.id.filterMenu);
                linearLayout.setVisibility(View.GONE);

                LinearLayout linearLayoutLocation = getView().findViewById(R.id.layoutLocation);
                linearLayoutLocation.setVisibility(View.GONE);

                LinearLayout linearLayoutButtons = getView().findViewById(R.id.layoutButtons);
                linearLayoutButtons.setVisibility(View.GONE);

                FragmentContainerView fragmentContainerView = getView().findViewById(R.id.maps);
                fragmentContainerView.setVisibility(View.GONE);

                //BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setVisibility(View.GONE);

                // Replace the current fragment with the receiving fragment
                transaction.replace(R.id.fragment_container, mapsFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

        });

        Button proceedButton = view.findViewById(R.id.btnProceed);
        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // In the fragment or activity where you want to navigate
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                MapsFragmentRoute mapsFragmentRoute = new MapsFragmentRoute();

                // Retrieve place details from arguments
                Bundle getBundle = getArguments();

                if (getBundle != null) {
                    String userID = getBundle.getString("userId");

                    // Use Bundle to pass values
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", userID);
                    bundle.putString("placeName", placeName);
                    bundle.putDouble("userCurrentLatitude", passedCurrentUserLocationLat);
                    bundle.putDouble("userCurrentLongitude", passedCurrentUserLocationLong);
                    bundle.putString("destinationLatitude", userDestinationLat);
                    bundle.putString("destinationLongitude", userDestinationLong);
                    mapsFragmentRoute.setArguments(bundle);
                }

                ConstraintLayout constraintLayout = getView().findViewById(R.id.constraintParent);
                constraintLayout.setVisibility(View.GONE);

                LinearLayout linearLayout = getView().findViewById(R.id.filterMenu);
                linearLayout.setVisibility(View.GONE);

                LinearLayout linearLayoutLocation = getView().findViewById(R.id.layoutLocation);
                linearLayoutLocation.setVisibility(View.GONE);

                LinearLayout linearLayoutButtons = getView().findViewById(R.id.layoutButtons);
                linearLayoutButtons.setVisibility(View.GONE);

                FragmentContainerView fragmentContainerView = getView().findViewById(R.id.maps);
                fragmentContainerView.setVisibility(View.GONE);

                //BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setVisibility(View.GONE);

                // Replace the current fragment with the receiving fragment
                transaction.replace(R.id.fragment_container, mapsFragmentRoute);
                transaction.addToBackStack(null);
                transaction.commit();
            }

        });

        return view;
    }

    private void loadFragment(Fragment fragment) {
        //Bundle bundle = new Bundle();
        //bundle.putString("userId", userId);
        //fragment.setArguments(bundle);

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

    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

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

        // Receive the values from the Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            // CURRENT USER LOCATION
            passedCurrentUserLocationLat = bundle.getDouble("userCurrentLatitude");
            passedCurrentUserLocationLong = bundle.getDouble("userCurrentLongitude");

            // DESTINATION LOCATION
            String placeName = bundle.getString("placeName");
            String destinationLatitude = bundle.getString("destinationLatitude");
            String destinationLongitude = bundle.getString("destinationLongitude");

            Double destLatitude = Double.parseDouble(destinationLatitude);
            Double destLongitude = Double.parseDouble(destinationLongitude);

            // Create MarkerOptions or LatLng objects for each place
            LatLng marketLocation = new LatLng(destLatitude, destLongitude);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(marketLocation);
                    //.title(placeName);

            // Add markers to the Google Map
            Marker marker = mMap.addMarker(markerOptions);

            // Clear the previous route polyline and border polyline if they exist
            if (currentRoutePolyline != null) {
                currentRoutePolyline.remove();
            }
            if (currentBorderPolyline != null) {
                currentBorderPolyline.remove();
            }

            // Get the destination coordinates (latitude and longitude) of the clicked marker
            LatLng destinationLatLng = marker.getPosition();

            // Get your current location using the FusedLocationProviderClient
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng originLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    // Use Google Directions API to request directions
                    String apiKey = getString(R.string.google_maps_api_key);
                    String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                            "origin=" + originLatLng.latitude + "," + originLatLng.longitude +
                            "&destination=" + destinationLatLng.latitude + "," + destinationLatLng.longitude +
                            "&key=" + apiKey;

                    // Make an HTTP request to the Directions API
                    RequestQueue queue = Volley.newRequestQueue(requireContext());

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                            response -> {
                                // Parse the JSON response to extract the route information
                                List<LatLng> points = parseDirectionsResponse(response);

                                // Draw the border line on the map with color "#1967d2" (slightly wider)
                                if (points != null) {
                                    PolylineOptions borderOptions = new PolylineOptions()
                                            .addAll(points)
                                            .width(20) // Adjust the width as needed for the border
                                            .color(Color.parseColor("#1967d2")); // Set color to "#1967d2" for the border
                                    currentBorderPolyline = mMap.addPolyline(borderOptions);

                                    // Draw the solid route line on the map with color "#00b0ff"
                                    PolylineOptions routeOptions = new PolylineOptions()
                                            .addAll(points)
                                            .width(14) // Adjust the width as needed for the route
                                            .color(Color.parseColor("#00b0ff")); // Set color to "#00b0ff" for the route
                                    currentRoutePolyline = mMap.addPolyline(routeOptions);

                                    // Move the camera to fit the bounds of the new route
                                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                    builder.include(originLatLng);
                                    builder.include(destinationLatLng);
                                    LatLngBounds bounds = builder.build();
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 250));
                                }
                            },
                            error -> {
                                // Handle errors in making the request or parsing the response
                                Log.e("Directions Error", error.toString());
                            }
                    );

                    // Add the request to the queue
                    queue.add(request);
                }
            });

            Button proceedButton = getView().findViewById(R.id.btnProceed);
            proceedButton.setEnabled(true);

            // return true;
        }

        /*
        // Set a click listener for each marker
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Clear the previous route polyline and border polyline if they exist
                if (currentRoutePolyline != null) {
                    currentRoutePolyline.remove();
                }
                if (currentBorderPolyline != null) {
                    currentBorderPolyline.remove();
                }

                // Get the destination coordinates (latitude and longitude) of the clicked marker
                LatLng destinationLatLng = marker.getPosition();

                // Get your current location using the FusedLocationProviderClient
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng originLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                        // Use Google Directions API to request directions
                        String apiKey = getString(R.string.google_maps_api_key);
                        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                                "origin=" + originLatLng.latitude + "," + originLatLng.longitude +
                                "&destination=" + destinationLatLng.latitude + "," + destinationLatLng.longitude +
                                "&key=" + apiKey;

                        // Make an HTTP request to the Directions API
                        RequestQueue queue = Volley.newRequestQueue(requireContext());

                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                                response -> {
                                    // Parse the JSON response to extract the route information
                                    List<LatLng> points = parseDirectionsResponse(response);

                                    // Draw the border line on the map with color "#1967d2" (slightly wider)
                                    if (points != null) {
                                        PolylineOptions borderOptions = new PolylineOptions()
                                                .addAll(points)
                                                .width(16) // Adjust the width as needed for the border
                                                .color(Color.parseColor("#1967d2")); // Set color to "#1967d2" for the border
                                        currentBorderPolyline = mMap.addPolyline(borderOptions);

                                        // Draw the solid route line on the map with color "#00b0ff"
                                        PolylineOptions routeOptions = new PolylineOptions()
                                                .addAll(points)
                                                .width(14) // Adjust the width as needed for the route
                                                .color(Color.parseColor("#00b0ff")); // Set color to "#00b0ff" for the route
                                        currentRoutePolyline = mMap.addPolyline(routeOptions);

                                        // Move the camera to fit the bounds of the new route
                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                        builder.include(originLatLng);
                                        builder.include(destinationLatLng);
                                        LatLngBounds bounds = builder.build();
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                                    }
                                },
                                error -> {
                                    // Handle errors in making the request or parsing the response
                                    Log.e("Directions Error", error.toString());
                                }
                        );

                        // Add the request to the queue
                        queue.add(request);
                    }
                });

                return true;
            }
        });
        */


        // ROUTES


    }

    // Method to parse the Directions API response and extract the route points
    private List<LatLng> parseDirectionsResponse(JSONObject response) {
        List<LatLng> points = new ArrayList<>();

        try {
            JSONArray routes = response.getJSONArray("routes");
            if (routes.length() > 0) {
                JSONObject route = routes.getJSONObject(0);
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String encodedPolyline = overviewPolyline.getString("points");

                // Decode the polyline to get a list of LatLng points
                points = decodePolyline(encodedPolyline);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return points;
    }

    // Method to decode an encoded polyline into a list of LatLng points
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> points = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng point = new LatLng((lat / 1E5), (lng / 1E5));
            points.add(point);
        }

        return points;
    }

}
