package com.example.qsee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class EditProfileFragment extends Fragment {

    public EditProfileFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);

        Button cancel = view.findViewById(R.id.cancelBt);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace the current fragment with the "fragment_quiz" fragment
                ProfileFragment profileFragment = new ProfileFragment();
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, profileFragment); // Replace 'fragment_container' with the ID of your container layout
                transaction.addToBackStack(null); // Optional: Add the transaction to the back stack
                transaction.commit();
            }
        });

        return view;
    }
}
