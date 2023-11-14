package com.example.qsee;

import static android.service.controls.ControlsProviderService.TAG;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class EditActivityFragment extends Fragment {
    private Switch completionSwitch;
    private DatabaseReference daySnapshot; // Declare DatabaseReference for daySnapshot
    private String militaryTime; // Declare String variable militaryTime
    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_editactivity, container, false);

        completionSwitch = rootView.findViewById(R.id.completionSwitch);
        // Retrieve the arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            String time = bundle.getString("time");
            String location = bundle.getString("location");
            String iterName = bundle.getString("iterName");


            TextView EditName = rootView.findViewById(R.id.editAct);
            EditName.setText("Edit " + location);

            // Find the TextInputLayout for location and set the text
            TextInputLayout locationTextInputLayout = rootView.findViewById(R.id.locName);
            TextInputLayout originTextInputLayout = rootView.findViewById(R.id.originName);
            AutoCompleteTextView originAutoCompleteTextView = (AutoCompleteTextView) Objects.requireNonNull(originTextInputLayout.getEditText());
            AutoCompleteTextView locationAutoCompleteTextView = (AutoCompleteTextView) Objects.requireNonNull(locationTextInputLayout.getEditText()); // Get the AutoCompleteTextView
            locationAutoCompleteTextView.setText(location);

            // Fetch data from Firebase Realtime Database
            DatabaseReference locationsRef = FirebaseDatabase.getInstance().getReference().child("Location");
            locationsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<String> locationsList = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String Location = snapshot.child("Location").getValue(String.class);
                        if (Location != null) {
                            locationsList.add(Location);
                        }
                    }

                    // Check if the fragment is attached to a context before using requireContext()
                    if (isAdded() && getContext() != null) {
                        // Set up the adapter for the AutoCompleteTextView
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, locationsList);
                        originAutoCompleteTextView.setAdapter(adapter);
                        locationAutoCompleteTextView.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle any errors
                }
            });

            TextInputLayout timeTextInputLayout = rootView.findViewById(R.id.locTime);
            timeTextInputLayout.getEditText().setText(time);

            // Convert the retrieved time to military time format
            try {
                SimpleDateFormat givenFormat = new SimpleDateFormat("hh:mm a", Locale.US);
                SimpleDateFormat militaryFormat = new SimpleDateFormat("HH:mm", Locale.US);

                assert time != null;
                Date date = givenFormat.parse(time);
                assert date != null;
                time = militaryFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            timeTextInputLayout.getEditText().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTimePicker(timeTextInputLayout);
                }
            });

            // Use the retrieved time and location to query the specific activity
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Itinerary");
            assert iterName != null;
            Query activityQuery = databaseReference.child(iterName);
            String finalTime = time;
            activityQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot daySnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot timeSnapshot : daySnapshot.getChildren()) {
                            if (timeSnapshot.getKey().equals(finalTime)) {
                                String retrievedLoc = timeSnapshot.child("location").getValue(String.class);
                                if (retrievedLoc != null && retrievedLoc.equals(location)) {
                                    // Retrieve the activity data
                                    String activity = timeSnapshot.child("activity").getValue(String.class);
                                    String origin = timeSnapshot.child("origin").getValue(String.class);

                                    // Update the TextInputLayout in your activity layout
                                    TextInputLayout activityTextInputLayout = rootView.findViewById(R.id.locActivity);
                                    activityTextInputLayout.getEditText().setText(activity);
                                    originAutoCompleteTextView.setText(origin);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors that occur during the query
                    Log.e("FirebaseError", "Error: " + databaseError.getMessage());
                }
            });

            // Find the save button by its ID
            Button saveButton = rootView.findViewById(R.id.saveBt);

            // Set an OnClickListener for the save button
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Build the confirmation dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Confirm Save");
                    builder.setMessage("Are you sure you want to save the changes?");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Retrieve the updated location and time

                            TextInputLayout locationTextInputLayout = rootView.findViewById(R.id.locName);
                            String updatedLocation = locationTextInputLayout.getEditText().getText().toString();

                            TextInputLayout timeTextInputLayout = rootView.findViewById(R.id.locTime);

                            TextInputLayout activityTextInputLayout = rootView.findViewById(R.id.locActivity);
                            String updatedActivity = activityTextInputLayout.getEditText().getText().toString();

                            TextInputLayout originTextInputLayout = rootView.findViewById(R.id.originName);
                            String updatedOrigin = originTextInputLayout.getEditText().getText().toString();

                            String standardTime = Objects.requireNonNull(timeTextInputLayout.getEditText()).getText().toString();
                            String militaryTime = convertToMilitaryTime(standardTime); // convert to military time

                            // Check if any of the fields are empty
                            if (updatedLocation.isEmpty() || standardTime.isEmpty() || updatedActivity.isEmpty()) {
                                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_LONG).show();
                            } else {

                            String status = completionSwitch.isChecked() ? "Completed" : "Incompleted";


                            // Update the location and time in the database
                        if (completionSwitch.isChecked()) {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Itinerary");
                            Query activityQuery = databaseReference.child(iterName);
                            activityQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot daySnapshot : dataSnapshot.getChildren()) {
                                        for (DataSnapshot timeSnapshot : daySnapshot.getChildren()) {
                                            String retrievedLoc = timeSnapshot.child("location").getValue(String.class);
                                            if (timeSnapshot.getKey().equals(finalTime) && retrievedLoc.equals(location)) {
                                                // Remove the old timeSnapshot

                                                // Add a new timeSnapshot with the updatedTime as the key
                                                DatabaseReference newTimeSnapshot = daySnapshot.child(finalTime).getRef();
                                                newTimeSnapshot.child("status").setValue("Completed");

                                                showRatingDialog();
                                                // Show a toast to confirm the save
                                                Toast.makeText(getContext(), "Changes saved successfully.", Toast.LENGTH_LONG).show();
                                                dialog.dismiss();
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle any errors that occur during the query
                                    Log.e("FirebaseError", "Error: " + databaseError.getMessage());
                                }

                            });
                        } else {
                            // If the switch is not checked, set the status to "Incompleted" in the database
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Itinerary");
                            Query activityQuery = databaseReference.child(iterName);
                            activityQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot daySnapshot : dataSnapshot.getChildren()) {
                                        for (DataSnapshot timeSnapshot : daySnapshot.getChildren()) {
                                            String retrievedLoc = timeSnapshot.child("location").getValue(String.class);
                                            if (timeSnapshot.getKey().equals(finalTime) && retrievedLoc.equals(location)) {
                                                // Remove the old timeSnapshot
                                                timeSnapshot.getRef().removeValue();

                                                // Add a new timeSnapshot with the updatedTime as the key
                                                DatabaseReference newTimeSnapshot = daySnapshot.child(militaryTime).getRef();
                                                newTimeSnapshot.child("location").setValue(updatedLocation);
                                                newTimeSnapshot.child("activity").setValue(updatedActivity);
                                                newTimeSnapshot.child("origin").setValue(updatedOrigin);
                                                newTimeSnapshot.child("status").setValue("Incomplete");
                                                //newTimeSnapshot.child("status").setValue("incomplete");


                                                // Show a toast to confirm the save
                                                Toast.makeText(getContext(), "Changes saved successfully.", Toast.LENGTH_LONG).show();
                                                dialog.dismiss();
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle any errors that occur during the query
                                    Log.e("FirebaseError", "Error: " + databaseError.getMessage());
                                }
                            });
                        }

                            // Show a toast to confirm the save
                            Toast.makeText(getContext(), "Changes saved successfully.", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    // Create and show the dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        }

        // Find the cancel button by its ID
        Button cancelButton = rootView.findViewById(R.id.cancelBt);

        // Set an OnClickListener for the cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pop back the stack when the cancel button is clicked
                if (getFragmentManager() != null) {
                    getFragmentManager().popBackStack();
                }
            }
        });

        return rootView;
    }

    private String convertToMilitaryTime(String standardTime) {
        try {
            SimpleDateFormat standardFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            Date date = standardFormat.parse(standardTime);
            if (date != null) {
                SimpleDateFormat militaryFormat = new SimpleDateFormat("HH:mm", Locale.US);
                return militaryFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ""; // Return empty string if conversion fails
    }

    // Method to show the time picker dialog
    private void showTimePicker(TextInputLayout textInputLayout) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(textInputLayout.getContext(),
                (TimePicker view, int selectedHour, int selectedMinute) -> {
                    String selectedTime;
                    if (selectedHour >= 12) {
                        if (selectedHour > 12) {
                            selectedHour -= 12;
                        }
                        selectedTime = String.format("%02d:%02d PM", selectedHour, selectedMinute);
                    } else {
                        if (selectedHour == 0) {
                            selectedHour = 12;
                        }
                        selectedTime = String.format("%02d:%02d AM", selectedHour, selectedMinute);
                    }
                    if (textInputLayout.getEditText() != null) {
                        textInputLayout.getEditText().setText(selectedTime);
                    }
                }, hour, minute, false);

        timePickerDialog.show();
    }
    private void showRatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Rate this activity");
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.rating_dialog_layout, null);

        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        // Find the RatingBar widget in your rating_dialog_layout.xml

        builder.setView(view);
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle bundle = getArguments();
                if (bundle != null) {
                    String location = bundle.getString("location");
                    float userRating = ratingBar.getRating();

                    DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference().child("Location");
                    locationRef.orderByChild("Location").equalTo(location).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot placeSnapshot : snapshot.getChildren()) {
                                    String key = placeSnapshot.getKey();
                                    locationRef.child(key).child("Ratings").push().setValue(userRating);
                                }
                                calculateAverageRating(location);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle errors if necessary
                        }
                    });

                    dialog.dismiss();


                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void calculateAverageRating(String location) {
        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference().child("Location");
        locationRef.orderByChild("Location").equalTo(location).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot placeSnapshot : snapshot.getChildren()) {
                        float totalRating = 0;
                        int numOfRatings = 0;

                        DataSnapshot ratingsSnapshot = placeSnapshot.child("Ratings");
                        for (DataSnapshot ratingSnapshot : ratingsSnapshot.getChildren()) {
                            float rating = ratingSnapshot.getValue(Float.class);
                            totalRating += rating;
                            numOfRatings++;
                        }

                        // Calculate average rating
                        float averageRating = totalRating / numOfRatings;

                        // Set the average rating in the 'AverageRate' field in the database
                        placeSnapshot.getRef().child("AverageRate").setValue(String.valueOf(averageRating));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if necessary
            }
        });
    }

}

