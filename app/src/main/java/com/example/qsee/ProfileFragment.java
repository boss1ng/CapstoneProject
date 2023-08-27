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

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        Button unameButton = rootView.findViewById(R.id.unameButton);
        unameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserBottomSheetDialogFragment bottomSheetDialog = new UserBottomSheetDialogFragment();
                bottomSheetDialog.show(getParentFragmentManager(), bottomSheetDialog.getTag());
            }
        });

        return rootView;
    }

}


