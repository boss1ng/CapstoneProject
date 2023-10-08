package com.example.qsee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class LocationDialogFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_location, container, false);

        // Find the "IndivBtn" and "BatchBtn" buttons
        Button indivBtn = view.findViewById(R.id.IndivBtn);
        Button batchBtn = view.findViewById(R.id.BatchBtn);

        // Set click listeners for the buttons
        indivBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create the fragment you want to replace in the fragment_container
                Fragment AddItineraryFragment = new AddItineraryFragment();

                // Get the parent FragmentActivity's FragmentManager
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

                // Begin the fragment transaction
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                // Replace the fragment_container with the new fragment
                transaction.replace(R.id.fragment_container, AddItineraryFragment);
                transaction.addToBackStack(null); // Optional: Add transaction to back stack
                transaction.commit();
            }
        });


        batchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create the fragment you want to replace in the fragment_container
                Fragment AddGroupItineraryFragment = new AddGroupItineraryFragment();

                // Get the parent FragmentActivity's FragmentManager
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

                // Begin the fragment transaction
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                // Replace the fragment_container with the new fragment
                transaction.replace(R.id.fragment_container, AddGroupItineraryFragment);
                transaction.addToBackStack(null); // Optional: Add transaction to back stack
                transaction.commit();
            }
        });

        return view;
    }
}


