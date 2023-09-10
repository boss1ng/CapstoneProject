package com.example.qsee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class GroupsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        // Find the button by its ID
        FloatingActionButton addGroupBtn = view.findViewById(R.id.addGroupBtn);

        // Set an OnClickListener for the button
        addGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new instance of the fragment you want to navigate to
                Fragment addGroupFragment = new AddGroupFragment();

                // Get the FragmentManager and start a transaction
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

                // Replace the current fragment with the new one
                transaction.replace(R.id.fragment_container, addGroupFragment);

                // Add the transaction to the back stack
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
            }
        });

        return view;
    }
}
