package com.example.qsee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

// Import
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.gms.maps.OnMapReadyCallback;

// Quezon City ONLY
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;


public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap mMap;

    public MapsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_maps, container, false);

        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        // Obtain the FragmentManager using getParentFragmentManager()
        FragmentManager fragmentManager = getParentFragmentManager();

        // Use fragmentManager to work with fragments, including SupportMapFragment
        SupportMapFragment mapFragment = new SupportMapFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.maps, mapFragment).commit();

        // SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager.findFragmentId(R.id.maps);
        mapFragment.getMapAsync(this);

        return view;
    }

//    @Override
//    public void onMapReady(@NonNull GoogleMap googleMap) {
//
//    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Define the bounds for Quezon City, Philippines
        LatLngBounds quezonCityBounds = new LatLngBounds(
                new LatLng(14.6138, 121.0357), // Southwest corner
                new LatLng(14.7395, 121.0711)  // Northeast corner
        );

        // Constrain the map's camera position to Quezon City bounds
        mMap.setLatLngBoundsForCameraTarget(quezonCityBounds);

        // Move the camera to Quezon City
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(quezonCityBounds.getCenter(), 12));
    }

    /*
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
*/
}
