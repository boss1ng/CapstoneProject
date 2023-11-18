package com.example.qsee;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MapsFragmentRoute extends Fragment implements OnMapReadyCallback, MapsInstructions.OnStopButtonClickListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    int isDisconnected = 0;

    private Polyline previousCurrentRoutePolyline = null;
    private Polyline previousCurrentBorderPolyline = null;
    private Polyline currentRoutePolyline = null; // Declare a member variable to keep track of the current route polyline
    private Polyline currentBorderPolyline = null; // Declare a member variable to keep track of the current border polyline
    List<PatternItem> pattern = Arrays.asList(new Dash(30), new Gap(20));

    Double currentUserLocationLat;
    Double currentUserLocationLong;

    Marker currentLocMarker;
    Marker destinationLocMarker;

    // Declare a boolean flag to control execution
    private boolean isRunning = true;
    private boolean isConnected = true;
    Handler handler;
    Runnable mapRefreshRunnable;

    @Override
    public void onStopButtonClicked() {
        // This method will be called when the Stop button is pressed in MapsInstruction
        isRunning = false;
        // Add any additional logic you need to perform when the Stop button is pressed.

        Toast.makeText(getContext(), "Exiting...", Toast.LENGTH_LONG).show();
        Log.d(TAG, "EXITING via STOP BUTTON...");

        loadFragment(new MapsFragment());

        BottomNavigationView bottomNavigationView = getView().findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setVisibility(View.GONE);

        LinearLayout linearLayoutDirections = getView().findViewById(R.id.directionsCont);
        linearLayoutDirections.setVisibility(View.GONE);

        FragmentContainerView fragmentContainerView = getView().findViewById(R.id.mapsRoute);
        fragmentContainerView.setVisibility(View.GONE);

        LinearLayout linearLayoutOverview = getView().findViewById(R.id.overviewCont);
        linearLayoutOverview.setVisibility(View.GONE);

        handler.removeCallbacks(mapRefreshRunnable); // Remove any pending callbacks // Dismiss the dialog
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_maps_route, container, false);
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
                    isRunning = false;
                    Toast.makeText(getContext(), "Exiting...", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "EXITING via HOME...");

                    loadFragment(new HomeFragment());

                    bottomNavigationView.setVisibility(View.GONE);

                    LinearLayout linearLayoutDirections = getView().findViewById(R.id.directionsCont);
                    linearLayoutDirections.setVisibility(View.GONE);

                    FragmentContainerView fragmentContainerView = getView().findViewById(R.id.mapsRoute);
                    fragmentContainerView.setVisibility(View.GONE);

                    LinearLayout linearLayoutOverview = getView().findViewById(R.id.overviewCont);
                    linearLayoutOverview.setVisibility(View.GONE);

                    handler.removeCallbacks(mapRefreshRunnable); // Remove any pending callbacks // Dismiss the dialog
                }

                else if (itemId == R.id.action_search) {
                    isRunning = false;
                    Toast.makeText(getContext(), "Exiting...", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "EXITING via SEARCH...");

                    loadFragment(new SearchFragment());

                    bottomNavigationView.setVisibility(View.GONE);

                    LinearLayout linearLayoutDirections = getView().findViewById(R.id.directionsCont);
                    linearLayoutDirections.setVisibility(View.GONE);

                    FragmentContainerView fragmentContainerView = getView().findViewById(R.id.mapsRoute);
                    fragmentContainerView.setVisibility(View.GONE);

                    LinearLayout linearLayoutOverview = getView().findViewById(R.id.overviewCont);
                    linearLayoutOverview.setVisibility(View.GONE);

                    handler.removeCallbacks(mapRefreshRunnable); // Remove any pending callbacks // Dismiss the dialog
                }

                else if (itemId == R.id.action_maps) {

                    /*
                    isRunning = false;
                    Toast.makeText(getContext(), "Exiting...", Toast.LENGTH_LONG).show();

                    // Use a Handler to refresh the map every second
                    Handler handlerDelay = new Handler();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            handler.removeCallbacks(mapRefreshRunnable); // Remove any pending callbacks // Dismiss the dialog
                        }
                    };
                    handlerDelay.postDelayed(runnable, 1000); // Schedule it to run after 1 second

                    bottomNavigationView.setVisibility(View.GONE);
                    loadFragment(new MapsFragment());
                     */
                }

                else if (itemId == R.id.action_quiz) {
                    isRunning = false;
                    Toast.makeText(getContext(), "Exiting...", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "EXITING via QUIZ...");

                    loadFragment(new StartQuizFragment());

                    // Use a Handler to refresh the map every second
                    Handler handlerDelay = new Handler();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            handler.removeCallbacks(mapRefreshRunnable); // Remove any pending callbacks // Dismiss the dialog
                        }
                    };
                    handlerDelay.postDelayed(runnable, 1000); // Schedule it to run after 1 second

                    bottomNavigationView.setVisibility(View.GONE);

                    LinearLayout linearLayoutDirections = getView().findViewById(R.id.directionsCont);
                    linearLayoutDirections.setVisibility(View.GONE);

                    FragmentContainerView fragmentContainerView = getView().findViewById(R.id.mapsRoute);
                    fragmentContainerView.setVisibility(View.GONE);

                    LinearLayout linearLayoutOverview = getView().findViewById(R.id.overviewCont);
                    linearLayoutOverview.setVisibility(View.GONE);
                }

                else if (itemId == R.id.action_profile) {
                    isRunning = false;
                    Toast.makeText(getContext(), "Exiting...", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "EXITING via PROFILE...");

                    loadFragment(new ProfileFragment());

                    // Use a Handler to refresh the map every second
                    Handler handlerDelay = new Handler();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            handler.removeCallbacks(mapRefreshRunnable); // Remove any pending callbacks // Dismiss the dialog
                        }
                    };
                    handlerDelay.postDelayed(runnable, 1000); // Schedule it to run after 1 second

                    bottomNavigationView.setVisibility(View.GONE);

                    LinearLayout linearLayoutDirections = getView().findViewById(R.id.directionsCont);
                    linearLayoutDirections.setVisibility(View.GONE);

                    FragmentContainerView fragmentContainerView = getView().findViewById(R.id.mapsRoute);
                    fragmentContainerView.setVisibility(View.GONE);

                    LinearLayout linearLayoutOverview = getView().findViewById(R.id.overviewCont);
                    linearLayoutOverview.setVisibility(View.GONE);
                }
                return true;
            }
        });

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
            String destinationLatitude = getBundle.getString("destinationLatitude");
            String destinationLongitude = getBundle.getString("destinationLongitude");
            Double passedCurrentUserLocationLat = getBundle.getDouble("userCurrentLatitude");
            Double passedCurrentUserLocationLong = getBundle.getDouble("userCurrentLongitude");

            bundle.putString("userId", userID);
            fragment.setArguments(bundle);

            // STORE ORIGIN-DESTINATION DATA FOR COME BACK WHEN APP IS CLOSED OR NAVIGATED TO OTHER OPTIONS
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.clear();
            editor.apply(); // Apply the changes

            // editor.putString("user_destLat_destLong", userID + "&" + destinationLatitude + "&" + destinationLongitude);
            editor.putString("user", userID);
            editor.putString("destinationLatitude", destinationLatitude);
            editor.putString("destinationLongitude", destinationLongitude);
            editor.putString("originLatitude", String.valueOf(passedCurrentUserLocationLat));
            editor.putString("originLongitude", String.valueOf(passedCurrentUserLocationLong));
            editor.apply();
        }

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(networkChangeReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(networkChangeReceiver);
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

        View rootView = getView(); // Get the root view
        if (rootView != null) {
            ImageView viewCurrent = getView().findViewById(R.id.imageViewOverviewButton);
            viewCurrent.setImageResource(R.drawable.currentloc);

            ImageView viewInstructions = getView().findViewById(R.id.btnShowInstructions);
            viewInstructions.setImageResource(R.drawable.expand_route);
            viewInstructions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

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

                        double latitude = 0;
                        double longitude = 0;

                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }

                        // Create a new PlaceDetailDialogFragment and pass the place details as arguments
                        MapsInstructions fragment = new MapsInstructions();
                        //fragment.setCancelable(true);

                        // Retrieve selected categories from Bundle arguments
                        Bundle getBundle = getArguments();

                        // Use Bundle to pass values
                        Bundle bundle = new Bundle();

                        if (getBundle != null) {
                            String placeName = getBundle.getString("placeName");

                            Double passedCurrentUserLocationLat = getBundle.getDouble("userCurrentLatitude");
                            Double passedCurrentUserLocationLong = getBundle.getDouble("userCurrentLongitude");

                            String destinationLatitude = getBundle.getString("destinationLatitude");
                            String destinationLongitude = getBundle.getString("destinationLongitude");

                            String userID = getBundle.getString("userId");
                            bundle.putString("userId", userID);

                            bundle.putString("placeName", placeName);
                            bundle.putDouble("userCurrentLatitude", latitude);
                            bundle.putDouble("userCurrentLongitude", longitude);
                            bundle.putString("destinationLatitude", destinationLatitude);
                            bundle.putString("destinationLongitude", destinationLongitude);

                            fragment.setArguments(bundle);
                        }

                        fragment.setOnStopButtonClickListener(MapsFragmentRoute.this);
                        // Show the PlaceDetailDialogFragment as a dialog
                        fragment.show(getChildFragmentManager(), "MapsInstructions");
                    });
                }
            });

            Button btnFinishRoute = getView().findViewById(R.id.btnDone);
            btnFinishRoute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    isRunning = false;

                    // Use a Handler to refresh the map every second
                    Handler handlerDelay = new Handler();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            handler.removeCallbacks(mapRefreshRunnable); // Remove any pending callbacks // Dismiss the dialog
                        }
                    };
                    handlerDelay.postDelayed(runnable, 1000); // Schedule it to run after 1 second

                    //loadFragment(new MapsFragmentArrived());

                    // In the fragment or activity where you want to navigate
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                    MapsFragmentArrived mapsFragment = new MapsFragmentArrived();

                    // Retrieve selected categories from Bundle arguments
                    Bundle getBundle = getArguments();

                    // Use Bundle to pass values
                    Bundle bundle = new Bundle();

                    if (getBundle != null) {
                        String placeName = getBundle.getString("placeName");
                        Double passedCurrentUserLocationLat = getBundle.getDouble("userCurrentLatitude");
                        Double passedCurrentUserLocationLong = getBundle.getDouble("userCurrentLongitude");
                        String destinationLatitude = getBundle.getString("destinationLatitude");
                        String destinationLongitude = getBundle.getString("destinationLongitude");

                        String userID = getBundle.getString("userId");
                        bundle.putString("userId", userID);

                        bundle.putString("placeName", placeName);
                        bundle.putDouble("userCurrentLatitude", passedCurrentUserLocationLat);
                        bundle.putDouble("userCurrentLongitude", passedCurrentUserLocationLong);
                        bundle.putString("destinationLatitude", destinationLatitude);
                        bundle.putString("destinationLongitude", destinationLongitude);
                        mapsFragment.setArguments(bundle);
                    }

                    LinearLayout linearLayoutDirections = getView().findViewById(R.id.directionsCont);
                    linearLayoutDirections.setVisibility(View.GONE);

                    FragmentContainerView fragmentContainerView = getView().findViewById(R.id.mapsRoute);
                    fragmentContainerView.setVisibility(View.GONE);

                    LinearLayout linearLayoutOverview = getView().findViewById(R.id.overviewCont);
                    linearLayoutOverview.setVisibility(View.GONE);

                    BottomNavigationView bottomNavigationView = getView().findViewById(R.id.bottomNavigationView);
                    bottomNavigationView.setVisibility(View.GONE);

                    // Replace the current fragment with the receiving fragment
                    transaction.replace(R.id.fragment_container, mapsFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });

            routing();

            /*updateMap();


            // Check network connectivity
            ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                Log.d(TAG, "-----outside CONNECTED-----");

                // Use a Handler to refresh the map every second
                final int INTERVAL = 1000; // 1000 milliseconds = 1 second
                handler = new Handler();
                mapRefreshRunnable = new Runnable() {
                    @Override
                    public void run() {
                        //updateMap(); // Call the method to update the map
                        //reUpdateMap();

                        if (!isRunning) {
                            // Stop the execution if the flag is false
                            return;
                        } else {
                            if (currentRoutePolyline != null && currentBorderPolyline != null) {
                                //updateMap();
                                reUpdateMapAgain();
                                //handler.postDelayed(this, 1000);
                            } else if (previousCurrentRoutePolyline != null && previousCurrentBorderPolyline != null) {
                                //manualMap();
                                reUpdateMap();
                                //handler.postDelayed(this, 1000);
                            }

                            handler.postDelayed(this, 1000);
                        }
                    }
                };

                handler.postDelayed(mapRefreshRunnable, 1000); // Schedule it to run again in 1 second
            } else {
                //isRunning = false;
                Toast.makeText(getContext(), "Reconnecting...", Toast.LENGTH_LONG).show();
                Log.d(TAG, "----DISCONNECTED----");
            }*/
        }

    }

    public void routing() {
        updateMap();

        // Check network connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.d(TAG, "-----outside CONNECTED-----");

            // Use a Handler to refresh the map every second
            final int INTERVAL = 1000; // 1000 milliseconds = 1 second
            handler = new Handler();
            mapRefreshRunnable = new Runnable() {
                @Override
                public void run() {
                    //updateMap(); // Call the method to update the map
                    //reUpdateMap();

                    if (!isRunning) {
                        // Stop the execution if the flag is false
                        return;
                    } else {
                        if (currentRoutePolyline != null && currentBorderPolyline != null) {
                            //updateMap();
                            reUpdateMapAgain();
                            //handler.postDelayed(this, 1000);
                        } else if (previousCurrentRoutePolyline != null && previousCurrentBorderPolyline != null) {
                            //manualMap();
                            reUpdateMap();
                            //handler.postDelayed(this, 1000);
                        }

                        handler.postDelayed(this, 1000);
                    }
                }
            };

            handler.postDelayed(mapRefreshRunnable, 1000); // Schedule it to run again in 1 second
        } else {
            //isRunning = false;
            Toast.makeText(getContext(), "Reconnecting...", Toast.LENGTH_LONG).show();
            Log.d(TAG, "----DISCONNECTED----");
        }
    }

    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if (!isConnected) {
                // Network is disconnected
                displayDisconnectMessage();
            } else {
                // Network is connected
                //handleReconnection();

                handler1.removeCallbacks(runnableCode);

                Log.d(TAG, "RECONNECTED---------------------------------------------------------------------------------------------------");

                /*
                if (isDisconnected > 0) {

                    Toast.makeText(getContext(), "Reconnected.", Toast.LENGTH_LONG).show();

                    isDisconnected = 0;


                    if (currentLocMarker != null) {
                        currentLocMarker.remove();
                        currentLocMarker = null;
                    }

                    if (currentRoutePolyline != null) {
                        currentRoutePolyline.remove();
                        currentRoutePolyline = null;
                    }

                    if (currentBorderPolyline != null) {
                        currentBorderPolyline.remove();
                        currentBorderPolyline = null;
                    }

                    if (previousCurrentRoutePolyline != null) {
                        previousCurrentRoutePolyline.remove();
                        previousCurrentRoutePolyline = null;
                    }
                    if (previousCurrentBorderPolyline != null) {
                        previousCurrentBorderPolyline.remove();
                        previousCurrentBorderPolyline = null;
                    }

                    routing();

                }
                */

                if (isDisconnected > 0) {

                    isDisconnected = 0;

                    Toast.makeText(getContext(), "Reconnected.", Toast.LENGTH_LONG).show();

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

                    // In the fragment or activity where you want to navigate
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                    MapsFragmentRoute mapsFragmentRoute = new MapsFragmentRoute();

                    // Retrieve selected categories from Bundle arguments
                    Bundle getBundle = getArguments();

                    // Use Bundle to pass values
                    Bundle bundle = new Bundle();

                    // Get the user's last known location and move the camera there
                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            if (getBundle != null) {
                                String destinationLatitude = getBundle.getString("destinationLatitude");
                                String destinationLongitude = getBundle.getString("destinationLongitude");

                                String userID = getBundle.getString("userId");
                                bundle.putString("userId", userID);

                                bundle.putDouble("userCurrentLatitude", latitude);
                                bundle.putDouble("userCurrentLongitude", longitude);
                                bundle.putString("destinationLatitude", destinationLatitude);
                                bundle.putString("destinationLongitude", destinationLongitude);
                                mapsFragmentRoute.setArguments(bundle);
                            }
                        }
                    });

                    LinearLayout linearLayoutDirections = getView().findViewById(R.id.directionsCont);
                    linearLayoutDirections.setVisibility(View.GONE);

                    FragmentContainerView fragmentContainerView = getView().findViewById(R.id.mapsRoute);
                    fragmentContainerView.setVisibility(View.GONE);

                    LinearLayout linearLayoutOverview = getView().findViewById(R.id.overviewCont);
                    linearLayoutOverview.setVisibility(View.GONE);

                    BottomNavigationView bottomNavigationView = getView().findViewById(R.id.bottomNavigationView);
                    bottomNavigationView.setVisibility(View.GONE);

                    // Replace the current fragment with the receiving fragment
                    transaction.replace(R.id.fragment_container, mapsFragmentRoute);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }

            }
        }
    };

    private final Handler handler1 = new Handler();
    private final int delay = 3500; // 3.5 seconds in milliseconds

    private final Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            // Your code to display the Toast message goes here
            Toast.makeText(getContext(), "Reconnecting...", Toast.LENGTH_LONG).show();
            handler1.postDelayed(this, delay); // Repeat the code after the specified delay
        }
    };

    private void displayDisconnectMessage() {
        isDisconnected++;
        handler1.postDelayed(runnableCode, delay);
    }

    private void handleReconnection() {
        // Implement the logic to handle reconnection
        // For example, refresh the map or retry network requests
        //Toast.makeText(getContext(), "Reconnected, updating map...", Toast.LENGTH_SHORT).show();

        updateMap(); // Assuming updateMap() is a method to refresh map data

        // Use a Handler to refresh the map every second
        final int INTERVAL = 1000; // 1000 milliseconds = 1 second
        handler = new Handler();
        mapRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                //updateMap(); // Call the method to update the map
                //reUpdateMap();

                if (!isRunning) {
                    // Stop the execution if the flag is false
                    return;
                }

                else {
                    if (currentRoutePolyline != null && currentBorderPolyline != null) {
                        //updateMap();
                        reUpdateMapAgain();
                        handler.postDelayed(this, 1000);
                    } else if (previousCurrentRoutePolyline != null && previousCurrentBorderPolyline != null) {
                        //manualMap();
                        reUpdateMap();
                        handler.postDelayed(this, 1000);
                    }
                }
            }
        };

        handler.postDelayed(mapRefreshRunnable, 1000); // Schedule it to run again in 1 second
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }



    // Create a method to update the map
    private void updateMap() {
        // Add your code here to update the map
        // This method will be called every time you want to refresh the

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(networkChangeReceiver, filter);


        // Check network connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.d(TAG, "-----updateMap CONNECTED-----");

            if (!isRunning) {
                // Stop execution of the method if isRunning is false
                return;
            }

            if (currentLocMarker != null) {
                currentLocMarker.remove();
            }

            View rootView = getView(); // Get the root view
            if (rootView != null) {
                LinearLayout linearLayoutDirections = rootView.findViewById(R.id.directionsCont);
                if (linearLayoutDirections != null) {
                    linearLayoutDirections.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
                }


                LinearLayout linearLayoutOverview = rootView.findViewById(R.id.overviewCont);
                if (linearLayoutOverview != null) {
                    linearLayoutOverview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
                }

                BitmapDescriptor customArrow = BitmapDescriptorFactory.fromResource(R.drawable.currentlocation);
                BitmapDescriptor destinationMarker = BitmapDescriptorFactory.fromResource(R.drawable.destinationflag);

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

                        ImageView imageViewReCenter = rootView.findViewById(R.id.imageViewOverviewButton);
                        if (imageViewReCenter != null) {
                            imageViewReCenter.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 19));
                                }

                            });
                        }

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
                            .title(placeName)
                            .icon(destinationMarker); // Use the scaled custom arrow as the marker icon
                    //.anchor(0.5f, 1f); // Adjust the anchor to the center of your custom arrow

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

                    // previousCurrentRoutePolyline previousCurrentBorderPolyline currentRoutePolyline currentBorderPolyline

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
                                            previousCurrentBorderPolyline = mMap.addPolyline(borderOptions);

                                            // Draw the solid route line on the map with color "#00b0ff"
                                            PolylineOptions routeOptions = new PolylineOptions()
                                                    .addAll(points)
                                                    .width(14) // Adjust the width as needed for the route
                                                    .color(Color.parseColor("#00b0ff")); // Set color to "#00b0ff" for the route
                                            previousCurrentRoutePolyline = mMap.addPolyline(routeOptions);

                                            // Move the camera to fit the bounds of the new route
                                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                            builder.include(originLatLng);
                                            builder.include(destinationLatLng);
                                            LatLngBounds bounds = builder.build();
                                            //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));

                                            // Move the camera to the user's location
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 19));   //19


                                            Button overviewButton = rootView.findViewById(R.id.overviewButton);
                                            if (overviewButton != null) {
                                                overviewButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 250));
                                                    }
                                                });
                                            }

                                        }
                                    },
                                    error -> {
                                        // Handle errors in making the request or parsing the response
                                        Log.e("Directions Error", error.toString());
                                    }
                            );

                            // ------------------------------------------------------------------------------------------------------------------------------------------------------------------


                            // Use this method before starting any async task
                            if(isNetworkAvailable()) {
                                // Start Async Task
                                // Create an instance of DirectionsTask and execute it
                                DirectionsTask directionsTask = new DirectionsTask(url);
                                directionsTask.execute();
                            } else {
                                // Show error message
                                Toast.makeText(getContext(), "Reconnecting...", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "updateMap----DISCONNECTED----");
                            }



                            //String jsonResponseString = String.valueOf(directionsTask);
                            //Toast.makeText(getContext(), jsonResponseString, Toast.LENGTH_LONG).show();


                            // ------------------------------------------------------------------------------------------------------------------------------------------------------------------


                            // Add the request to the queue
                            queue.add(request);
                        }
                    });
                }


                // Use a Handler to refresh the map every second
                Handler handler = new Handler();
                Runnable mapRefreshRunnable = new Runnable() {
                    @Override
                    public void run() {
                        // Clear the previous route polyline and border polyline if they exist
                        if (currentRoutePolyline != null) {
                            currentRoutePolyline.remove();
                            currentRoutePolyline = null;
                        }
                        if (currentBorderPolyline != null) {
                            currentBorderPolyline.remove();
                            currentBorderPolyline = null;
                        }
                    }
                };
                handler.postDelayed(mapRefreshRunnable, 800); // Schedule it to run after 0.3 second
            }
        }

        else {
            //isRunning = false;
            Toast.makeText(getContext(), "Reconnecting...", Toast.LENGTH_LONG).show();
            Log.d(TAG, "----DISCONNECTED----");
        }
    }

    private void reUpdateMap() {
        // Add your code here to update the map
        // This method will be called every time you want to refresh the map

        if (!isRunning) {
            // Stop execution of the method if isRunning is false
            return;
        }

        // Check network connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.d(TAG, "-----reUpdateMap CONNECTED-----");
        } else {
            Log.d(TAG, "----DISCONNECTED----");
        }

        if (currentLocMarker != null) {
            currentLocMarker.remove();
        }

        BitmapDescriptor customArrow = BitmapDescriptorFactory.fromResource(R.drawable.currentlocation);
        BitmapDescriptor destinationMarker = BitmapDescriptorFactory.fromResource(R.drawable.destinationflag);

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
                    .title(placeName)
                    .icon(destinationMarker); // Use the scaled custom arrow as the marker icon
                    //.anchor(0.5f, 1f); // Adjust the anchor to the center of your custom arrow

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


                    // Use this method before starting any async task
                    if(isNetworkAvailable()) {
                        // Start Async Task
                        // Create an instance of DirectionsTask and execute it
                        DirectionsTask directionsTask = new DirectionsTask(url);
                        directionsTask.execute();
                    } else {
                        // Show error message
                        Toast.makeText(getContext(), "Reconnecting...", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "reUpdateMap----DISCONNECTED----");
                    }

                    //String jsonResponseString = String.valueOf(directionsTask);
                    //Toast.makeText(getContext(), jsonResponseString, Toast.LENGTH_LONG).show();


                    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------


                    // Add the request to the queue
                    queue.add(request);
                }
            });
        }

        // Use a Handler to refresh the map every second
        Handler handler = new Handler();
        Runnable mapRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                // Clear the previous route polyline and border polyline if they exist
                if (previousCurrentRoutePolyline != null) {
                    previousCurrentRoutePolyline.remove();
                    previousCurrentRoutePolyline = null;
                }
                if (previousCurrentBorderPolyline != null) {
                    previousCurrentBorderPolyline.remove();
                    previousCurrentBorderPolyline = null;
                }
            }
        };
        handler.postDelayed(mapRefreshRunnable, 800); // Schedule it to run after 0.5 second

    }

    private void reUpdateMapAgain() {
        // Add your code here to update the map
        // This method will be called every time you want to refresh the map

        if (!isRunning) {
            // Stop execution of the method if isRunning is false
            return;
        }

        // Check network connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.d(TAG, "-----reUpdateMapAgain CONNECTED-----");
        } else {
            Log.d(TAG, "----DISCONNECTED----");
        }

        if (currentLocMarker != null) {
            currentLocMarker.remove();
        }

        View rootView = getView(); // Get the root view
        if (rootView != null) {

            LinearLayout linearLayoutDirections = rootView.findViewById(R.id.directionsCont);
            if (linearLayoutDirections != null) {
                linearLayoutDirections.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }

            LinearLayout linearLayoutOverview = rootView.findViewById(R.id.overviewCont);
            if (linearLayoutOverview != null) {
                linearLayoutOverview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }

            BitmapDescriptor customArrow = BitmapDescriptorFactory.fromResource(R.drawable.currentlocation);
            BitmapDescriptor destinationMarker = BitmapDescriptorFactory.fromResource(R.drawable.destinationflag);

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

                    ImageView imageViewReCenter = rootView.findViewById(R.id.imageViewOverviewButton);
                    if (imageViewReCenter != null) {
                        imageViewReCenter.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 19));
                            }
                        });
                    }

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
                        .title(placeName)
                        .icon(destinationMarker); // Use the scaled custom arrow as the marker icon
                //.anchor(0.5f, 1f); // Adjust the anchor to the center of your custom arrow

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

                // previousCurrentRoutePolyline previousCurrentBorderPolyline currentRoutePolyline currentBorderPolyline

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
                                        previousCurrentBorderPolyline = mMap.addPolyline(borderOptions);

                                        // Draw the solid route line on the map with color "#00b0ff"
                                        PolylineOptions routeOptions = new PolylineOptions()
                                                .addAll(points)
                                                .width(14) // Adjust the width as needed for the route
                                                .color(Color.parseColor("#00b0ff")); // Set color to "#00b0ff" for the route
                                        previousCurrentRoutePolyline = mMap.addPolyline(routeOptions);

                                        // Move the camera to fit the bounds of the new route
                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                        builder.include(originLatLng);
                                        builder.include(destinationLatLng);
                                        LatLngBounds bounds = builder.build();
                                        //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));

                                        // Move the camera to the user's location
                                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 19));   //19


                                        Button overviewButton = rootView.findViewById(R.id.overviewButton);
                                        if (overviewButton != null) {
                                            overviewButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 250));
                                                }
                                            });
                                        }

                                    }
                                },
                                error -> {
                                    // Handle errors in making the request or parsing the response
                                    Log.e("Directions Error", error.toString());
                                }
                        );

                        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------


                        // Use this method before starting any async task
                        if(isNetworkAvailable()) {
                            // Start Async Task
                            // Create an instance of DirectionsTask and execute it
                            DirectionsTask directionsTask = new DirectionsTask(url);
                            directionsTask.execute();
                        } else {
                            // Show error message
                            Toast.makeText(getContext(), "Reconnecting...", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "reupdateMapAGain()----DISCONNECTED----");
                        }

                        //String jsonResponseString = String.valueOf(directionsTask);
                        //Toast.makeText(getContext(), jsonResponseString, Toast.LENGTH_LONG).show();


                        // ------------------------------------------------------------------------------------------------------------------------------------------------------------------


                        // Add the request to the queue
                        queue.add(request);
                    }
                });
            }


            // Use a Handler to refresh the map every second
            Handler handler = new Handler();
            Runnable mapRefreshRunnable = new Runnable() {
                @Override
                public void run() {
                    // Clear the previous route polyline and border polyline if they exist
                    if (currentRoutePolyline != null) {
                        currentRoutePolyline.remove();
                        currentRoutePolyline = null;
                    }
                    if (currentBorderPolyline != null) {
                        currentBorderPolyline.remove();
                        currentBorderPolyline = null;
                    }
                }
            };
            handler.postDelayed(mapRefreshRunnable, 800); // Schedule it to run after 0.3 second

        }

    }



    public class DirectionsTask extends AsyncTask<Void, Void, String> {

        String passedUrl;
        public DirectionsTask(String url) {
            passedUrl = url;
        }

        @Override
        protected String doInBackground(Void... voids) {

            if (!isRunning) {
                // Stop the execution if isRunning is false
                return null;
            }

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
                return null;
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

            if (!isRunning) {
                // Stop further processing if isRunning is false
                return;
            }

            // Use the jsonResponseString in your application as needed

            try {

                if (jsonResponseString != null) {

                    JSONObject jsonResponse = new JSONObject(jsonResponseString); // jsonResponseString is the JSON response you received

                    // Check the status of the response
                    String status = jsonResponse.getString("status");

                    if (!isRunning || !status.equals("OK")) {
                        // Stop processing if isRunning is false or status is not "OK"
                        return;
                    } else if (status.equals("OK")) {
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

                        View rootview = getView();
                        if (rootview != null) {

                            // Populate UI elements with place details
                            TextView textViewDistance = getView().findViewById(R.id.textViewDistance);
                            Button buttonFinish = getView().findViewById(R.id.btnDone);
                            //textViewDistance.setText(distanceSample);
                            //Toast.makeText(getContext(), distanceSample, Toast.LENGTH_LONG).show();

                            TextView textViewDirection = getView().findViewById(R.id.textViewDirection);
                            if (textViewDirection != null) {
                                textViewDirection.setText(plainTextInstructions);
                            }

                            // Extract the numerical value from the distance string
                            double distanceValue = Double.parseDouble(distanceSample.replaceAll("[^0-9.]+", ""));

                            if (distanceSample.contains("km") && distanceValue < 1.0) {
                                // Convert the distance to meters
                                int meters = (int) (distanceValue * 1000);
                                String distanceInMeters = meters + " m";
                                // Use distanceInMeters as needed
                                if (textViewDistance != null) {
                                    textViewDistance.setText(distanceInMeters);
                                }
                            } else {
                                // Use the original distance string (it's already in meters or more than 1 km)
                                if (textViewDistance != null) {
                                    textViewDistance.setText(distanceSample);
                                }
                            }


                            // Initialize ImageView container
                            ImageView imageViewDirections = getView().findViewById(R.id.imageViewDirections);

                            // Get the maneuver from your API response
                            // Retrieve maneuver if it's present, or provide a default value
                            String maneuverType = stepSamp.optString("maneuver", "No Maneuver");
                            //Toast.makeText(getContext(), maneuverType, Toast.LENGTH_LONG).show();

                            // Create a variable to store the drawable resource ID
                            int drawableResource = R.drawable.straight; // Default drawable

                            // Map maneuver types to corresponding drawable resource IDs
                            switch (maneuverType) {
                                case "keep-left":
                                    drawableResource = R.drawable.keep_left;
                                    break;
                                case "keep-right":
                                    drawableResource = R.drawable.keep_right;
                                    break;
                                case "ferry":
                                    drawableResource = R.drawable.ferry;
                                    break;
                                case "ferry-train":
                                    drawableResource = R.drawable.ferry_train;
                                    break;
                                case "fork-left":
                                    drawableResource = R.drawable.fork_left;
                                    break;
                                case "fork-right":
                                    drawableResource = R.drawable.fork_right;
                                    break;
                                case "merge":
                                    drawableResource = R.drawable.merge;
                                    break;
                                case "ramp-left":
                                    drawableResource = R.drawable.ramp_left;
                                    break;
                                case "ramp-right":
                                    drawableResource = R.drawable.ramp_right;
                                    break;
                                case "roundabout-left":
                                    drawableResource = R.drawable.roundabout_left;
                                    break;
                                case "roundabout-right":
                                    drawableResource = R.drawable.roundabout_right;
                                    break;
                                case "straight":
                                    drawableResource = R.drawable.straight;
                                    break;
                                case "turn-right":
                                    drawableResource = R.drawable.turn_right;
                                    break;
                                case "turn-left":
                                    drawableResource = R.drawable.turn_left;
                                    break;
                                case "turn-sharp-right":
                                    drawableResource = R.drawable.turn_sharp_right;
                                    break;
                                case "turn-sharp-left":
                                    drawableResource = R.drawable.turn_sharp_left;
                                    break;
                                case "turn-slight-right":
                                    drawableResource = R.drawable.turn_slight_right;
                                    break;
                                case "turn-slight-left":
                                    drawableResource = R.drawable.turn_slight_left;
                                    break;
                                case "uturn-right":
                                    drawableResource = R.drawable.uturn_right;
                                    break;
                                case "uturn-left":
                                    drawableResource = R.drawable.uturn_left;
                                    break;

                                default:
                                    // Handle unknown maneuver types or use a default drawable
                                    drawableResource = R.drawable.straight;
                                    break;
                            }


                            if (imageViewDirections != null) {
                                // Set the selected drawable to the ImageView
                                imageViewDirections.setImageResource(drawableResource);
                            }

                            double totalDistanceKm = 0.0;
                            int totalDurationMinutes = 0;

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

                                        // Extract and add up the distance and duration for each leg
                                        totalDistanceKm += step.getJSONObject("distance").getDouble("value") / 1000.0; // Convert meters to kilometers
                                        totalDurationMinutes += step.getJSONObject("duration").getInt("value") / 60; // Convert seconds to minutes

                                        //String[] splitDistance = distance.split(" ");
                                        //String[] splitTime = duration.split(" ");

                                        //Toast.makeText(getContext(), splitTime[0], Toast.LENGTH_LONG).show();

                                        //double doubleDistance = Double.parseDouble(splitDistance[0]);
                                        //int intMinutes = Integer.parseInt(splitTime[0]);

                                        //totalDistance += doubleDistance;
                                        //totalTime += intMinutes;

                                        //Toast.makeText(getContext(), distance, Toast.LENGTH_LONG).show();

                                        // You can now use the extracted information as needed

                                        // Retrieve maneuver if it's present, or provide a default value
                                        //String maneuver = step.optString("maneuver", "No Maneuver");
                                        //Toast.makeText(getContext(), maneuver, Toast.LENGTH_LONG).show();
                                    }
                                }
                            }

                            // Format totalDistanceKm with 2 decimal places
                            String formattedDistance = String.format("%.2f", totalDistanceKm);

                            double thresholdDistance = 1.2; // 0.015=15 meters threshold    1.2

                            if (textViewDistance != null && textViewDirection != null && buttonFinish != null && imageViewDirections != null) {
                                if (totalDistanceKm <= thresholdDistance) {
                                    textViewDistance.setText("You have arrived at your destination.");
                                    textViewDirection.setVisibility(View.GONE);
                                    buttonFinish.setVisibility(View.VISIBLE);
                                    buttonFinish.setText("Done");
                                    imageViewDirections.setImageResource(R.drawable.arrived);
                                } else {
                                    textViewDirection.setVisibility(View.VISIBLE);
                                    buttonFinish.setVisibility(View.GONE);
                                    imageViewDirections.setImageResource(drawableResource);
                                }
                            }

                            TextView textViewTotal = getView().findViewById(R.id.textViewTotalMinKm);
                            if (textViewTotal != null) {
                                textViewTotal.setText(formattedDistance + " km • " + totalDurationMinutes + " mins");
                            }
                        }

                    } else {
                        // Handle the case when the API request returns a status other than "OK"
                    }
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

        if (currentLocMarker != null) {
            currentLocMarker.remove();
        }

        LinearLayout linearLayoutDirections = getView().findViewById(R.id.directionsCont);
        linearLayoutDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        LinearLayout linearLayoutOverview = getView().findViewById(R.id.overviewCont);
        linearLayoutOverview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        BitmapDescriptor customArrow = BitmapDescriptorFactory.fromResource(R.drawable.currentlocation);
        BitmapDescriptor destinationMarker = BitmapDescriptorFactory.fromResource(R.drawable.destinationflag);

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

            /*
            // Clear the previous route polyline and border polyline if they exist
            if (previousCurrentRoutePolyline != null) {
                previousCurrentRoutePolyline.remove();
            }
            if (previousCurrentBorderPolyline != null) {
                previousCurrentBorderPolyline.remove();
            }
             */


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


        // Use a Handler to refresh the map every second
        Handler handler = new Handler();
        Runnable mapRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                // Clear the previous route polyline and border polyline if they exist
                if (previousCurrentRoutePolyline != null) {
                    previousCurrentRoutePolyline.remove();
                    previousCurrentRoutePolyline = null;
                }
                if (previousCurrentBorderPolyline != null) {
                    previousCurrentBorderPolyline.remove();
                    previousCurrentBorderPolyline = null;
                }
            }
        };
        handler.postDelayed(mapRefreshRunnable, 1000); // Schedule it to run after 0.5 seconds


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
