package com.example.qsee;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class UserBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public UserBottomSheetDialogFragment() {
        // Required empty public constructor
    }

    // Declaration of userId
    private String userId;

    public static UserBottomSheetDialogFragment newInstance(String userId) {
        UserBottomSheetDialogFragment fragment = new UserBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_options_bottom_sheet, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Retrieve the userId argument inside onCreateView
        Bundle args = getArguments();
        if (args != null) {
            userId = args.getString("userId");
        } else {
            // Handle the case where userId is not provided
            // You might want to log an error or take appropriate action
        }

        TextView changePasswordOption = rootView.findViewById(R.id.menu_change_password);
        TextView signOutOption = rootView.findViewById(R.id.menu_sign_out);

        changePasswordOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pass the userId to the EditPasswordFragment
                EditPasswordFragment editPasswordFragment = EditPasswordFragment.newInstance(userId);
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, editPasswordFragment); // Replace 'fragment_container' with the ID of your container layout
                transaction.addToBackStack(null); // Optional: Add the transaction to the back stack
                transaction.commit();
                dismiss(); // Close the bottom sheet dialog after the fragment is replaced
            }
        });

        signOutOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Sign Out option click
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
                dismiss(); // Close the bottom sheet dialog after starting the new activity
            }
        });

        return rootView;
    }
}
