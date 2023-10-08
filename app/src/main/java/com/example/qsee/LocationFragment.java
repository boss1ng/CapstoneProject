package com.example.qsee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class LocationFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_list, container, false);

        // Find the "addLocationBtn" button
        FloatingActionButton addLocationBtn = view.findViewById(R.id.addLocationBtn);

        // Set a click listener for the button
        addLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the dialog fragment
                LocationDialogFragment dialogFragment = new LocationDialogFragment();
                FragmentManager fragmentManager = getParentFragmentManager();
                dialogFragment.show(fragmentManager, "LocationDialogFragment");
            }
        });

        return view;
    }
}

