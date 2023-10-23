package com.example.qsee;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;  // Import for screen orientation control
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class AddGlimpseFragment extends DialogFragment {
    private Context context;
    private String userId;
    private TextView usernameTextView;
    private ImageView imageView16; // Reference to your image view
    private static final int CAMERA_REQUEST_CODE = 100; // Define your request code here

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Lock the orientation to portrait mode for this fragment
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_add_glimpse, container, false);

        // Initialize your views
        context = getActivity();
        userId = getArguments().getString("userId");
        imageView16 = rootView.findViewById(R.id.imageView16); // Initialize imageView16

        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("MobileUsers");
        Query query = usersReference.orderByChild("userId").equalTo(userId);

        // Find the usernameTextView by its ID
        usernameTextView = rootView.findViewById(R.id.username_Tv);

        ImageView cameraImage = rootView.findViewById(R.id.imageView28);
        TextView cameraText = rootView.findViewById(R.id.textView24);
        cameraImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        cameraText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        // Get encrypted user data from Firebase
                        String encryptedUsername = userSnapshot.child("username").getValue(String.class);

                        String username = AESUtils.decrypt(encryptedUsername);

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

        // Add your code to handle adding a glimpse here

        return rootView;
    }

    private void openCamera() {
        // Create an intent to launch the camera app
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Check if there's a camera app available to handle the intent
        if (cameraIntent.resolveActivity(context.getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            // Handle the case where there's no camera app available
            Toast.makeText(context, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    // You may also need to handle the result of taking a photo
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // The user took a photo, and it should be available in the Intent data
                Bundle extras = data.getExtras();
                if (extras != null) {
                    // Get the captured image as a Bitmap
                    Bitmap imageBitmap = (Bitmap) extras.get("data");

                    // Set the captured image to the ImageView
                    ImageView imageView16 = getView().findViewById(R.id.imageView16);
                    // Apply the 4:3 aspect ratio
                    int targetWidth = imageView16.getWidth();
                    int targetHeight = (int) (targetWidth / 1.3333); // 4:3 aspect ratio
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, targetWidth, targetHeight, true);
                    imageView16.setImageBitmap(scaledBitmap);
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled taking a photo
                // Handle this case if needed
            }
        }
    }
}
