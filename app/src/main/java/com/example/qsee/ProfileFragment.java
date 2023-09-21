package com.example.qsee;

import static android.content.Intent.getIntent;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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
    private TextView userFullNameTextView;
    private TextView userIdTextView;
    private TextView usernameTextView;
    private Context context;
    private String userId;
    // Create a ViewModel instance


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the fragment_profile.xml layout
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        context = getActivity(); // Get the context

        // Retrieve the username from the arguments
        userId = getArguments().getString("userId");

        TextView uname = rootView.findViewById(R.id.ProfileUsername);

        uname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the UserBottomSheetDialogFragment
                UserBottomSheetDialogFragment bottomSheetDialog = new UserBottomSheetDialogFragment().newInstance(userId);
                bottomSheetDialog.show(getParentFragmentManager(), bottomSheetDialog.getTag());
            }
        });

        // Find the "Change Username" button
        Button unameButton = rootView.findViewById(R.id.unameButton);
        Button editProfile = rootView.findViewById(R.id.editButton);
        unameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the UserBottomSheetDialogFragment
                // Show the UserBottomSheetDialogFragment
                UserBottomSheetDialogFragment bottomSheetDialog = UserBottomSheetDialogFragment.newInstance(userId);
                bottomSheetDialog.show(getParentFragmentManager(), bottomSheetDialog.getTag());
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pass the username to EditProfileFragment
                EditProfileFragment editProfileFragment = EditProfileFragment.newInstance(userId);

                // Replace the current fragment with the "EditProfileFragment"
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, editProfileFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });


        // Find the userFullNameTextView and userIdTextView by their IDs
        userFullNameTextView = rootView.findViewById(R.id.UserFullName);
        userIdTextView = rootView.findViewById(R.id.UserId);

        // Set a click listener for userIdTextView to copy its text to the clipboard
        userIdTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the text from userIdTextView
                String userId = userIdTextView.getText().toString().trim();

                // Extract only the numeric part from the text
                String numericPart = userId.replaceAll("[^0-9]", "");

                if (!numericPart.isEmpty()) {
                    try {
                        // Copy the numeric part to the clipboard
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("UserId", numericPart);
                        clipboard.setPrimaryClip(clip);

                        // Show a toast message indicating that the numeric part has been copied
                        Toast.makeText(context, "User ID copied to clipboard", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        // Handle the exception (e.g., show a message to the user)
                        Toast.makeText(context, "Clipboard functionality is not available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Show a message if there are no numeric characters to copy
                    Toast.makeText(context, "No numeric part found in User ID", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Retrieve user's data from Firebase based on the username
        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("MobileUsers");
        Query query = usersReference.orderByChild("userId").equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        // Get encrypted user data from Firebase
                        String encryptedFirstName = userSnapshot.child("firstName").getValue(String.class);
                        String encryptedLastName = userSnapshot.child("lastName").getValue(String.class);
                        String encryptedUsername = userSnapshot.child("username").getValue(String.class);

                        // Decrypt the values
                        String firstName = AESUtils.decrypt(encryptedFirstName);
                        String lastName = AESUtils.decrypt(encryptedLastName);
                        String username = AESUtils.decrypt(encryptedUsername);

                        // Set the text of userFullNameTextView with the full name
                        userFullNameTextView.setText(firstName + " " + lastName);

                        // Set the text of userIdTextView with the userId
                        userIdTextView.setText("User ID: " + userId);

                        // Find the usernameTextView by its ID
                        usernameTextView = rootView.findViewById(R.id.ProfileUsername);

                        // Set the text of usernameTextView with the retrieved username
                        usernameTextView.setText(username);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
            }
        });



        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the ViewPager2 and TabLayout
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);

        // Create and set the adapter for ViewPager2, passing the userId
        ProfilePagerAdapter pagerAdapter = new ProfilePagerAdapter(getChildFragmentManager(), getLifecycle(), userId);
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


