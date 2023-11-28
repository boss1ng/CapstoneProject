package com.example.qsee;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AddItineraryActivity extends Fragment {

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_editactivity, container, false);
        // Retrieve the passed data
        Bundle args = getArguments();
        if (args != null) {
            String iterName = args.getString("iterName");
            String day = args.getString("day");

            TextView EditName = rootView.findViewById(R.id.editAct);
            EditName.setText("Add Activity");

            TextInputLayout activityTextInputLayout = rootView.findViewById(R.id.locActivity);
            TextInputLayout locationTextInputLayout = rootView.findViewById(R.id.locName);
            TextInputLayout originTextInputLayout = rootView.findViewById(R.id.originName);
            Switch completionswitch = rootView.findViewById(R.id.completionSwitch);

            completionswitch.setVisibility(View.GONE);

            AutoCompleteTextView originAutoCompleteTextView = (AutoCompleteTextView) Objects.requireNonNull(originTextInputLayout.getEditText());
            AutoCompleteTextView locationAutoCompleteTextView = (AutoCompleteTextView) Objects.requireNonNull(locationTextInputLayout.getEditText()); // Get the AutoCompleteTextView

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

                        originAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                                // Not used in this case
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                                // Check if the entered text is not present in the locationsList
                                String locationText = charSequence.toString();
                                if (!locationsList.contains(locationText)) {
                                    originAutoCompleteTextView.setError("Invalid location"); // Optionally, you can show an error message
                                } else {
                                    originAutoCompleteTextView.setError(null); // Clear the error if the location is valid
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                // Not used in this case
                            }
                        });

                        locationAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                                // Not used in this case
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                                // Check if the entered text is not present in the locationsList
                                String locationText = charSequence.toString();
                                if (!locationsList.contains(locationText)) {
                                    locationAutoCompleteTextView.setError("Invalid location"); // Optionally, you can show an error message
                                } else {
                                    locationAutoCompleteTextView.setError(null); // Clear the error if the location is valid
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                // Not used in this case
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle any errors
                }
            });


            TextInputLayout timeTextInputLayout = rootView.findViewById(R.id.locTime);

            Button saveButton = rootView.findViewById(R.id.saveBt);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Show a confirmation dialog
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Confirmation")
                            .setMessage("Are you sure you want to save this activity?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // Fetch data from Firebase Realtime Database
                                DatabaseReference locationsRef = FirebaseDatabase.getInstance().getReference().child("Location");
                                locationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        List<String> locationsList = new ArrayList<>();
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            String Location = snapshot.child("Location").getValue(String.class);
                                            locationsList.add(Location);
                                        }

                                        String time = timeTextInputLayout.getEditText().getText().toString();
                                        String activity = activityTextInputLayout.getEditText().getText().toString();
                                        String location = locationTextInputLayout.getEditText().getText().toString();
                                        String origin = originTextInputLayout.getEditText().getText().toString();

                                        // Check if any of the fields are empty
                                        if (time.isEmpty() || activity.isEmpty() || location.isEmpty()) {
                                            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        // Check if origin and location are in locationsList
                                        if (!locationsList.contains(origin) || !locationsList.contains(location)) {
                                            Toast.makeText(getContext(), "Origin or Location not in list", Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                                                .getReference().child("Itinerary").child(iterName).child(day);

                                        String standardTime = Objects.requireNonNull(time);
                                        String militaryTime = convertToMilitaryTime(standardTime); // convert to military time

                                        databaseReference.child(militaryTime).child("activity").setValue(activity);
                                        databaseReference.child(militaryTime).child("status").setValue("incomplete");
                                        databaseReference.child(militaryTime).child("origin").setValue(origin);
                                        databaseReference.child(militaryTime).child("location").setValue(location)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(getContext(), "Activity added to " + day, Toast.LENGTH_LONG).show();
                                                        } else {
                                                            Toast.makeText(getContext(), "Failed to save data", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                        DatabaseReference originReference = FirebaseDatabase.getInstance().getReference("Location");
                                        originReference.orderByChild("Location").equalTo(origin).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                                                    // Retrieve latitude and longitude for the origin
                                                    String firebaseOrigLat = locationSnapshot.child("Latitude").getValue(String.class);
                                                    String firebaseOrigLong = locationSnapshot.child("Longitude").getValue(String.class);

                                                    databaseReference.child(militaryTime).child("originLat").setValue(firebaseOrigLat);
                                                    databaseReference.child(militaryTime).child("originLong").setValue(firebaseOrigLong);
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                        DatabaseReference destReference = FirebaseDatabase.getInstance().getReference("Location");
                                        destReference.orderByChild("Location").equalTo(location).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                                                    // Retrieve latitude and longitude for the origin
                                                    String firebaseOrigLat = locationSnapshot.child("Latitude").getValue(String.class);
                                                    String firebaseOrigLong = locationSnapshot.child("Longitude").getValue(String.class);

                                                    databaseReference.child(militaryTime).child("locationLat").setValue(firebaseOrigLat);
                                                    databaseReference.child(militaryTime).child("locationLong").setValue(firebaseOrigLong);
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        // Handle any errors
                                    }
                                });
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });


            timeTextInputLayout.getEditText().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTimePicker(timeTextInputLayout);
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
