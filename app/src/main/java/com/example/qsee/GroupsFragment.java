package com.example.qsee;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class GroupsFragment extends Fragment {
    private String username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        // Retrieve the username from the arguments
        Bundle args = getArguments();
        if (args != null) {
            username = args.getString("username");
            // Now you have the username, and you can use it in this fragment
            Log.d("Profile",username);
        }

        return view;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton addGroupBtn = view.findViewById(R.id.addGroupBtn);

        addGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the AddGroupFragment as a pop-up dialog
                showAddGroupDialog();
            }
        });
    }

    private void showAddGroupDialog() {
        FragmentManager fragmentManager = getChildFragmentManager();
        AddGroupFragment addGroupFragment = new AddGroupFragment(username);

        // Pass the stored username to AddGroupFragment using arguments
        Bundle args = new Bundle();
        args.putString("username", username);
        addGroupFragment.setArguments(args);

        addGroupFragment.show(fragmentManager, "AddGroupFragment");
    }
}
