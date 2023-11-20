package com.example.qsee;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    boolean isUserInQuezonCity = true;


    private TextView userFullNameTextView;
    private TextView usernameTextView;
    private Context context;
    private String userId;
    private TextView notificationBadge;
    private NotificationAdapter notificationAdapter;
    private int notificationCount = 0;
    private ImageView Pfp;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the fragment_profile.xml layout
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Load the background image using Picasso
        String imageUrl = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/profbg.jpg?alt=media&token=4b33d94a-47f5-48f3-90f4-c768b7f0480f";
        ImageView backgroundImageView = new ImageView(getActivity());

        // Set a listener to be notified when the image is loaded
        Picasso.get().load(imageUrl).into(backgroundImageView, new Callback() {
            @Override
            public void onSuccess() {
                // Set the background of the profileCont LinearLayout
                LinearLayout profileContLayout = rootView.findViewById(R.id.profileCont);
                profileContLayout.setBackground(backgroundImageView.getDrawable());
            }

            @Override
            public void onError(Exception e) {
                // Handle error if necessary
            }
        });

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        context = getActivity(); // Get the context

        // Retrieve the username from the arguments
        userId = getArguments().getString("userId");

        // Initialize the FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Call showNotificationCount to update the notification count
        showNotificationCount();

        // Initialize the notificationBadge
        notificationBadge = rootView.findViewById(R.id.notificationBadge);
        Pfp = rootView.findViewById(R.id.profilePic);
        TextView uname = rootView.findViewById(R.id.ProfileUsername);

        // Retrieve the profile picture URL from Firebase
        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("MobileUsers");
        Query query = usersReference.orderByChild("userId").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        // Get the profile picture URL from Firebase
                        String profilePictureUrl = userSnapshot.child("profilePictureUrl").getValue(String.class);

                        // Load and display the profile picture
                        loadProfilePicture(profilePictureUrl);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
            }
        });

        uname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the UserBottomSheetDialogFragment
                UserBottomSheetDialogFragment bottomSheetDialog = new UserBottomSheetDialogFragment().newInstance(userId);
                bottomSheetDialog.show(getParentFragmentManager(), bottomSheetDialog.getTag());

                //PlaceDetailFragment pdf = new PlaceDetailFragment().newInstance(userId);
                //pdf.show(getParentFragmentManager(), pdf.getTag());

            }
        });

        ImageView addButton = rootView.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check if location permission is granted
                if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Request location permission if not granted
                    requestPermissions(new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, LOCATION_PERMISSION_REQUEST_CODE);
                }

                // Get the user's last known location and move the camera there
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

/*
                        Geocoder geocoder = new Geocoder(context);

                        try {
                            List<android.location.Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                            if (addresses != null && addresses.size() > 0) {
                                Address address = addresses.get(0);

                                // You can now extract address components
                                String completeAddress = address.getAddressLine(0); // Full street address
                                String city = address.getLocality();
                                String state = address.getAdminArea();
                                String postalCode = address.getPostalCode();
                                String country = address.getCountryName();

                                / *
                                https://quezoncity.com/000001a/000001b/qc+links/backups/quezon+city+zip+code+6.html
                                1105	1102	1106	1116	1109	1111	1119	1110	1115	1126	1120	1101	1117	1100	1121
                                1128	1104	1112	1123	1113	1125	1118	1122	1114	1127	1124	1103	1108	1107
                                 * /

                                if (completeAddress.contains("Metro Manila")) {

                                        if (completeAddress.contains("1105") || completeAddress.contains("1102") || completeAddress.contains("1106") || completeAddress.contains("1116") ||
                                                completeAddress.contains("1109") || completeAddress.contains("1111") || completeAddress.contains("1119") || completeAddress.contains("1110") ||
                                                completeAddress.contains("1115") || completeAddress.contains("1126") || completeAddress.contains("1120") || completeAddress.contains("1101") ||
                                                completeAddress.contains("1117") || completeAddress.contains("1100") || completeAddress.contains("1121") || completeAddress.contains("1128") ||
                                                completeAddress.contains("1104") || completeAddress.contains("1112") || completeAddress.contains("1123") || completeAddress.contains("1113") ||
                                                completeAddress.contains("1125") || completeAddress.contains("1118") || completeAddress.contains("1122") || completeAddress.contains("1114") ||
                                                completeAddress.contains("1127") || completeAddress.contains("1124") || completeAddress.contains("1103") || completeAddress.contains("1108") ||
                                                completeAddress.contains("1107") || completeAddress.contains("Quezon City")) {

                                            isUserInQuezonCity = true;
                                        }

                                        else
                                            isUserInQuezonCity = false;
                                }

                                else
                                    isUserInQuezonCity = false;

                            } else {
                                // Geocoder couldn't find an address for the given latitude and longitude
                            }
                        } catch (IOException e) {
                            // Handle geocoding errors (e.g., network issues, service not available)
                            throw new RuntimeException(e);
                        }
*/

                        if (isUserInQuezonCity) {
                            // The user is within Quezon City
                            // Create an instance of the AddGlimpseFragment
                            AddGlimpseFragment addGlimpseFragment = new AddGlimpseFragment();

                            // Create a Bundle to pass the userId as an argument
                            Bundle args = new Bundle();
                            args.putString("userId", userId); // Replace "your_user_id_here" with the actual user ID
                            addGlimpseFragment.setArguments(args);

                            // Show the AddGlimpseFragment as a dialog
                            FragmentManager fragmentManager = getChildFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            addGlimpseFragment.show(transaction, "add_glimpse_dialog"); // You can provide a tag for the dialog
                        }

                        else {
                            // The user is outside Quezon City
                            addButton.setEnabled(false);

                            Toast.makeText(getContext(), "You are outside Quezon City.", Toast.LENGTH_LONG).show();

                            // Create a Handler to introduce a delay
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Display the second Toast with LENGTH_LONG duration after a delay
                                    Toast.makeText(getContext(), "You won't be able to post.", Toast.LENGTH_LONG).show();
                                }
                            }, 3500); // 2000 milliseconds (2 seconds) delay
                        }
                    }
                });
            }
        });

        ImageView notifButton = rootView.findViewById(R.id.notifButton);
        notifButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the custom notification container when the button is clicked
                showNotificationContainer();
            }
        });

        // Find the "Change Username" button
        ImageView unameButton = rootView.findViewById(R.id.unameButton);
        ImageView editProfile = rootView.findViewById(R.id.editButton);
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

        // Retrieve selected categories from Bundle arguments
        Bundle getBundle = getArguments();

        if (getBundle != null) {
            String userID = getBundle.getString("userId");
            //Toast.makeText(getContext(), userID, Toast.LENGTH_LONG).show();
        }

        BottomNavigationView bottomNavigationView = rootView.findViewById(R.id.bottomNavigationView);
        // Set the default item as highlighted
        MenuItem defaultItem = bottomNavigationView.getMenu().findItem(R.id.action_profile);
        defaultItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.action_home) {
                    loadFragment(new HomeFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                    LinearLayout linearLayout = rootView.findViewById(R.id.profileCont);
                    linearLayout.setVisibility(View.GONE);

                } else if (itemId == R.id.action_search) {
                    loadFragment(new SearchFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                    LinearLayout linearLayout = rootView.findViewById(R.id.profileCont);
                    linearLayout.setVisibility(View.GONE);

                } else if (itemId == R.id.action_maps) {
                    loadFragment(new MapsFragment());
                    //BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
                    bottomNavigationView.setVisibility(View.GONE);
                    LinearLayout linearLayout = rootView.findViewById(R.id.profileCont);
                    linearLayout.setVisibility(View.GONE);

                } else if (itemId == R.id.action_quiz) {
                    loadFragment(new StartQuizFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                    LinearLayout linearLayout = rootView.findViewById(R.id.profileCont);
                    linearLayout.setVisibility(View.GONE);

                } else if (itemId == R.id.action_profile) {
                    loadFragment(new ProfileFragment());
                    bottomNavigationView.setVisibility(View.GONE);
                    LinearLayout linearLayout = rootView.findViewById(R.id.profileCont);
                    linearLayout.setVisibility(View.GONE);
                }
                return true;
            }
        });

        return rootView;
    }

    private void loadFragment(Fragment fragment) {
        //Bundle bundle = new Bundle();
        //bundle.putString("userId", userId);
        //fragment.setArguments(bundle);

        // Use Bundle to pass values
        Bundle bundle = new Bundle();

        // Retrieve selected categories from Bundle arguments
        Bundle getBundle = getArguments();

        if (getBundle != null) {
            String userID = getBundle.getString("userId");
            bundle.putString("userId", userID);
            fragment.setArguments(bundle);
        }

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Update the notification count in your showNotificationCount method
    private void showNotificationCount() {
        // Replace "YourNotificationNode" with the actual node path where notifications are stored in your Firebase database
        DatabaseReference notificationsReference = FirebaseDatabase.getInstance().getReference("Notifications");

        // Query the notifications based on the user's invitedUserId (receiver's ID)
        DatabaseReference userNotificationsReference = notificationsReference.child(userId);

        userNotificationsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    notificationCount = (int) dataSnapshot.getChildrenCount(); // Get the count of notifications
                    updateNotificationBadge(); // Update the notification badge
                } else {
                    notificationCount = 0;
                    updateNotificationBadge(); // Update the notification badge (hide it)
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error if needed
                Log.e("ProfileFragment", "Failed to fetch notifications", databaseError.toException());
            }
        });
    }

    private void updateNotificationBadge() {
        if (notificationCount > 0) {
            notificationBadge.setVisibility(View.VISIBLE);
            notificationBadge.setText(String.valueOf(notificationCount));
        } else {
            notificationBadge.setVisibility(View.GONE);
        }
    }


    private void showNotificationContainer() {
        // Replace "YourNotificationNode" with the actual node path where notifications are stored in your Firebase database
        DatabaseReference notificationsReference = FirebaseDatabase.getInstance().getReference("Notifications");

        // Query the notifications based on the user's invitedUserId (receiver's ID)
        DatabaseReference userNotificationsReference = notificationsReference.child(userId);

        userNotificationsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Create and configure a custom dialog
                    Dialog notificationDialog = new Dialog(context);
                    notificationDialog.setContentView(R.layout.notification_dialog); // Set the custom dialog layout

                    // Find the RecyclerView within the custom dialog layout
                    RecyclerView recyclerView = notificationDialog.findViewById(R.id.notificationRecyclerView);

                    // Initialize the NotificationAdapter
                    notificationAdapter = new NotificationAdapter(getActivity(), new ArrayList<>(), userId);
                    recyclerView.setAdapter(notificationAdapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));

                    // Fetch and display notifications when the dialog is shown
                    fetchAndDisplayNotifications();

                    // Show the dialog
                    notificationDialog.show();

                    // Set a dismiss listener for the dialog
                    notificationDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            // The dialog is dismissed, recreate the ProfileFragment
                            recreateProfileFragment();
                        }
                    });
                } else {
                    // Show a toast message when there are no notifications
                    Toast.makeText(context, "No notifications found.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error if needed
                Log.e("ProfileFragment", "Failed to fetch notifications", databaseError.toException());
            }
        });
    }

    private void recreateProfileFragment() {
        // Create a new instance of ProfileFragment
        ProfileFragment newProfileFragment = new ProfileFragment();

        // Create a bundle to pass any required data to the new instance, such as userId
        Bundle args = new Bundle();
        args.putString("userId", userId);
        newProfileFragment.setArguments(args);

        // Replace the current fragment with the new instance
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newProfileFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    // Fetch notification data from Firebase or your data source
    private void fetchAndDisplayNotifications() {
        List<Notification> notifications = new ArrayList<>();

        // Replace "YourNotificationNode" with the actual node path where notifications are stored in your Firebase database
        DatabaseReference notificationsReference = FirebaseDatabase.getInstance().getReference("Notifications");

        // Query the notifications based on the user's invitedUserId (receiver's ID)
        DatabaseReference userNotificationsReference = notificationsReference.child(userId);

        userNotificationsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot notificationSnapshot : dataSnapshot.getChildren()) {
                        // Retrieve notification data from Firebase
                        String notificationId = notificationSnapshot.getKey(); // Use the key as the notificationId
                        String senderId = notificationSnapshot.child("senderId").getValue(String.class);
                        String groupId = notificationSnapshot.child("groupId").getValue(String.class);

                        // Query the MobileUsers node to get the sender's username
                        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("MobileUsers");
                        usersReference.child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                if (userSnapshot.exists()) {
                                    String senderUsername = userSnapshot.child("username").getValue(String.class);
                                    String decryptedUsername = AESUtils.decrypt(senderUsername);

                                    // Create a Notification object and add it to the list
                                    Notification notification = new Notification(notificationId, decryptedUsername, groupId);
                                    notification.setMessage(senderId);
                                    notifications.add(notification);

                                    // Update the notification adapter with the retrieved notifications
                                    notificationAdapter.updateData(notifications);
                                }

                                // Check if the notifications list is empty
                                if (notifications.isEmpty()) {
                                    // Show a toast message when there are no items in the notification list
                                    Toast.makeText(context, "Notifications is .", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle the error if needed
                                Log.e("ProfileFragment", "Failed to fetch sender's username", databaseError.toException());
                            }
                        });
                    }
                } else {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error if needed
                Log.e("ProfileFragment", "Failed to fetch notifications", databaseError.toException());
            }
        });
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

        if (getArguments() != null) {
            String caseNumber1 = getArguments().getString("fromGroup");
            if (caseNumber1 != null) {
                if (caseNumber1.equals("1")) {
                    // Set the current item to the second tab (Group)
                    viewPager.setCurrentItem(Integer.parseInt(caseNumber1), false); // 'false' means no smooth scroll
                }
            }

            String caseNumber2 = getArguments().getString("fromItinerary");
            if (caseNumber2 != null) {
                if (caseNumber2.equals("2")) {
                    // Set the current item to the second tab (Group)
                    viewPager.setCurrentItem(Integer.parseInt(caseNumber2), false); // 'false' means no smooth scroll
                }
            }
        }

        //bundle.putString("fromGroup", "1");

    }
    private void loadProfilePicture(String profilePictureUrl) {
        // Use a library like Picasso or Glide to load and display the image
        if (getContext() != null && profilePictureUrl != null) {
            Picasso.get()
                    .load(profilePictureUrl)
                    .resize(200, getView().getMinimumHeight()) // Set the target dimensions
                    .centerCrop()
                    .into(Pfp);
        }
    }
}