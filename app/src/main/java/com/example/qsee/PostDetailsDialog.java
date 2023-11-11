package com.example.qsee;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class PostDetailsDialog extends DialogFragment {
    private ImageView dialogImageView;
    private ImageView dialogUserImageView;
    private TextView dialogCaptionTextView;
    private TextView dialogCategoryTextView;
    private TextView dialogLocationTextView;
    private TextView dialogUsernTextView;
    private TextView dialogUserTextView;

    private String imageUrl;
    private String caption;
    private String category;
    private String location;
    private String userId;

    public void setUserData(String userId) {
        this.userId = userId;
    }

    public PostDetailsDialog() {
        // Empty constructor is required.
    }

    public void setData(String imageUrl, String caption, String category, String location) {
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.category = category;
        this.location = location;
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
        dialogCaptionTextView.setText(caption);
        dialogCategoryTextView.setText(category);
        dialogLocationTextView.setText(location);

        return view;
    }
}
