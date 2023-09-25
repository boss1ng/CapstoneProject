package com.example.qsee;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        // Initialize the FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.maps);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

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
                LatLng userLocation = new LatLng(latitude, longitude);

                // Philippine Heart Center
                // LatLng userLocation = new LatLng(14.6440, 121.0481);

                // Add a marker at the user's location
                mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));

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

                for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                    // Extract place data (e.g., latitude, longitude, name) from placeSnapshot

                    String name = placeSnapshot.child("Location").getValue(String.class);
                    String latitude = placeSnapshot.child("Latitude").getValue(String.class);
                    String longitude = placeSnapshot.child("Longitude").getValue(String.class);

                    try {
                        Double doubleLatitude = Double.parseDouble(latitude);
                        Double doubleLongitude = Double.parseDouble(longitude);

                        // Create MarkerOptions or LatLng objects for each place
                        LatLng location = new LatLng(doubleLatitude, doubleLongitude);
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(location)
                                .title(name);

                        // Add markers to the Google Map
                        Marker marker = mMap.addMarker(markerOptions);

                    } catch (NumberFormatException e) {
                        // Handle the case where the String cannot be parsed as a Double
                        // This can happen if the String is not a valid numeric format
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // This method is called if there is an error reading from the database
                // Handle the error here
            }
        });

/*
// ROUTES API
        // Define the Directions API endpoint and your API key
        String apiUrl = "https://maps.googleapis.com/maps/api/directions/json";
        String apiKey = "AIzaSyAwTBhjMDtD74Nvqz7eUbN81v93SLhM3IU"; // YOUR_API_KEY_HERE

        // Specify the origin and destination coordinates
                String origin = "14.6440, 121.0481";
                String destination = "14.6515,121.0493";

        // Build the URL for the Directions API request
                String requestUrl = apiUrl + "?origin=" + origin + "&destination=" + destination + "&key=" + apiKey;

        // Make the API request using an HTTP client (e.g., HttpURLConnection, OkHttp)
        // Parse the JSON response and draw directions on the map
*/


/*
        // Define the bounds for Quezon City, Philippines
        LatLngBounds quezonCityBounds = new LatLngBounds(
                new LatLng(14.6138, 121.0357), // Southwest corner
                new LatLng(14.7395, 121.0711)  // Northeast corner
        );

            // Request location permission if not granted
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Move the camera to Quezon City
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(quezonCityBounds.getCenter(), 12));
        // mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(quezonCityBounds, 0));
*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, reinitialize the map
                if (mMap != null) {
                    onMapReady(mMap);
                }
            }
        }
    }


/*
    //When the connect request has successfully completed
    //@Override
    public void onConnected(Bundle bundle) {

    }

    //Called when the client is temporarily in a disconnected state.
    //@Override
    public void onConnectionSuspended(int i) {

    }

    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void onLocationChanged(Location location) {

    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
*/

}
