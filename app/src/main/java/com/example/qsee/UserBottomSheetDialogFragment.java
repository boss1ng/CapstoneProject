package com.example.qsee;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class    UserBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public UserBottomSheetDialogFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_options_bottom_sheet, container, false);

        TextView changePasswordOption = rootView.findViewById(R.id.menu_change_password);
        TextView signOutOption = rootView.findViewById(R.id.menu_sign_out);
        changePasswordOption.setOnClickListener(new View.OnClickListener() {
               public void onClick(View v) {
                   // Replace the current fragment with the "fragment_quiz" fragment
                   EditPasswordFragment editPasswordFragment = new EditPasswordFragment();
                   FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                   transaction.replace(R.id.fragment_container, editPasswordFragment); // Replace 'fragment_container' with the ID of your container layout
                   transaction.addToBackStack(null); // Optional: Add the transaction to the back stack
                   transaction.commit();
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
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        return rootView;
    }

}
