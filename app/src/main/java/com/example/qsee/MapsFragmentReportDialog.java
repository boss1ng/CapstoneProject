package com.example.qsee;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MapsFragmentReportDialog extends DialogFragment {

    public Boolean isNotRSS = false;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create a new Dialog instance
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // Set a custom layout for the dialog
        dialog.setContentView(R.layout.fragment_maps_report_dialog);

        // Customize the width of the dialog (75% of screen width)
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 1);
        dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

        return dialog;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the custom layout for this dialog fragment
        View view = inflater.inflate(R.layout.fragment_maps_report_dialog, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Retrieve place details from arguments
        Bundle getBundle = getArguments();

        if (getBundle != null) {
            String userID = getBundle.getString("userId");
            String placeName = getBundle.getString("placeName");
            //Toast.makeText(getContext(), userID, Toast.LENGTH_LONG).show();
            //Toast.makeText(getContext(), placeName, Toast.LENGTH_LONG).show();
        }

        /*
        // Set the custom background to the root view
        if (view != null) {
            view.setBackgroundResource(R.drawable.dialog_background);
        }
         */

        TextView textView = view.findViewById(R.id.textViewReport);
        textView.setText("Report an issue?");

        Button buttonClosed = view.findViewById(R.id.btnClosed);
        Button buttonNonExist = view.findViewById(R.id.btnNoneExisting);

        buttonClosed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                buttonClosed.setEnabled(false);
                buttonNonExist.setEnabled(false);
                reportEstablishment();

                // Use a Handler to refresh the map every second
                Handler handler = new Handler();
                Runnable mapRefreshRunnable = new Runnable() {
                    @Override
                    public void run() {
                        dismiss(); // Dismiss the dialog
                    }
                };
                handler.postDelayed(mapRefreshRunnable, 2000); // Schedule it to run after 1 second
            }
        });

        buttonNonExist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                buttonClosed.setEnabled(false);
                buttonNonExist.setEnabled(false);
                reportEstablishment();

                // Use a Handler to refresh the map every second
                Handler handler = new Handler();
                Runnable mapRefreshRunnable = new Runnable() {
                    @Override
                    public void run() {
                        dismiss(); // Dismiss the dialog
                    }
                };
                handler.postDelayed(mapRefreshRunnable, 2000); // Schedule it to run after 1 second
            }
        });

        return view;
    }

    public void reportEstablishment() {

        final String[] pushKey = {null};
        final Boolean[] isEstablishmentExisting = {false};
        final Boolean[] isUserExisting = {false};

        // Retrieve place details from arguments
        Bundle getBundle = getArguments();

        if (getBundle != null) {
            String userID = getBundle.getString("userId");
            String placeName = getBundle.getString("placeName");

            // Initialize the Firebase Database reference
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Report");

            // Create a query to check if EstablishmentName matches the target name
            Query query = databaseReference.orderByChild("EstablishmentName").equalTo(placeName);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        // The placeName already exists in the database
                        // You may want to display an error message or take some other action

                        //isEstablishmentExisting[0] = true;

                        for (DataSnapshot reportSnapshot : dataSnapshot.getChildren()) {
                            // Here, reportSnapshot points to a specific report that matches the establishment name

                            pushKey[0] = reportSnapshot.getKey();

                            String numberReportsFirebase = reportSnapshot.child("NumReports").getValue(String.class);
                            //Toast.makeText(getContext(), numberReportsFirebase, Toast.LENGTH_LONG).show();

                            // Access the "Users" node under the specific report
                            DataSnapshot usersSnapshot = reportSnapshot.child("Users");

                            // Iterate through the "Users" under this report
                            for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
                                String key = userSnapshot.getKey(); // Get the key ("-NhtkyoZUylTyqoeHvLQ")
                                String value = userSnapshot.getValue(String.class); // Get the value ("5456073013")

                                //Toast.makeText(getContext(), key, Toast.LENGTH_LONG).show();
                                //Toast.makeText(getContext(), value, Toast.LENGTH_LONG).show();

                                if (value.equals(userID)) {
                                    isUserExisting[0] = true;
                                }
                            }

                            if (isUserExisting[0]) {
                                Toast.makeText(getContext(), "Establishment already reported.", Toast.LENGTH_LONG).show();
                            }

                            else {
                                // Convert numReports to an integer, update it, and set it back
                                int intNumReports = Integer.parseInt(numberReportsFirebase);
                                intNumReports++;
                                reportSnapshot.getRef().child("NumReports").setValue(String.valueOf(intNumReports));

                                DatabaseReference pushKeyRef = databaseReference.child(pushKey[0]).child("Users");

                                // Append a new child node under "Users" with the key as "UserId" and the value as the user ID
                                pushKeyRef.push().setValue(userID);

                                if (intNumReports == 20) {

                                    DatabaseReference locationReference = FirebaseDatabase.getInstance().getReference().child("Location");
                                    locationReference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot placeSnapshot : snapshot.getChildren()) {
                                                // Extract place data (e.g., latitude, longitude, name) from placeSnapshot
                                                String name = placeSnapshot.child("Location").getValue(String.class);
                                                //String filename = placeSnapshot.child("Image").getValue(String.class);
                                                String createdBy = placeSnapshot.child("CreatedBy").getValue(String.class);

                                                if (name.equals(placeName)) {

                                                    if (createdBy.equals("Administrator")) {
                                                        isNotRSS = true;
                                                    }

                                                    else {
                                                        isNotRSS = false;
                                                        // Delete Location record from Firebase Realtime Database
                                                        placeSnapshot.getRef().removeValue();

                                                        //Toast.makeText(getContext(), filename, Toast.LENGTH_LONG).show();

                                                        //FirebaseApp.initializeApp(getContext());

                                                        // Initialize Firebase Storage and Get a non-default Storage bucket
                                                        //FirebaseStorage storage = FirebaseStorage.getInstance("gs://capstone-project-ffe21.appspot.com");

                                                        // Create a storage reference from our app
                                                        //StorageReference storageRef = storage.getReference();

                                                        // Create a reference to the file to delete
                                                        //StorageReference fileReference = storageRef.child("Location/" + filename);
                                                        // Delete image from Firebase Cloud Storage
                                                        /*
                                                        fileReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                // File deleted successfully
                                                                // You can add your logic here
                                                                //Toast.makeText(getContext(), "Successfully deleted.", Toast.LENGTH_LONG).show();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                // An error occurred while deleting the file
                                                                // Handle the error here
                                                            }
                                                        });
                                                         */
                                                    }

                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isNotRSS)
                                            Toast.makeText(getContext(), "Establishment Reported to the Administrator.", Toast.LENGTH_LONG).show();
                                        else
                                            Toast.makeText(getContext(), "Reported Successfully.", Toast.LENGTH_LONG).show();
                                    }
                                }, 500); // 1000 milliseconds = 1 second

                            }
                        }
                    }

                    else {
                        // The userId doesn't exist in the database
                        // You can proceed to check if placeName exists
                        reportNewLocation(userID, placeName);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle any errors that may occur during the database query
                }
            });





            /*
            // For Reading the Database
            // Initialize Firebase Database reference
            // Reference to the "Location" node in Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Report");

            // Add markers for places retrieved from Firebase
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot placeSnapshot : snapshot.getChildren()) {

                        // Extract database data
                        String establishmentNameFirebase = placeSnapshot.child("EstablishmentName").getValue(String.class);
                        String numberReportsFirebase = placeSnapshot.child("NumReports").getValue(String.class);
                        //String userIdFirebase = placeSnapshot.child("UserId").getValue(String.class);

                        if (establishmentNameFirebase.equals(placeName)) {
                            //Toast.makeText(getContext(), "SAME", Toast.LENGTH_LONG).show();
                            isEstablishmentExisting[0] = true;
                            pushKey[0] = placeSnapshot.getKey();



                            DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("Users");
                            usersReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                        String key = userSnapshot.getKey(); // Get the key ("-NhtguWSv9lYhLe41ufh", "-Nhth0cET4hUsbSrxh0V")
                                        String value = userSnapshot.getValue(String.class); // Get the value ("5456073013", "9225443216")

                                        Toast.makeText(getContext(), key, Toast.LENGTH_LONG).show();
                                        Toast.makeText(getContext(), value, Toast.LENGTH_LONG).show();

                                        // Now, you can use the key and value as needed
                                        // ...
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            /
                            if (userIdFirebase.equals(userID)) {
                                int intReports = Integer.parseInt(numberReportsFirebase);
                                ++intReports;

                                Toast.makeText(getContext(), String.valueOf(intReports), Toast.LENGTH_LONG).show();

                                //placeSnapshot.child("NumReports").getRef().setValue(String.valueOf(intReports));
                            }

                            else {
                                //Toast.makeText(getContext(), "NOT SAME", Toast.LENGTH_LONG).show();
                                int intReports = Integer.parseInt(numberReportsFirebase);
                                intReports++;
                                placeSnapshot.getRef().child("NumReports").setValue(String.valueOf(intReports));

                                Toast.makeText(getContext(), "Reported Successfully.", Toast.LENGTH_LONG).show();
                                break;

                                //Toast.makeText(getContext(), String.valueOf(intReports), Toast.LENGTH_LONG).show();
                                //placeSnapshot.child("NumReports").getRef().setValue(String.valueOf(intReports));
                            }
                             *

                        }

                        else
                            Toast.makeText(getContext(), "NOT SAME", Toast.LENGTH_LONG).show();

                    }

                    if (!(isEstablishmentExisting[0])) {
                        reportNewLocation(userID, placeName);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

             */







             /*

            DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference().child("Report");

            // Add markers for places retrieved from Firebase
            dbReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                        // Extract place data (e.g., latitude, longitude, name) from placeSnapshot
                        String establishmentNameFirebase = placeSnapshot.child("EstablishmentName").getValue(String.class);
                        String numberReportsFirebase = placeSnapshot.child("NumReports").getValue(String.class);
                        String userIdFirebase = placeSnapshot.child("UserId").getValue(String.class);

                        if (establishmentNameFirebase.equals(placeName)) {
                            pushKey[0] = placeSnapshot.getKey();
                            //Toast.makeText(getContext(), pushKey[0], Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle any errors that may occur during the database query
                }
            });

            // Get a reference to the Firebase Realtime Database
            DatabaseReference databaseReference;

            // Initialize the Firebase Database reference
            databaseReference = FirebaseDatabase.getInstance().getReference("Report");

            // Create a query to check if placeName already exists
            Query query = databaseReference.orderByChild("EstablishmentName").equalTo(placeName);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        // The placeName already exists in the database
                        // You may want to display an error message or take some other action

                        // Now, check if userId exists in the EstablishmentName and update NumReports
                        Query userIdRef = dataSnapshot.getRef().equalTo(userID);
                        userIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot userIdDataSnapshot) {
                                if (userIdDataSnapshot.exists()) {
                                    // userId exists
                                    Toast.makeText(getContext(), "Establishment already reported.", Toast.LENGTH_LONG).show();
                                }

                                else {
                                    for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                                        String numReports = placeSnapshot.child("NumReports").getValue(String.class);
                                        if (numReports != null) {
                                            // Convert numReports to an integer, update it, and set it back
                                            int intNumReports = Integer.parseInt(numReports);
                                            intNumReports++;
                                            placeSnapshot.getRef().child("NumReports").setValue(String.valueOf(intNumReports));

                                            DatabaseReference pushKeyRef = databaseReference.child(pushKey[0]).child("Users");

                                            // Append a new child node under "Users" with the key as "UserId" and the value as the user ID
                                            pushKeyRef.push().setValue(userID);



                                                //databaseReference.child("Users").child("UserId").setValue(userID);

                                            Toast.makeText(getContext(), "Reported Successfully.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Handle any errors that may occur during the database query
                            }
                        });

                    } else {
                        // The userId doesn't exist in the database
                        // You can proceed to check if placeName exists
                        reportNewLocation(userID, placeName);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle any errors that may occur during the database query
                }
            });
              */


        }

        else {
            //Toast.makeText(getContext(), "No getBundle", Toast.LENGTH_LONG).show();
        }
    }

    // Report a new location
    private void reportNewLocation(String userId, String placeName) {
        // Get a reference to the Firebase Realtime Database
        DatabaseReference databaseReference;

        // Initialize the Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Report");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Save the data to the Firebase Realtime Database
                DatabaseReference newPostRef = databaseReference.push();

                /*
                // Create a mew node for all the userId that will report
                DatabaseReference userNodeRef = databaseReference.child("Report").child("UserId").push();

                userNodeRef.child("UserId").setValue(userId);
                 */

                //newPostRef.child("UserId").setValue(userId);
                newPostRef.child("EstablishmentName").setValue(placeName);
                newPostRef.child("NumReports").setValue("1");

                //newPostRef.child("Users").child("UserId").setValue(userId);
                newPostRef.child("Users").push().setValue(userId);

                Toast.makeText(getContext(), "Reported Successfully.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that may occur during the database query
            }
        });
    }

}