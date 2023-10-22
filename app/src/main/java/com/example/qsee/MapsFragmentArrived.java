package com.example.qsee;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
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
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapsFragmentArrived extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Polyline currentRoutePolyline; // Declare a member variable to keep track of the current route polyline
    private Polyline currentBorderPolyline; // Declare a member variable to keep track of the current border polyline
    List<PatternItem> pattern = Arrays.asList(new Dash(30), new Gap(20));

    Double currentUserLocationLat;
    Double currentUserLocationLong;
    Double passedCurrentUserLocationLat;
    Double passedCurrentUserLocationLong;
    String destinationLatitude;
    String destinationLongitude;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps_arrived, container, false);

        // Initialize the FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Receive the values from the Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            // Retrieve selected categories from Bundle arguments
            Bundle getBundle = getArguments();

            if (getBundle != null) {
                String placeName = getBundle.getString("placeName");
                TextView textView = view.findViewById(R.id.textViewName);
                textView.setText(placeName);

                passedCurrentUserLocationLat = bundle.getDouble("userCurrentLatitude");
                passedCurrentUserLocationLong = bundle.getDouble("userCurrentLongitude");
                destinationLatitude = getBundle.getString("destinationLatitude");
                destinationLongitude = getBundle.getString("destinationLongitude");
            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.maps);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        ImageButton filterMenuBar = view.findViewById(R.id.filterMenuBar);
        filterMenuBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
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

        /*
        // Get the user's last known location and move the camera there
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                currentUserLocationLat = passedCurrentUserLocationLat;
                currentUserLocationLong = passedCurrentUserLocationLong;
                LatLng userLocation = new LatLng(currentUserLocationLat, currentUserLocationLong);

                // Add a marker at the user's location
                // mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));

                // Move the camera to the user's location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            }
        });
         */

        //BottomNavigationView bottomNavigationView = getView().findViewById(R.id.bottomNavigationView);
        //bottomNavigationView.setVisibility(View.GONE);

        // Receive the values from the Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {

            // CURRENT USER LOCATION
            //Double passedCurrentUserLocationLat = bundle.getDouble("userCurrentLatitude");
            //Double passedCurrentUserLocationLong = bundle.getDouble("userCurrentLongitude");

            // DESTINATION LOCATION
            String placeName = bundle.getString("placeName");
            //String destinationLatitude = bundle.getString("destinationLatitude");
            //String destinationLongitude = bundle.getString("destinationLongitude");

            Double destLatitude = Double.parseDouble(destinationLatitude);
            Double destLongitude = Double.parseDouble(destinationLongitude);

            // Create MarkerOptions or LatLng objects for each place
            LatLng marketLocation = new LatLng(destLatitude, destLongitude);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(marketLocation);
                    //.title(placeName);

            // Add markers to the Google Map
            Marker marker = mMap.addMarker(markerOptions);

            /*
            // Clear the previous route polyline and border polyline if they exist
            if (currentRoutePolyline != null) {
                currentRoutePolyline.remove();
            }
            if (currentBorderPolyline != null) {
                currentBorderPolyline.remove();
            }
             */

            // Get the destination coordinates (latitude and longitude) of the clicked marker
            LatLng destinationLatLng = marker.getPosition();

            // Get your current location using the FusedLocationProviderClient
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentUserLocationLat = passedCurrentUserLocationLat;
                    currentUserLocationLong = passedCurrentUserLocationLong;
                    LatLng originLatLng = new LatLng(currentUserLocationLat, currentUserLocationLong);

                    //LatLng originLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    //Toast.makeText(getContext(), currentUserLocationLat.toString(), Toast.LENGTH_SHORT).show();

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

            // return true;
        }

        //BottomNavigationView bottomNavigationView = getView().findViewById(R.id.bottomNavigationView);
        //bottomNavigationView.setVisibility(View.INVISIBLE);


        // Create a new PlaceDetailDialogFragment and pass the place details as arguments
        MapsFragmentArrivedDialog fragment = new MapsFragmentArrivedDialog();
        fragment.setCancelable(false);

        // Retrieve selected categories from Bundle arguments
        Bundle getBundle = getArguments();
        // Use Bundle to pass values
        Bundle bundlePass = new Bundle();

        if (getBundle != null) {
            String placeName = getBundle.getString("placeName");
            bundlePass.putString("placeName", placeName);

            fragment.setArguments(bundle);
        }

        BottomNavigationView bottomNavigationView = getView().findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setVisibility(View.GONE);

        // Show the PlaceDetailDialogFragment as a dialog
        fragment.show(getChildFragmentManager(), "MapsFragmentArrivedDialog");


        /*
        // In the fragment or activity where you want to navigate
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

        MapsFragment mapsFragment = new MapsFragment();

        BottomNavigationView bottomNavigationView = getView().findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setVisibility(View.GONE);

        // Replace the current fragment with the receiving fragment
        transaction.replace(R.id.fragment_container_arrived, mapsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
         */

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
                        LatLng originLatLng = new LatLng(passedCurrentUserLocationLat, passedCurrentUserLocationLong);

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
