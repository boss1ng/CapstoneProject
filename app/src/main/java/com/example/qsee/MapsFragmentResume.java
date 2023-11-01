package com.example.qsee;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MapsFragmentResume extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create a new Dialog instance
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // Set a custom layout for the dialog
        dialog.setContentView(R.layout.fragment_maps_resume);

        // Customize the width of the dialog (75% of screen width)
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 1);
        dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

        return dialog;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the custom layout for this dialog fragment
        View view = inflater.inflate(R.layout.fragment_maps_resume, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Retrieve place details from arguments
        Bundle getBundle = getArguments();

        if (getBundle != null) {
            String userID = getBundle.getString("userId");
            String placeName = getBundle.getString("placeName");
            //Toast.makeText(getContext(), userID, Toast.LENGTH_SHORT).show();
            //Toast.makeText(getContext(), placeName, Toast.LENGTH_SHORT).show();
        }

        /*
        // Set the custom background to the root view
        if (view != null) {
            view.setBackgroundResource(R.drawable.dialog_background);
        }
         */

        TextView textView = view.findViewById(R.id.textViewReport);
        textView.setText("Resume drive?");

        Button buttonResume = view.findViewById(R.id.btnYes);
        Button buttonStop = view.findViewById(R.id.btnNo);

        buttonResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                MapsFragmentRoute mapsFragmentRoute = new MapsFragmentRoute();

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
                    mapsFragmentRoute.setArguments(bundle);
                }

                LinearLayout linearLayoutFilter = getParentFragment().getView().findViewById(R.id.filterMenu);
                linearLayoutFilter.setVisibility(View.GONE);

                FragmentContainerView fragmentContainerView = getParentFragment().getView().findViewById(R.id.maps);
                fragmentContainerView.setVisibility(View.GONE);

                BottomNavigationView bottomNavigationView = getParentFragment().getView().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setVisibility(View.GONE);

                // Replace the current fragment with the receiving fragment
                transaction.replace(R.id.fragment_container, mapsFragmentRoute);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply(); // Apply the changes

                dismiss();

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                MapsFragment mapsFragment = new MapsFragment();

                // Retrieve selected categories from Bundle arguments
                Bundle getBundle = getArguments();

                // Use Bundle to pass values
                Bundle bundle = new Bundle();

                if (getBundle != null) {
                    String userID = getBundle.getString("userId");
                    bundle.putString("userId", userID);
                    mapsFragment.setArguments(bundle);
                }

                LinearLayout linearLayoutFilter = getParentFragment().getView().findViewById(R.id.filterMenu);
                linearLayoutFilter.setVisibility(View.GONE);

                FragmentContainerView fragmentContainerView = getParentFragment().getView().findViewById(R.id.maps);
                fragmentContainerView.setVisibility(View.GONE);

                BottomNavigationView bottomNavigationView = getParentFragment().getView().findViewById(R.id.bottomNavigationView);
                bottomNavigationView.setVisibility(View.GONE);

                // Replace the current fragment with the receiving fragment
                transaction.replace(R.id.fragment_container, mapsFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return view;
    }
}