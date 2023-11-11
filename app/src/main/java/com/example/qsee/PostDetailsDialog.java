package com.example.qsee;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostDetailsDialog extends DialogFragment {
    private ImageView dialogImageView;
    private ImageView dialogUserImageView;
    private TextView dialogCaptionTextView;
    private TextView dialogCategoryTextView;
    private TextView dialogLocationTextView;
    private TextView dialogUsernTextView;
    private TextView dialogUserTextView;
    private TextView dialogTimePostedTextView;

    private String imageUrl;
    private String caption;
    private String category;
    private String location;
    private long timestamp;
    private String userId;

    private ImageView optionsButton;

    public void setUserData(String userId) {
        this.userId = userId;
    }

    public PostDetailsDialog() {
        // Empty constructor is required.
    }

    public void setData(String imageUrl, String caption, String category, String location, Long timestamp) {
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.category = category;
        this.location = location;
        this.timestamp = timestamp;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_post_details, container, false);

        dialogImageView = view.findViewById(R.id.dialogImageView);
        dialogCaptionTextView = view.findViewById(R.id.dialogCaptionTextView);
        dialogCategoryTextView = view.findViewById(R.id.dialogCategoryTextView);
        dialogLocationTextView = view.findViewById(R.id.dialogLocationTextView);
        dialogUserTextView = view.findViewById(R.id.dialogUsernameTextView);
        dialogUsernTextView = view.findViewById(R.id.dialogUserTextView);
        dialogUserImageView = view.findViewById(R.id.dialogUserImageView);
        dialogTimePostedTextView = view.findViewById(R.id.dialogTimePostedTextView);

        // Load the image using Picasso
        if (imageUrl != null) {
            Picasso.get().load(imageUrl).into(dialogImageView);
        }

        // Initialize notificationsReference
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("MobileUsers").child(userId);

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get encrypted user data from Firebase
                    String encryptedUsername = dataSnapshot.child("username").getValue(String.class);
                    String pfp = dataSnapshot.child("profilePictureUrl").getValue(String.class);

                    // Decrypt the values
                    String username = AESUtils.decrypt(encryptedUsername);

                    // Set the text of usernameTextView with the retrieved username
                    dialogUserTextView.setText(username);
                    dialogUsernTextView.setText(username);
                    Picasso.get().load(pfp).into(dialogUserImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
            }
        });

        // Set other data
        if (caption.equals(null) || caption.equals("")) {
            LinearLayout linearLayout = view.findViewById(R.id.layoutUserCaption);
            linearLayout.setVisibility(View.GONE);
        }

        else {
            dialogCaptionTextView.setText(caption);
            dialogCategoryTextView.setText(category);
        }

        if (location.equals(null) || location.equals(""))
            dialogLocationTextView.setVisibility(View.GONE);
        else
            dialogLocationTextView.setText(location);

        // Set the timestamp text
        if (timestamp != 0) {
            String formattedTimestamp = formatTimestamp(timestamp); // Implement a method to format the timestamp
            dialogTimePostedTextView.setText(formattedTimestamp);
            dialogTimePostedTextView.setVisibility(View.VISIBLE); // Make the TextView visible
        } else {
            dialogTimePostedTextView.setVisibility(View.GONE); // Hide the TextView if timestamp is null
        }


        optionsButton = view.findViewById(R.id.options);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Creating a PopupMenu
                PopupMenu popupMenu = new PopupMenu(getActivity(), optionsButton);

                // Inflate the menu from xml
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                // Apply the custom style
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    popupMenu.setGravity(Gravity.END);
                }

                // Set the item click listener
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        // Handle menu item click

                        if (menuItem.getItemId() == R.id.editPost) {
                            Toast.makeText(getContext(), "EDIT PRESSED", Toast.LENGTH_SHORT).show();
                        }

                        else if (menuItem.getItemId() == R.id.deletePost) {
                            Toast.makeText(getContext(), "DELETE PRESSED", Toast.LENGTH_SHORT).show();
                        }

                        return true;
                    }
                });

                // Show the popup menu
                popupMenu.show();

            }
        });

        return view;
    }
    private String formatTimestamp(long timestamp) {
        long now = System.currentTimeMillis();
        long differenceMillis = now - timestamp;

        long seconds = differenceMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (minutes > 0) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else {
            return "just now";
        }
    }
}
