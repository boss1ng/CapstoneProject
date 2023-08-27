package com.example.qsee;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class UserBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public UserBottomSheetDialogFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_options_bottom_sheet, container, false);

        TextView registeredLocationsOption = rootView.findViewById(R.id.menu_registered_locations);
        TextView registerLocationOption = rootView.findViewById(R.id.menu_register_location);
        TextView changePasswordOption = rootView.findViewById(R.id.menu_change_password);
        TextView signOutOption = rootView.findViewById(R.id.menu_sign_out);

        registeredLocationsOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Registered Locations option click
            }
        });

        registerLocationOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Register a Location option click
            }
        });

        changePasswordOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Change Password option click
            }
        });

        signOutOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Sign Out option click
            }
        });

        return rootView;
    }

}
