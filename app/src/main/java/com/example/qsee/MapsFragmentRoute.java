package com.example.qsee;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MapsFragmentRoute extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Polyline currentRoutePolyline; // Declare a member variable to keep track of the current route polyline
    private Polyline currentBorderPolyline; // Declare a member variable to keep track of the current border polyline
    List<PatternItem> pattern = Arrays.asList(new Dash(30), new Gap(20));

    Double currentUserLocationLat;
    Double currentUserLocationLong;

    Marker currentLocMarker;
    Marker destinationLocMarker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_maps_route, container, false);

        // Initialize the FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapsRoute);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        //mMap.getUiSettings().setCompassEnabled(true);

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
        mMap.setMyLocationEnabled(false);

        /*
        // Set a custom marker for the user's location
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(@NonNull Location location) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .icon(customArrow)
                        .anchor(0.5f, 0.5f) // Adjust the anchor to the center of your custom arrow
                        .rotation(location.getBearing()) // Rotate the arrow to match the user's heading
                        .flat(true) // Make the arrow always face the same direction
                );
            }
        });
         */

        updateMap();


        // Use a Handler to refresh the map every second
        final int INTERVAL = 1000; // 1000 milliseconds = 1 second
        Handler handler = new Handler();
        Runnable mapRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                updateMap(); // Call the method to update the map
                reUpdateMap();
                // manualMap();
            }
        };

        handler.postDelayed(mapRefreshRunnable, 2000); // Schedule it to run again in 1 second


    }

    // Create a method to update the map
    private void updateMap() {
        // Add your code here to update the map
        // This method will be called every time you want to refresh the map

        if (currentLocMarker != null) {
            currentLocMarker.remove();
        }

        BitmapDescriptor customArrow = BitmapDescriptorFactory.fromResource(R.drawable.arrow);

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

        // Get the user's last known location and move the camera there
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                currentUserLocationLat = location.getLatitude();
                currentUserLocationLong = location.getLongitude();
                LatLng userLocation = new LatLng(latitude, longitude);

                // Add a marker to the map using the scaled custom arrow icon
                currentLocMarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude())) // Specify the position of the marker
                        .icon(customArrow) // Use the scaled custom arrow as the marker icon
                        .anchor(0.5f, 0.5f) // Adjust the anchor to the center of your custom arrow
                        .rotation(location.getBearing()) // Rotate the arrow to match the user's heading (if needed)
                        .flat(true) // Make the arrow always face the same direction
                );

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 19));
                        return true;
                    }
                });

                // Add a marker at the user's location
                // mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));

                // Move the camera to the user's location
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            }
        });

        // Receive the values from the Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            // CURRENT USER LOCATION
            //Double userCurrentLatitude = bundle.getDouble("userCurrentLatitude");
            //Double userCurrentLongitude = bundle.getDouble("userCurrentLongitude");

            // DESTINATION LOCATION
            String placeName = bundle.getString("placeName");
            String destinationLatitude = bundle.getString("destinationLatitude");
            String destinationLongitude = bundle.getString("destinationLongitude");

            Double destLatitude = Double.parseDouble(destinationLatitude);
            Double destLongitude = Double.parseDouble(destinationLongitude);

            // Create MarkerOptions or LatLng objects for each place
            LatLng marketLocation = new LatLng(destLatitude, destLongitude);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(marketLocation)
                    .title(placeName);

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
                                    //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));

                                    // Move the camera to the user's location
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 19));   //19
                                }
                            },
                            error -> {
                                // Handle errors in making the request or parsing the response
                                Log.e("Directions Error", error.toString());
                            }
                    );

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------


                    // Create an instance of DirectionsTask and execute it
                    DirectionsTask directionsTask = new DirectionsTask(url);
                    directionsTask.execute();

                    //String jsonResponseString = String.valueOf(directionsTask);
                    //Toast.makeText(getContext(), jsonResponseString, Toast.LENGTH_LONG).show();




    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------


                    // Add the request to the queue
                    queue.add(request);
                }
            });

            /*
            // To remove the marker from the map
            if (currentLocMarker != null) {
                currentLocMarker.remove();
            }
             */
        }
    }

    private void reUpdateMap() {
        // Add your code here to update the map
        // This method will be called every time you want to refresh the map

        if (currentLocMarker != null) {
            currentLocMarker.remove();
        }

        BitmapDescriptor customArrow = BitmapDescriptorFactory.fromResource(R.drawable.arrow);

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

        // Get the user's last known location and move the camera there
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                currentUserLocationLat = location.getLatitude();
                currentUserLocationLong = location.getLongitude();
                LatLng userLocation = new LatLng(latitude, longitude);

                // Add a marker to the map using the scaled custom arrow icon
                currentLocMarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude())) // Specify the position of the marker
                        .icon(customArrow) // Use the scaled custom arrow as the marker icon
                        .anchor(0.5f, 0.5f) // Adjust the anchor to the center of your custom arrow
                        .rotation(location.getBearing()) // Rotate the arrow to match the user's heading (if needed)
                        .flat(true) // Make the arrow always face the same direction
                );

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 19));
                        return true;
                    }
                });

                // Add a marker at the user's location
                // mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));

                // Move the camera to the user's location
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            }
        });

        // Receive the values from the Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            // CURRENT USER LOCATION
            //Double userCurrentLatitude = bundle.getDouble("userCurrentLatitude");
            //Double userCurrentLongitude = bundle.getDouble("userCurrentLongitude");

            // DESTINATION LOCATION
            String placeName = bundle.getString("placeName");
            String destinationLatitude = bundle.getString("destinationLatitude");
            String destinationLongitude = bundle.getString("destinationLongitude");

            Double destLatitude = Double.parseDouble(destinationLatitude);
            Double destLongitude = Double.parseDouble(destinationLongitude);

            // Create MarkerOptions or LatLng objects for each place
            LatLng marketLocation = new LatLng(destLatitude, destLongitude);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(marketLocation)
                    .title(placeName);

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
                                    //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));

                                    // Move the camera to the user's location
                                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 19));   //19
                                }
                            },
                            error -> {
                                // Handle errors in making the request or parsing the response
                                Log.e("Directions Error", error.toString());
                            }
                    );

                    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------


                    // Create an instance of DirectionsTask and execute it
                    DirectionsTask directionsTask = new DirectionsTask(url);
                    directionsTask.execute();

                    //String jsonResponseString = String.valueOf(directionsTask);
                    //Toast.makeText(getContext(), jsonResponseString, Toast.LENGTH_LONG).show();




                    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------


                    // Add the request to the queue
                    queue.add(request);
                }
            });

            /*
            // To remove the marker from the map
            if (currentLocMarker != null) {
                currentLocMarker.remove();
            }
             */
        }
    }

    public class DirectionsTask extends AsyncTask<Void, Void, String> {

        String passedUrl;
        public DirectionsTask(String url) {
            passedUrl = url;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String jsonResponseString = null;
            HttpURLConnection urlConnection = null;
            /*
            String newUrl = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + 14.6440 + "," + 121.0481 +
                    "&destination=" + 14.6485387 + "," + 121.0499399 +
                    "&key=AIzaSyAwTBhjMDtD74Nvqz7eUbN81v93SLhM3IU";
            */

            try {
                URL urlRequest = new URL(passedUrl);
                urlConnection = (HttpURLConnection) urlRequest.openConnection();

                int responseCode = urlConnection.getResponseCode();


                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    jsonResponseString = response.toString();
                }

                else {
                    // Handle the case when the request returns an error
                }
            }

            catch (Exception e) {
                e.printStackTrace();
                // Handle exceptions
            }

            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return jsonResponseString;

        }

        @Override
        protected void onPostExecute(String jsonResponseString) {
            // Use the jsonResponseString in your application as needed

            try {

                JSONObject jsonResponse = new JSONObject(jsonResponseString); // jsonResponseString is the JSON response you received

                // Check the status of the response
                String status = jsonResponse.getString("status");
                if (status.equals("OK")) {
                    JSONArray routes = jsonResponse.getJSONArray("routes");

                    JSONObject routeSample = routes.getJSONObject(0);
                    JSONArray legsSample = routeSample.getJSONArray("legs");
                    JSONObject legSamp = legsSample.getJSONObject(0);
                    JSONArray stepsSample = legSamp.getJSONArray("steps");
                    JSONObject stepSamp = stepsSample.getJSONObject(0);
                    String distanceSample = stepSamp.getJSONObject("distance").getString("text");
                    String htmlInstructionsSample = stepSamp.getString("html_instructions");
                    // Remove HTML tags and display plain text instructions
                    String plainTextInstructions = Html.fromHtml(htmlInstructionsSample).toString();

                    // Populate UI elements with place details
                    TextView textViewDistance = getView().findViewById(R.id.textViewDistance);
                    //textViewDistance.setText(distanceSample);
                    //Toast.makeText(getContext(), distanceSample, Toast.LENGTH_LONG).show();

                    TextView textViewDirection = getView().findViewById(R.id.textViewDirection);
                    textViewDirection.setText(plainTextInstructions);

                    // Extract the numerical value from the distance string
                    double distanceValue = Double.parseDouble(distanceSample.replaceAll("[^0-9.]+", ""));

                    if (distanceSample.contains("km") && distanceValue < 1.0) {
                        // Convert the distance to meters
                        int meters = (int) (distanceValue * 1000);
                        String distanceInMeters = meters + " m";
                        // Use distanceInMeters as needed
                        textViewDistance.setText(distanceInMeters);
                    } else {
                        // Use the original distance string (it's already in meters or more than 1 km)
                        textViewDistance.setText(distanceSample);
                    }




                    for (int i = 0; i < routes.length(); i++) {
                        JSONObject route = routes.getJSONObject(i);

                        JSONArray legs = route.getJSONArray("legs");

                        for (int j = 0; j < legs.length(); j++) {
                            JSONObject leg = legs.getJSONObject(j);

                            JSONArray steps = leg.getJSONArray("steps");

                            for (int k = 0; k < steps.length(); k++) {
                                JSONObject step = steps.getJSONObject(k);

                                // Extract information from the step
                                String distance = step.getJSONObject("distance").getString("text");
                                String duration = step.getJSONObject("duration").getString("text");
                                String htmlInstructions = step.getString("html_instructions");

                                //Toast.makeText(getContext(), distance, Toast.LENGTH_LONG).show();

                                // You can now use the extracted information as needed
                            }
                        }
                    }
                } else {
                    // Handle the case when the API request returns a status other than "OK"
                }

            }

            catch (JSONException e) {
                e.printStackTrace();
                // Handle JSON parsing errors
            }

        }
    }





    // TESTING PURPOSES ONLY --------------------------------------------------------------------------------------------------------------------------------------------------------

    public void manualMap() {

        //Toast.makeText(getContext(), "NEW CURRENT LOC", Toast.LENGTH_LONG).show();

        currentLocMarker.remove();

        BitmapDescriptor customArrow = BitmapDescriptorFactory.fromResource(R.drawable.arrow);

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

        // Get the user's last known location and move the camera there
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                currentUserLocationLat = 14.6060039;
                currentUserLocationLong = 120.9893612;
                LatLng userLocation = new LatLng(14.6060039, 120.9893612);

                // Add a marker to the map using the scaled custom arrow icon
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(currentUserLocationLat, currentUserLocationLong)) // Specify the position of the marker
                        .icon(customArrow) // Use the scaled custom arrow as the marker icon
                        .anchor(0.5f, 0.5f) // Adjust the anchor to the center of your custom arrow
                        .rotation(location.getBearing()) // Rotate the arrow to match the user's heading (if needed)
                        .flat(true) // Make the arrow always face the same direction
                );

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 19));
                        return true;
                    }
                });

                // Add a marker at the user's location
                // mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));

                // Move the camera to the user's location
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            }
        });

        // Receive the values from the Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            // DESTINATION LOCATION
            String placeName = bundle.getString("placeName");
            String destinationLatitude = bundle.getString("destinationLatitude");
            String destinationLongitude = bundle.getString("destinationLongitude");

            Double destLatitude = Double.parseDouble(destinationLatitude);
            Double destLongitude = Double.parseDouble(destinationLongitude);

            // Create MarkerOptions or LatLng objects for each place
            LatLng marketLocation = new LatLng(destLatitude, destLongitude);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(marketLocation)
                    .title(placeName);

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

                    LatLng originLatLng = new LatLng(currentUserLocationLat, currentUserLocationLong);

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
                                    //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));

                                    // Move the camera to the user's location
                                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 15));   //19
                                }
                            },
                            error -> {
                                // Handle errors in making the request or parsing the response
                                Log.e("Directions Error", error.toString());
                            }
                    );

                    // Create an instance of DirectionsTask and execute it
                    DirectionsTask directionsTask = new DirectionsTask(url);
                    directionsTask.execute();

                    // Add the request to the queue
                    queue.add(request);
                }
            });
        }

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
