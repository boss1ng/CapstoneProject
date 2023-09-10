package com.example.qsee;

import static android.content.Intent.getIntent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ProfileFragment extends Fragment {
    private TextView usernameTextView;
    private String username;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the fragment_profile.xml layout
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Retrieve the username from the arguments
        username = getArguments().getString("username");

        // Find the "Change Username" button
        Button unameButton = rootView.findViewById(R.id.unameButton);
        unameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the UserBottomSheetDialogFragment
                UserBottomSheetDialogFragment bottomSheetDialog = new UserBottomSheetDialogFragment();
                bottomSheetDialog.show(getParentFragmentManager(), bottomSheetDialog.getTag());
            }
        });
        // Find the usernameTextView by its ID
        usernameTextView = rootView.findViewById(R.id.ProfileUsername);

        // Set the text of usernameTextView with the retrieved username
        usernameTextView.setText(username);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the ViewPager2 and TabLayout
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);

        // Create and set the adapter for ViewPager2, passing the username
        ProfilePagerAdapter pagerAdapter = new ProfilePagerAdapter(getChildFragmentManager(), getLifecycle(), username);
        viewPager.setAdapter(pagerAdapter);

        // Attach TabLayout to ViewPager2 using TabLayoutMediator
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setIcon(R.drawable.baseline_image_24);
                            break;
                        case 1:
                            tab.setIcon(R.drawable.baseline_group_24);
                            break;
                        case 2:
                            tab.setIcon(R.drawable.ic_maps);
                            break;
                    }
                }).attach();
    }
}


