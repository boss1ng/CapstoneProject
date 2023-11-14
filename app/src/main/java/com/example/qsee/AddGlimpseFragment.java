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
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.Arrays;
import java.util.Collections;
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

    private String filename;
    private String uniqueId;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Initialize the Places API here
        Places.initialize(context, getString(R.string.google_maps_api_key));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_glimpse, container, false);
        context = getActivity();
        userId = getArguments().getString("userId");
        cameraButton = rootView.findViewById(R.id.cameraBT);

        caption = rootView.findViewById(R.id.comment);
        // Set maximum number of characters (e.g., 10 characters)
        int maxLength = 250;
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(maxLength);
        caption.setFilters(filters);

        imageDisplay = rootView.findViewById(R.id.imageView16);
        imageDisplay.setVisibility(View.GONE);

        // Your existing layout
        LinearLayout linearLayout = rootView.findViewById(R.id.postContent);

        // Set a minimum height in pixels
        int minHeightInPixels = 400; // Set your desired minimum height
        linearLayout.setMinimumHeight(minHeightInPixels);

        // Alternatively, set a minimum height in density-independent pixels (dp)
        int minHeightInDp = 100; // Set your desired minimum height in dp
        int minHeightInPixelsDp = (int) (minHeightInDp * getResources().getDisplayMetrics().density);
        linearLayout.setMinimumHeight(minHeightInPixelsDp);


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
            Toast.makeText(context, "No camera app found", Toast.LENGTH_LONG).show();
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
                    imageDisplay.setVisibility(View.VISIBLE);
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
        uniqueId = String.valueOf(System.currentTimeMillis());

        // Create a filename using the user's ID and the unique ID
        filename = userId + "_" + uniqueId + ".png";

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
                Toast.makeText(context, "Location permission denied. Location data won't be available.", Toast.LENGTH_LONG).show();
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

                        if (name != null) {
                            if (name.equals(userLocation)) {
                                isEstablishmentExisting = true;
                                break;
                            }
                        }
                    }

                    /*
                    if (isEstablishmentExisting) {
                        postDetails(userLocation, downloadUrl, textCaption, selectedCategory);
                    }

                    else {
                        postDetails(userLocation, downloadUrl, textCaption, selectedCategory);
                        postRss(userLocation, selectedCategory, downloadUrl);
                    }
                     */
                }

                else {

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (isEstablishmentExisting) {
            postDetails(userLocation, downloadUrl, textCaption, selectedCategory);
        }

        else {
            postDetails(userLocation, downloadUrl, textCaption, selectedCategory);
            postRss(userLocation, selectedCategory, downloadUrl);
        }
    }

    private void postDetails(String userLocation, String downloadUrl, String textCaption, String selectedCategory) {

        if (isAdded()) {
            // The fragment is attached to a context, so it's safe to use requireContext().
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

        } else {
            // Handle the case where the fragment is not attached to a context.
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

                    Bundle getBundle = getArguments();
                    if (getBundle != null) {
                        String fromHome = getBundle.getString("fromHome");
                        if (fromHome != null) {
                            HomeFragment homeFragment = new HomeFragment();

                            // Use Bundle to pass values
                            Bundle bundle = new Bundle();

                            if (getBundle != null) {
                                String userID = getBundle.getString("userId");
                                bundle.putString("userId", userID);
                                homeFragment.setArguments(bundle);
                            }

                            ScrollView scrollView = getParentFragment().getView().findViewById(R.id.homeContainer);
                            scrollView.setVisibility(View.GONE);

                            BottomNavigationView bottomNavigationView = getParentFragment().getView().findViewById(R.id.bottomNavigationView);
                            bottomNavigationView.setVisibility(View.GONE);

                            FloatingActionButton floatingActionButton = getParentFragment().getView().findViewById(R.id.floatingAddButton);
                            floatingActionButton.setVisibility(View.GONE);

                            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                            transaction.replace(R.id.homeFragmentContainer, homeFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                    }

                }

                else {
                    // Handle the case where location data is not available
                    Toast.makeText(context, "Location data not available", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void refreshFragment() {
        // Perform actions to refresh the fragment content
        // For example, update UI elements, reload data, etc.
    }

    private void postRss(String userLocation, String selectedCategory, String downloadUrl) {

        final String[] pushKey = {null};
        final Boolean[] isUserExisting = {false};
        final Boolean[] isCategoryExisting = {false};
        final Boolean[] isWithinRadius = {false};

        if (isAdded()) {
            // The fragment is attached to a context, so it's safe to use requireContext().
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

        } else {
            // Handle the case where the fragment is not attached to a context.
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

                                    String numberPostsFirebase = postSnapshot.child("NumPosts").getValue(String.class);
                                    //Toast.makeText(getContext(), numberReportsFirebase, Toast.LENGTH_LONG).show();

                                    // Access the "Users" node under the specific rss
                                    DataSnapshot usersSnapshot = postSnapshot.child("Users");

                                    // Iterate through the "Users"
                                    for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
                                        String key = userSnapshot.getKey(); // Get the key ("-NhtkyoZUylTyqoeHvLQ")
                                        String value = userSnapshot.getValue(String.class); // Get the value ("5456073013")

                                        //Toast.makeText(getContext(), key, Toast.LENGTH_LONG).show();
                                        //Toast.makeText(getContext(), value, Toast.LENGTH_LONG).show();

                                        if (value.equals(userId)) {
                                            isUserExisting[0] = true;
                                            break;
                                        }
                                    }

                                    if (isUserExisting[0]) {
                                        //Toast.makeText(getContext(), "Establishment already reported.", Toast.LENGTH_LONG).show();

                                    }

                                    else { // User not yet posting an initial RSS feed

                                        // Verify if current loc is within the 0.000807 (90m) radius
                                        Double firebaseLat = postSnapshot.child("Latitude").getValue(Double.class);
                                        Double firebaseLong = postSnapshot.child("Longitude").getValue(Double.class);

                                        double radius = 0.000807; // 90 meters in degrees
                                        double lowerBound = firebaseLat - radius;
                                        double upperBound = firebaseLat + radius;
                                        double leftBound = firebaseLong - radius;
                                        double rightBound = firebaseLong + radius;

                                        if ((latitude >= lowerBound || latitude <= upperBound) && (longitude >= leftBound || longitude <= rightBound)) {
                                            //Toast.makeText(context, "WITHIN 90", Toast.LENGTH_LONG).show();

                                            isWithinRadius[0] = true;

                                            // Access the "Category" node under the specific report
                                            DataSnapshot categoriesSnapshot = postSnapshot.child("Category");

                                            // Iterate through the "Category"
                                            for (DataSnapshot catSnapshot : categoriesSnapshot.getChildren()) {
                                                String key = catSnapshot.getKey(); // Get the key ("Accommodations")
                                                //String value = catSnapshot.getValue(String.class); // Get the value ("1")

                                                if (key.equals(selectedCategory)) {
                                                    //Toast.makeText(context, "Inside " + selectedCategory + " node.", Toast.LENGTH_LONG).show();

                                                    isCategoryExisting[0] = true;

                                                    String numberPost = catSnapshot.child("Number").getValue(String.class);
                                                    // Long timestampPosted = catSnapshot.child("Timestamp").getValue(Long.class);

                                                    int intnumberPost = Integer.parseInt(numberPost);
                                                    intnumberPost++;

                                                    DatabaseReference pushKeyCatRef = databaseReference.child(pushKey[0]).child("Category").child(selectedCategory);
                                                    // Append a new child node under "Category" with the key as "selectedCategory" and the value as the incremented
                                                    pushKeyCatRef.child("Number").setValue(String.valueOf(intnumberPost));
                                                    pushKeyCatRef.child("Timestamp").setValue(ServerValue.TIMESTAMP);

                                                    // Convert numReports to an integer, update it, and set it back
                                                    int intNumPosts = Integer.parseInt(numberPostsFirebase);
                                                    intNumPosts++;
                                                    postSnapshot.getRef().child("NumPosts").setValue(String.valueOf(intNumPosts));

                                                    DatabaseReference pushKeyRef = databaseReference.child(pushKey[0]).child("Users");
                                                    pushKeyRef.push().setValue(userId);

                                                    if (intNumPosts == 20) {

                                                        //reachThresholdPostLocation(firebaseLat, firebaseLong, categoriesSnapshot, downloadUrl, userLocation);
                                                        //Toast.makeText(context, "ADD RSS FEED TO LOCATION", Toast.LENGTH_LONG).show();

                                                        // Register the RSS Feed to Location table from Firebase
                                                        DatabaseReference newLocation = FirebaseDatabase.getInstance().getReference("Location");
                                                        //DatabaseReference newLocation = FirebaseDatabase.getInstance().getReference("Location");

                                                        // Save the data to the Firebase Realtime Database
                                                        DatabaseReference rssPostNewLoc = newLocation.push();

                                                        // Identify Category with highest posts
                                                        String highestCategory = null;
                                                        int previousValue = 0;
                                                        long previousTimestamp = 0;
                                                        // Iterate through the "Category"
                                                        for (DataSnapshot categorySnapshot : categoriesSnapshot.getChildren()) {
                                                            String catKey = categorySnapshot.getKey(); // Get the key ("Accommodations")

                                                            String catNumberPost = categorySnapshot.child("Number").getValue(String.class);
                                                            Long catTimestampPosted = categorySnapshot.child("Timestamp").getValue(Long.class);

                                                            if (Integer.parseInt(catNumberPost) > previousValue) {
                                                                previousValue = Integer.parseInt(catNumberPost);
                                                                highestCategory = key;
                                                                previousTimestamp = catTimestampPosted;
                                                            }

                                                            else if (Integer.parseInt(catNumberPost) == previousValue) {
                                                                if (previousTimestamp > catTimestampPosted) { // previousTimestamp more recent.
                                                                    previousValue = Integer.parseInt(catNumberPost);
                                                                    highestCategory = key;
                                                                    previousTimestamp = catTimestampPosted;
                                                                }

                                                                else {

                                                                }
                                                            }
                                                        }

                                                        reachThresholdPostLocation(firebaseLat, firebaseLong, rssPostNewLoc, downloadUrl, userLocation, highestCategory);

                                                        break;

                                                    }

                                                    //Toast.makeText(context, selectedCategory + ": " + String.valueOf(timestampPosted), Toast.LENGTH_LONG).show();

                                                }
                                            }

                                            if (isCategoryExisting[0]) {

                                            }

                                            else {

                                                DatabaseReference pushKeyCatRef = databaseReference.child(pushKey[0]).child("Category");
                                                pushKeyCatRef.child(selectedCategory).child("Timestamp").setValue(ServerValue.TIMESTAMP);
                                                pushKeyCatRef.child(selectedCategory).child("Number").setValue("1");

                                                // Convert numReports to an integer, update it, and set it back
                                                int intNumPosts = Integer.parseInt(numberPostsFirebase);
                                                intNumPosts++;
                                                postSnapshot.getRef().child("NumPosts").setValue(String.valueOf(intNumPosts));

                                                DatabaseReference pushKeyRef = databaseReference.child(pushKey[0]).child("Users");

                                                // Append a new child node under "Users" with the key as "UserId" and the value as the user ID
                                                pushKeyRef.push().setValue(userId);

                                                if (intNumPosts == 20) {
                                                    //reachThresholdPostLocation(firebaseLat, firebaseLong, categoriesSnapshot, downloadUrl, userLocation);
                                                    //Toast.makeText(context, "ADD RSS FEED TO LOCATION", Toast.LENGTH_LONG).show();

                                                    // Register the RSS Feed to Location table from Firebase
                                                    DatabaseReference newLocation = FirebaseDatabase.getInstance().getReference("Location");
                                                    //DatabaseReference newLocation = FirebaseDatabase.getInstance().getReference("Location");

                                                    // Save the data to the Firebase Realtime Database
                                                    DatabaseReference rssPostNewLoc = newLocation.push();

                                                    // Identify Category with highest posts
                                                    String highestCategory = null;
                                                    int previousValue = 0;
                                                    long previousTimestamp = 0;
                                                    // Iterate through the "Category"
                                                    for (DataSnapshot catSnapshot : categoriesSnapshot.getChildren()) {
                                                        String key = catSnapshot.getKey(); // Get the key ("Accommodations")

                                                        String numberPost = catSnapshot.child("Number").getValue(String.class);
                                                        Long timestampPosted = catSnapshot.child("Timestamp").getValue(Long.class);

                                                        if (Integer.parseInt(numberPost) > previousValue) {
                                                            previousValue = Integer.parseInt(numberPost);
                                                            highestCategory = key;
                                                            previousTimestamp = timestampPosted;
                                                        }

                                                        else if (Integer.parseInt(numberPost) == previousValue) {
                                                            if (previousTimestamp > timestampPosted) { // previousTimestamp more recent.

                                                            }

                                                            else {
                                                                previousValue = Integer.parseInt(numberPost);
                                                                highestCategory = key;
                                                                previousTimestamp = timestampPosted;
                                                            }
                                                        }
                                                    }

                                                    reachThresholdPostLocation(firebaseLat, firebaseLong, rssPostNewLoc, downloadUrl, userLocation, highestCategory);

                                                    break;
                                                }
                                            }

                                        } else {
                                            //Toast.makeText(context, "OUTSIDE", Toast.LENGTH_LONG).show();

                                        }

                                    }
                                }

                                // end of for loop here

                            }

                            // Add report on existing rss
                            else {
                                // new record of RSS

                                // Save the data to the Firebase Realtime Database
                                DatabaseReference rssPost = databaseReference.push();

                                rssPost.child("EstablishmentName").setValue(userLocation);
                                //rssPost.child("userId").setValue(userId);
                                rssPost.child("NumPosts").setValue("1");
                                rssPost.child("Users").push().setValue(userId);
                                rssPost.child("Category").child(selectedCategory).child("Timestamp").setValue(ServerValue.TIMESTAMP);
                                rssPost.child("Category").child(selectedCategory).child("Number").setValue("1");
                                rssPost.child("Latitude").setValue(latitude);
                                rssPost.child("Longitude").setValue(longitude);

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

    public void reachThresholdPostLocation(Double firebaseLat, Double firebaseLong, DatabaseReference rssPostNewLoc, String downloadUrl, String userLocation, String highestCategory) {

        Geocoder geocoder = new Geocoder(context);

        if (isAdded()) {
            // The fragment is attached to a context, so it's safe to use requireContext().
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

        }

        PlacesClient placesClient = Places.createClient(context);  // context   requireContext()

        // Define the fields you want to retrieve
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID);

        // Define the fields you want to retrieve (e.g., contact number and description)
        List<Place.Field> placeFieldsMore = Arrays.asList(Place.Field.PHONE_NUMBER, Place.Field.TYPES);

        // Create a FindCurrentPlaceRequest for a place search near the specified location
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        // Use the request to find place details at the specified location
        placesClient.findCurrentPlace(request).addOnSuccessListener((response) -> {
            FindCurrentPlaceResponse findCurrentPlaceResponse = response;

            // Retrieve place details including the placeId
            List<PlaceLikelihood> placeLikelihoods = findCurrentPlaceResponse.getPlaceLikelihoods();
            if (!placeLikelihoods.isEmpty()) {
                Place place = placeLikelihoods.get(0).getPlace();
                String placeId = place.getId();
                // Use the placeId as needed

                // Create a FetchPlaceRequest with the Place ID and desired fields
                FetchPlaceRequest requestDetails = FetchPlaceRequest.builder(placeId, placeFieldsMore).build();

                // Use the Places API client to fetch the details
                placesClient.fetchPlace(requestDetails).addOnSuccessListener((responseDetails) -> {
                    Place placeDetails = responseDetails.getPlace();
                    String phoneNumber = placeDetails.getPhoneNumber();
                    //String description = placeDetails.getName(); // This is just an example; you can retrieve a relevant description field

                    //Toast.makeText(context, phoneNumber, Toast.LENGTH_LONG).show();
                    //Toast.makeText(context, description, Toast.LENGTH_LONG).show();

                    try {
                        List<android.location.Address> addresses = geocoder.getFromLocation(firebaseLat, firebaseLong, 1);

                        if (addresses != null && addresses.size() > 0) {
                            Address address = addresses.get(0);

                            // You can now extract address components
                            String completeAddress = address.getAddressLine(0); // Full street address
                            String city = address.getLocality();
                            String state = address.getAdminArea();
                            String postalCode = address.getPostalCode();
                            String country = address.getCountryName();

                            rssPostNewLoc.child("Address").setValue(completeAddress);
                            rssPostNewLoc.child("AverageRate").setValue("0");
                            rssPostNewLoc.child("Category").setValue(highestCategory);
                            rssPostNewLoc.child("ContactNo").setValue(phoneNumber);
                            rssPostNewLoc.child("CreatedBy").setValue("RSS");
                            rssPostNewLoc.child("Description").setValue("");
                            rssPostNewLoc.child("HighestPrice").setValue("");
                            rssPostNewLoc.child("Image").setValue(filename);
                            rssPostNewLoc.child("ImageDescription").setValue(""); // NULL
                            rssPostNewLoc.child("Latitude").setValue(String.valueOf(firebaseLat));
                            rssPostNewLoc.child("Link").setValue(downloadUrl);
                            rssPostNewLoc.child("Location").setValue(userLocation);
                            rssPostNewLoc.child("Longitude").setValue(String.valueOf(firebaseLong));
                            rssPostNewLoc.child("LowestPrice").setValue("");

                        } else {
                            // Geocoder couldn't find an address for the given latitude and longitude
                        }
                    } catch (IOException e) {
                        // Handle geocoding errors (e.g., network issues, service not available)
                        throw new RuntimeException(e);
                    }

                    // Do something with the retrieved information
                }).addOnFailureListener((exception) -> {
                    // Handle any errors
                });
            }
        }).addOnFailureListener((exception) -> {
            // Handle any errors that occurred during the request
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
