package com.example.qsee;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
                                // Assuming you have initialized the Firebase database reference
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                                        .getReference().child("Itinerary").child(iterName).child(day);

                                String time = timeTextInputLayout.getEditText().getText().toString();
                                String activity = activityTextInputLayout.getEditText().getText().toString();
                                String location = locationTextInputLayout.getEditText().getText().toString();

                                String standardTime = Objects.requireNonNull(time);
                                String militaryTime = convertToMilitaryTime(standardTime); // convert to military time

                                databaseReference.child(militaryTime).child("activity").setValue(activity);
                                databaseReference.child(militaryTime).child("status").setValue("incomplete");
                                databaseReference.child(militaryTime).child("location").setValue(location)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getContext(), "Activity added to " + day, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(getContext(), "Failed to save data", Toast.LENGTH_SHORT).show();
                                                }
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
