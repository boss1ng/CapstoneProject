package com.example.qsee;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class AddGlimpseFragment extends DialogFragment {
    static final int CAMERA_REQUEST_CODE = 100;
    private static final int TARGET_WIDTH = 240;
    private static final int TARGET_HEIGHT = 180;
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private DatabaseReference databaseReference;
    private TextView caption;
    private TextView location;
    private Context context;
    private String userId;
    private ImageView imageDisplay;
    private ImageView cameraButton;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    private Uri imageUri;
    private Bitmap imageBitmap;
    private FusedLocationProviderClient fusedLocationClient;
    private Spinner categorySpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_glimpse, container, false);
        context = getActivity();
        userId = getArguments().getString("userId");
        cameraButton = rootView.findViewById(R.id.cameraBT);
        caption = rootView.findViewById(R.id.comment);
        imageDisplay = rootView.findViewById(R.id.imageView16);
        location = rootView.findViewById(R.id.location_ET);
        imageUri = null;
        categorySpinner = rootView.findViewById(R.id.categorySpinner);

        // Retrieve categories from Firebase and populate the Spinner
        retrieveCategoriesFromFirebase();

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        ImageView postButton = rootView.findViewById(R.id.post_bt);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageBitmap != null) {
                    uploadImageToFirebaseStorage(imageBitmap);
                } else {
                    Toast.makeText(context, "Image is null", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(context.getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(context, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap originalImage = (Bitmap) extras.get("data");
                    imageBitmap = adjustImage(originalImage);
                    imageDisplay.setImageBitmap(imageBitmap);
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Handle the user canceling the camera action
            }
        }
    }

    private Bitmap adjustImage(Bitmap inputBitmap) {
        if (inputBitmap == null) {
            return null;
        }
        return Bitmap.createScaledBitmap(inputBitmap, TARGET_WIDTH, TARGET_HEIGHT, true);
    }

    private void uploadImageToFirebaseStorage(Bitmap imageBitmap) {
        if (imageBitmap == null) {
            Toast.makeText(context, "Image is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the user's ID (you may need to retrieve it from your app's data)
        String userId = getArguments().getString("userId"); // Replace with the actual user ID

        // Generate a unique ID for the image file (e.g., timestamp)
        String uniqueId = String.valueOf(System.currentTimeMillis());

        // Create a filename using the user's ID and the unique ID
        String filename = userId + "_" + uniqueId + ".png";

        // Initialize Firebase Storage with the correct bucket name.
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://capstone-project-ffe21.appspot.com");

        // Create a reference to the Firebase Storage location with the generated filename.
        StorageReference storageRef = storage.getReference().child("Post/" + filename);

        // Convert the Bitmap to a byte array (PNG format for better quality).
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        // Upload the image to Firebase Storage
        uploadTask = storageRef.putBytes(data);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Handle the success here, e.g., get the download URL.
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUrl = uri.toString();
                        saveImageToDatabase(downloadUrl); // Save image URL and location
                    }
                });
            }
        });
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // You don't have the necessary permission, request it.
            ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, LOCATION_PERMISSION_REQUEST);
        } else {
            // Permission is already granted, proceed to get the location.
            getUserLocation();
        }
    }

    private void getUserLocation() {
        // Use fusedLocationClient to get the user's location as shown in the previous code.
    }

    // Add this method to handle permission request results
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed to get the location.
                getUserLocation();
            } else {
                // Permission denied, handle this case (e.g., show a message to the user).
                Toast.makeText(context, "Location permission denied. Location data won't be available.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void saveImageToDatabase(final String downloadUrl) {
        // Get the user-entered location and caption
        String userLocation = location.getText().toString();
        String textCaption = caption.getText().toString();
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        String userId = getArguments().getString("userId");
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Initialize the Firebase Database reference
                    databaseReference = FirebaseDatabase.getInstance().getReference(); // Change this to match your database structure

                    // Save the data to the Firebase Realtime Database
                    DatabaseReference newPostRef = databaseReference.child("Posts").push();
                    newPostRef.child("userId").setValue(userId);
                    newPostRef.child("imageUrl").setValue(downloadUrl);
                    newPostRef.child("caption").setValue(textCaption);
                    newPostRef.child("location").setValue(userLocation);
                    newPostRef.child("category").setValue(selectedCategory);
                    newPostRef.child("latitude").setValue(latitude);
                    newPostRef.child("longitude").setValue(longitude);
                    newPostRef.child("timestamp").setValue(ServerValue.TIMESTAMP);

                    // Dismiss the dialog after a successful upload
                    dismiss();
                } else {
                    // Handle the case where location data is not available
                    Toast.makeText(context, "Location data not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void retrieveCategoriesFromFirebase() {
        // Assuming you have set up Firebase properly, you can use Firebase Realtime Database reference
        DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference("Category");

        // Read data from Firebase
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> categoryList = new ArrayList<>();

                // Iterate through Firebase data and add categories to the list
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    // Check if the categorySnapshot contains the "name" field
                    if (categorySnapshot.hasChild("Category")) {
                        String category = categorySnapshot.child("Category").getValue(String.class);
                        if (category != null) {
                            categoryList.add(category);
                        }
                    }
                }

                // Create an ArrayAdapter to populate the Spinner with categories
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, categoryList);

                // Set the adapter to the Spinner
                categorySpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors if necessary
            }
        });
    }
}
