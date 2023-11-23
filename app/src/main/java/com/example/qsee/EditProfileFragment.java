package com.example.qsee;

import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static com.example.qsee.AddGlimpseFragment.CAMERA_REQUEST_CODE;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.Manifest;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {
    private static final int CAMERA_REQUEST_CODE = 100;
    private ImageView profilePictureImageView;
    private TextInputLayout firstNameEditText;
    private DatePickerDialog datePickerDialog;
    private TextInputLayout lastNameEditText;
    private TextInputLayout contactNoInputLayout;
    private TextInputLayout birthdateEditText;
    private TextView usernameEditText;
    private LinearLayout linearLayout;
    private DatabaseReference userReference;
    private Uri selectedProfilePictureUri = null;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Context context;
    private String userId;
    private Uri imageUri; // Declare this as a member variable

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int TARGET_SIZE = 2;
    private static final int TARGET_WIDTH_DP = 400; // Target width in dp
    private static final int TARGET_HEIGHT_DP = 500; // Target height in dp
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);

        // Load the background image using Picasso
        String imageUrl = "https://firebasestorage.googleapis.com/v0/b/capstone-project-ffe21.appspot.com/o/profbg.jpg?alt=media&token=4b33d94a-47f5-48f3-90f4-c768b7f0480f";
        ImageView backgroundImageView = new ImageView(getActivity());

        // Set a listener to be notified when the image is loaded
        Picasso.get().load(imageUrl).into(backgroundImageView, new Callback() {
            @Override
            public void onSuccess() {
                // Set the background of the profileCont LinearLayout
                LinearLayout linearLayout = view.findViewById(R.id.editprofcont);
                linearLayout.setBackground(backgroundImageView.getDrawable());
            }

            @Override
            public void onError(Exception e) {
                // Handle error if necessary
            }
        });
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        context = getActivity();
        context = requireContext();
        userId = getArguments().getString("userId");
        String uniqueId = String.valueOf(System.currentTimeMillis());
        String filename = userId + "_" + uniqueId + ".png";

        storage = FirebaseStorage.getInstance("gs://capstone-project-ffe21.appspot.com");
        storageReference = storage.getReference().child("Profile/" + filename);
        profilePictureImageView = view.findViewById(R.id.profilePic_edit);
        firstNameEditText = view.findViewById(R.id.firstName);
        lastNameEditText = view.findViewById(R.id.lastName);
        contactNoInputLayout = view.findViewById(R.id.contactNo);
        birthdateEditText = view.findViewById(R.id.birthdate);
        usernameEditText = view.findViewById(R.id.username);

        ImageView changePictureButton = view.findViewById(R.id.pfpBtn);


        changePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageSourceDialog();
            }
        });

        EditText contactNumberEditText = contactNoInputLayout.getEditText();

        if (contactNumberEditText != null) {
            InputFilter[] filters = new InputFilter[1];
            filters[0] = new InputFilter.LengthFilter(9);
            contactNumberEditText.setFilters(filters);
        }

        userReference = FirebaseDatabase.getInstance().getReference("MobileUsers");
        Query query = userReference.orderByChild("userId").equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String encryptedFirstName = userSnapshot.child("firstName").getValue(String.class);
                        String encryptedLastName = userSnapshot.child("lastName").getValue(String.class);
                        String encryptedContactNumber = userSnapshot.child("contactNumber").getValue(String.class);
                        String encryptedBirthdate = userSnapshot.child("birthdate").getValue(String.class);
                        String encryptedUsername = userSnapshot.child("username").getValue(String.class);
                        String profilePic = userSnapshot.child("profilePictureUrl").getValue(String.class);

                        String firstName = AESUtils.decrypt(encryptedFirstName);
                        String lastName = AESUtils.decrypt(encryptedLastName);
                        String contactNumber = AESUtils.decrypt(encryptedContactNumber);
                        contactNumber = contactNumber.substring(2);

                        String birthdate = AESUtils.decrypt(encryptedBirthdate);
                        String username = AESUtils.decrypt(encryptedUsername);

                        firstNameEditText.getEditText().setText(firstName);
                        lastNameEditText.getEditText().setText(lastName);
                        contactNoInputLayout.getEditText().setText(contactNumber);
                        birthdateEditText.getEditText().setText(birthdate);
                        usernameEditText.setText(username);
                        bindData(profilePic);
                    }
                } else {
                    Log.e("EditProfileFragment", "User with username not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any database query errors here
            }
        });

        EditText dateEditText = birthdateEditText.getEditText();

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                datePickerDialog = new DatePickerDialog(requireContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                String formattedDate = (selectedMonth + 1) + "/" + selectedDay + "/" + selectedYear;
                                dateEditText.setText(formattedDate);
                            }
                        }, year, month, day);

                datePickerDialog.show();
            }
        });

        Button saveButton = view.findViewById(R.id.saveBt);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newFirstName = firstNameEditText.getEditText().getText().toString();
                String newLastName = lastNameEditText.getEditText().getText().toString();
                String newContactNumber = contactNoInputLayout.getEditText().getText().toString();
                String modifiedContactNo = "09" + newContactNumber;

                String newBirthdate = birthdateEditText.getEditText().getText().toString();

                String encryptedFirstName = AESUtils.encrypt(newFirstName);
                String encryptedLastName = AESUtils.encrypt(newLastName);
                //String encryptedContactNumber = AESUtils.encrypt(newContactNumber);
                String encryptedContactNumber = AESUtils.encrypt(modifiedContactNo);
                String encryptedBirthdate = AESUtils.encrypt(newBirthdate);

                DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("MobileUsers");
                Query query = usersReference.orderByChild("userId").equalTo(userId);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                userSnapshot.getRef().child("firstName").setValue(encryptedFirstName);
                                userSnapshot.getRef().child("lastName").setValue(encryptedLastName);
                                userSnapshot.getRef().child("contactNumber").setValue(encryptedContactNumber);
                                userSnapshot.getRef().child("birthdate").setValue(encryptedBirthdate);

                                // Only upload the profile picture if selectedProfilePictureUri is not null
                                if (selectedProfilePictureUri != null) {
                                    uploadProfilePicture(userId, selectedProfilePictureUri);
                                } else {
                                    Toast.makeText(getContext(), "Profile information updated successfully.", Toast.LENGTH_LONG).show();
                                    getParentFragmentManager().popBackStack();
                                }
                            }
                        } else {
                            Log.e("EditProfileFragment", "User with username not found.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("EditProfileFragment", "Database Error: " + databaseError.getMessage());
                    }
                });
            }
        });


        Button cancel = view.findViewById(R.id.cancelBt);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        return view;
    }

    public void bindData(String url) {
        Picasso.get()
                .load(url)
                .placeholder(R.drawable.profilepicture) // Placeholder image
                .error(R.drawable.ic_profile) // Error image
                .into(profilePictureImageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        // Successful loading
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("PicassoError", "Load failed", e);
                    }
                });
    }



    public static EditProfileFragment newInstance(String userId) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                if (imageUri != null) {
                // The image URI will be contained in the imageUri variable
                startCrop(imageUri);
                } else {
                    // Handle null Uri case here
                    Log.e("EditProfileFragment", "Image Uri is null in onActivityResult.");
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                Uri imageUri = data.getData();
                startCrop(imageUri);
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                Uri photoUri = getImageUri(getContext(), photo);
                startCrop(photoUri);
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri resultUri = result.getUri();
                profilePictureImageView.setImageURI(resultUri);
                selectedProfilePictureUri = resultUri;
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            Exception error = CropImage.getActivityResult(data).getError();
            // Handle error, show a message to the user
        }
    }


    private void startCrop(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1) // You can specify aspect ratio
                .start(getContext(), this);
    }




    // Get the orientation of the image from its Exif data

    private int getOrientation(Context context, Uri photoUri) {
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

        if (cursor == null || cursor.getCount() != 1) {
            return 0; // Default orientation
        }

        cursor.moveToFirst();
        int orientation = cursor.getInt(0);
        cursor.close();
        return orientation;
    }


    // Rotate the bitmap based on the given orientation value
    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        matrix.setRotate(orientation);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Bitmap scaleImageToTargetDimensions(Bitmap image, int targetWidthDp, int targetHeightDp, Uri imageUri) {
        if (context == null) {
            Log.e("EditProfileFragment", "Context is null. Cannot scale image.");
            return image;
        }
        if (getActivity() == null) {
            Log.e("EditProfileFragment", "Activity is null. Cannot scale image.");
            return image;
        }

        // Convert dp to pixels
        int targetWidth = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, targetWidthDp, getResources().getDisplayMetrics());
        int targetHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, targetHeightDp, getResources().getDisplayMetrics());

        // Handle landscape mode by checking the orientation from Exif data
        int orientation = getOrientation(getActivity(), imageUri);
        if (orientation == 90 || orientation == 270) {
            // Swap target dimensions for landscape orientation
            int temp = targetWidth;
            targetWidth = targetHeight;
            targetHeight = temp;
        }

        return Bitmap.createScaledBitmap(image, targetWidth, targetHeight, true);
    }



    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose Image Source");

        String[] options = {"Take a Picture", "Upload from Gallery"};

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    openCamera();
                } else if (which == 1) {
                    openGallery();
                }
            }
        });

        builder.create().show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the camera permission
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Camera permission is already granted, open the camera
            //Log.d(TAG, "GUMANA YUNG CAMERA HAHAHA!");
            launchCameraIntent();
        }
    }
    private void launchCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getContext().getPackageManager()) != null) {
            // Create a file to save the image
            imageUri = createImageUri(); // Make sure this method returns a valid Uri
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(getContext(), "No camera app found", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the camera
                launchCameraIntent();
            } else {
                // Permission denied
                Toast.makeText(getContext(), "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Uri createImageUri() {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "Image_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void uploadProfilePicture(String userId, Uri imageUri) {
        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("MobileUsers");
        Query query = usersReference.orderByChild("userId").equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String currentImageUrl = userSnapshot.child("profilePictureUrl").getValue(String.class);

                        // Delete the current image from Firebase Storage
                        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                            StorageReference oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentImageUrl);
                            oldImageRef.delete().addOnSuccessListener(aVoid -> {
                                Log.d("EditProfileFragment", "Old profile image deleted successfully.");
                            }).addOnFailureListener(exception -> {
                                Log.e("EditProfileFragment", "Error deleting old profile image: " + exception.getMessage());
                            });
                        }

                        // Upload new image
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                            int orientation = getOrientation(context, imageUri);
                            Bitmap rotatedBitmap = rotateBitmap(bitmap, orientation);

                            Uri rotatedBitmapUri = getImageUri(context, rotatedBitmap);
                            if (rotatedBitmapUri != null) {
                                StorageReference profilePictureRef = storageReference.child(userId + ".jpg");
                                UploadTask uploadTask = profilePictureRef.putFile(getImageUri(context, rotatedBitmap));
                                uploadTask.addOnSuccessListener(taskSnapshot -> {
                                    profilePictureRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                        String imageUrl = uri.toString();

                                        // Update profile picture URL in "MobileUsers" node
                                        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("MobileUsers");
                                        Query query = usersReference.orderByChild("userId").equalTo(userId);

                                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                                        userSnapshot.getRef().child("profilePictureUrl").setValue(imageUrl);
                                                        Toast.makeText(getContext(), "Profile updated successfully.", Toast.LENGTH_LONG).show();
                                                        getParentFragmentManager().popBackStack();

                                                        // Update profile picture URL in "Posts" node
                                                        DatabaseReference postsReference = FirebaseDatabase.getInstance().getReference("Posts");
                                                        Query postsQuery = postsReference.orderByChild("userId").equalTo(userId);

                                                        postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot postsDataSnapshot) {
                                                                if (postsDataSnapshot.exists()) {
                                                                    for (DataSnapshot postSnapshot : postsDataSnapshot.getChildren()) {
                                                                        postSnapshot.getRef().child("profilePictureUrl").setValue(imageUrl);
                                                                    }
                                                                } else {
                                                                    Log.e("EditProfileFragment", "No posts found for the user.");
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                Log.e("EditProfileFragment", "Database Error: " + databaseError.getMessage());
                                                            }
                                                        });
                                                    }
                                                } else {
                                                    Log.e("EditProfileFragment", "User with username not found.");
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Log.e("EditProfileFragment", "Database Error: " + databaseError.getMessage());
                                            }
                                        });
                                    });
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("EditProfileFragment", "Error uploading profile picture: " + e.getMessage());
                        }

                    }
                } else {
                    Log.e("EditProfileFragment", "User with username not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("EditProfileFragment", "Database Error: " + databaseError.getMessage());
            }
        });
    }

    // Defining the getImageUri
    private Uri getImageUri(Context context, Bitmap bitmap) {
        if (bitmap == null) {
            Log.e("EditProfileFragment", "Bitmap is null. Cannot create Uri.");
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        Uri uri = createImageUri();
        if (uri != null) {
            try {
                OutputStream os = context.getContentResolver().openOutputStream(uri);
                os.write(imageData);
                os.flush();
                os.close();
                return uri;
            } catch (Exception e) {
                Log.e("EditProfileFragment", "Error writing to output stream: " + e.getMessage());
            }
        } else {
            Log.e("EditProfileFragment", "Failed to create Uri for image.");
        }

        return null;
    }

}
