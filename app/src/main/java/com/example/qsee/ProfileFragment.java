package com.example.qsee;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private TextView userFullNameTextView;
    private TextView userIdTextView;
    private TextView usernameTextView;
    private Context context;
    private String userId;
    private TextView notificationBadge;
    private NotificationAdapter notificationAdapter;
    private int notificationCount = 0;


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

        // Call showNotificationCount to update the notification count
        showNotificationCount();

        // Initialize the notificationBadge
        notificationBadge = rootView.findViewById(R.id.notificationBadge);

        TextView uname = rootView.findViewById(R.id.ProfileUsername);

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

        Button notifButton = rootView.findViewById(R.id.notifButton);
        notifButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the custom notification container when the button is clicked
                showNotificationContainer();
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
                    Toast.makeText(context, "No notifications found", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(context, "Notifications is empty", Toast.LENGTH_SHORT).show();
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
    }
}