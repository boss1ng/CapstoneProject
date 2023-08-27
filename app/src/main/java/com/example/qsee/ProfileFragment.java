package com.example.qsee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class ProfileFragment extends Fragment {

    // ... Your code ...


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Button unameButton;

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        unameButton = view.findViewById(R.id.unameButton);

        // Example of using AppCompatActivity features in fragment
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            // Access support action bar if needed
                super.onCreate(savedInstanceState);
                activity.setContentView(R.layout.fragment_profile);

                unameButton.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        UserBottomSheetDialogFragment bottomSheetDialogFragment = new UserBottomSheetDialogFragment();
                        bottomSheetDialogFragment.show(getChildFragmentManager(), bottomSheetDialogFragment.getTag());

                }

        });

        
    }
        return view;
    }




    /*public void OnCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

            View unameButton = findViewById(R.id.unameButton);

            unameButton.setOnClickListener(new View.OnClickListener() {
    }*/

}


