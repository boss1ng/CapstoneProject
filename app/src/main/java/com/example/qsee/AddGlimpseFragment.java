package com.example.qsee;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddGlimpseFragment extends DialogFragment {
    static final int CAMERA_REQUEST_CODE = 100;
    private static final int TARGET_WIDTH = 1000;
    private static final int TARGET_HEIGHT = 1000;
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
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

    private Boolean isEstablishmentExisting = false;

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

        ImageView profilePic = rootView.findViewById(R.id.imageView19);
        TextView usernameText = rootView.findViewById(R.id.username_Tv);

        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("MobileUsers");

        userReference.orderByChild("userId").equalTo(userId)
        .addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot mobileUsersSnapshot) {
                for (DataSnapshot userSnapshot : mobileUsersSnapshot.getChildren()) {
                    usernameText.setText(AESUtils.decrypt(userSnapshot.child("username").getValue(String.class)));
                    String profilePictureUrl = userSnapshot.child("profilePictureUrl").getValue(String.class);

                    if (profilePictureUrl == null)
                        profilePic.setImageResource(R.drawable.profilepicture);
                    else
                        loadUserPostImage(profilePictureUrl, profilePic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
                    postButton.setEnabled(false);
                    categorySpinner.setEnabled(false);
                    caption.setEnabled(false);
                    cameraButton.setEnabled(false);
                    location.setEnabled(false);

                    Toast.makeText(context, "Activity Posting...", Toast.LENGTH_LONG).show();
                    uploadImageToFirebaseStorage(imageBitmap);
                } else {
                    Toast.makeText(context, "Kindly take a picture of the establishment.", Toast.LENGTH_LONG).show();
                }
            }
        });

        return rootView;
    }

    private void loadUserPostImage(String imageUrl, ImageView profilePic) {
        // Use a library like Picasso or Glide to load and display the image
        if (profilePic.getContext() != null && imageUrl != null) {
            Picasso.get().load(imageUrl).fit().into(profilePic);
        }
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
            Toast.makeText(context, "Kindly take a picture of the establishment.", Toast.LENGTH_LONG).show();
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

    private void saveImageToDatabase(final String downloadUrl) {
        // Get the user-entered location and caption
        String userLocation = location.getText().toString();
        String textCaption = caption.getText().toString();
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        String userId = getArguments().getString("userId");

        DatabaseReference databaseLocationReference = FirebaseDatabase.getInstance().getReference().child("Location");
        databaseLocationReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot placeSnapshot : snapshot.getChildren()) {
                        String name = placeSnapshot.child("Location").getValue(String.class);

                        if (name.equals(userLocation)) {
                            isEstablishmentExisting = true;
                        }
                    }

                    if (isEstablishmentExisting) {
                        postDetails(userLocation, downloadUrl, textCaption, selectedCategory);
                    }

                    else {
                        postDetails(userLocation, downloadUrl, textCaption, selectedCategory);
                        postRss(userLocation); // downloadUrl selectedCategory
                    }
                }

                else {

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void postDetails(String userLocation, String downloadUrl, String textCaption, String selectedCategory) {

        // Check if location permission is granted
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Request location permission if not granted
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
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

                    Toast.makeText(context, "Activity Posted.", Toast.LENGTH_LONG).show();

                    // Dismiss the dialog after a successful upload
                    dismiss();
                }

                else {
                    // Handle the case where location data is not available
                    Toast.makeText(context, "Location data not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void postRss(String userLocation) {

        final String[] pushKey = {null};
        final Boolean[] isUserExisting = {false};

        Geocoder geocoder = new Geocoder(context);

        // Check if location permission is granted
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Request location permission if not granted
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Initialize the Firebase Database reference
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("RSS");

                    // Create a query to check if EstablishmentName matches the target name
                    Query query = databaseReference.orderByChild("EstablishmentName").equalTo(userLocation);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Check if userId already posted that establishment. If yes, skip. If no, update numPosts

                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                                    pushKey[0] = postSnapshot.getKey();

                                    String numberPostsFirebase = postSnapshot.child("numPosts").getValue(String.class);
                                    //Toast.makeText(getContext(), numberReportsFirebase, Toast.LENGTH_SHORT).show();

                                    // Access the "Users" node under the specific report
                                    DataSnapshot usersSnapshot = postSnapshot.child("Users");

                                    // Iterate through the "Users" under this report
                                    for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
                                        String key = userSnapshot.getKey(); // Get the key ("-NhtkyoZUylTyqoeHvLQ")
                                        String value = userSnapshot.getValue(String.class); // Get the value ("5456073013")

                                        //Toast.makeText(getContext(), key, Toast.LENGTH_SHORT).show();
                                        //Toast.makeText(getContext(), value, Toast.LENGTH_SHORT).show();

                                        if (value.equals(userId)) {
                                            isUserExisting[0] = true;
                                        }
                                    }

                                    if (isUserExisting[0]) {
                                        //Toast.makeText(getContext(), "Establishment already reported.", Toast.LENGTH_LONG).show();

                                    }

                                    else {
                                        // Convert numReports to an integer, update it, and set it back
                                        int intNumReports = Integer.parseInt(numberPostsFirebase);
                                        intNumReports++;
                                        postSnapshot.getRef().child("NumReports").setValue(String.valueOf(intNumReports));

                                        DatabaseReference pushKeyRef = databaseReference.child(pushKey[0]).child("Users");

                                        // Append a new child node under "Users" with the key as "UserId" and the value as the user ID
                                        pushKeyRef.push().setValue(userId);
                                    }


                                }
                            }

                            // Add report on existing rss
                            else {
                                // new record of RSS
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

                                        // Do something with the address components

                                        // Save the data to the Firebase Realtime Database
                                        DatabaseReference rssPost = databaseReference.push();

                                        rssPost.child("EstablishmentName").setValue(userLocation);
                                        rssPost.child("numPosts").setValue("1");
                                        //rssPost.child("userId").setValue(userId);
                                        rssPost.child("Users").push().setValue(userId);

                                        rssPost.child("completeAddress").setValue(completeAddress);
                                        rssPost.child("city").setValue(city);
                                        rssPost.child("state").setValue(state);
                                        rssPost.child("postalCode").setValue(postalCode);
                                        rssPost.child("country").setValue(country);

                                    } else {
                                        // Geocoder couldn't find an address for the given latitude and longitude
                                    }
                                } catch (IOException e) {
                                    // Handle geocoding errors (e.g., network issues, service not available)
                                    throw new RuntimeException(e);
                                }

                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

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
