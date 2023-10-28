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
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class EditActivityFragment extends Fragment {

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_editactivity, container, false);

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
            locationTextInputLayout.getEditText().setText(location);

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

                                    // Update the TextInputLayout in your activity layout
                                    TextInputLayout activityTextInputLayout = rootView.findViewById(R.id.locActivity);
                                    activityTextInputLayout.getEditText().setText(activity);
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

                            String standardTime = Objects.requireNonNull(timeTextInputLayout.getEditText()).getText().toString();
                            String militaryTime = convertToMilitaryTime(standardTime); // convert to military time

                            // Update the location and time in the database
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
                                                    newTimeSnapshot.child("status").setValue("incomplete");

                                                // Show a toast to confirm the save
                                                Toast.makeText(getContext(), "Changes saved successfully", Toast.LENGTH_SHORT).show();
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

                            // Show a toast to confirm the save
                            Toast.makeText(getContext(), "Changes saved successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
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
}
